package niomodel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author augenye
 * @date 2019/11/15 12:31 上午
 */
public class ChatServer {
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector; // 处理channel上各种事件
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);//处理通道的读取
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);//转发消息给其他用户的channel
    private Charset charset = StandardCharsets.UTF_8;
    private int port;
    private SelectableChannel channel;


    public ChatServer(int port) {
        this.port = port;
    }

    public ChatServer() {
        this(DEFAULT_PORT);
    }


    public void start() {
        try {
            server = ServerSocketChannel.open();
            //处于非阻塞式调用的状态
            server.configureBlocking(false);
            //绑定为当前的端口
            server.socket().bind(new InetSocketAddress(port));
            //开启一个selector对象
            selector = Selector.open();
            //开始监视注册的channel, SelectionKey Accept 事件的发生
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器， 监听端口：" + port + "...");

            //获取几条channel被监听
            while (true) {
                selector.select();
                //所监听的事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    //处理每一个触发的事件
                    handles(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // accept 事件 和客户端建立连接
        if (key.isAcceptable()) {
            SocketChannel client = (SocketChannel) key.channel();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("客户端【 " + client.socket().getPort() + " 】已链接");
        }
        // read事件 客户端发送了消息
        else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client);
            if (fwdMsg.isEmpty()) {
                //客户端出现异常, 不在监听客户端上发来的消息
                // 取消对这个通道的监听
                key.cancel();
                // 提醒 select 状态更新，最新状况
                selector.wakeup();
            } else {
                //转发消息给除了自己外的在线用户
                forwardMessage(client, fwdMsg);
                //检查用户是否退出
                if (readyToQuit(fwdMsg)) {
                    key.cancel();
                    selector.wakeup();
                    System.out.println("客户端【 "+client.socket()+ "】已断开");
                }
            }
        }
    }

    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            channel = key.channel();
            if (channel instanceof ServerSocketChannel) {
                continue;
            }
            if (key.isValid() && !client.equals(channel)) {
                wBuffer.clear();
                wBuffer.put(charset.encode(fwdMsg));
                wBuffer.flip();//转手写到通道里
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel) channel).write(wBuffer);
                }
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();//重置buffer,免得造成消息的污染
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(DEFAULT_PORT);
        chatServer.start();
    }

}
