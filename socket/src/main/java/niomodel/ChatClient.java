package niomodel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author augenye
 * @date 2019/11/15 6:31 下午
 */
public class ChatClient {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";

    private String host;
    private int port;
    private SocketChannel client;
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer wBuffer = ByteBuffer.allocate(1024);
    private Selector selector;
    private Charset charset = StandardCharsets.UTF_8;

    public ChatClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }

    public void start() {
        //开启一个socketChannel通道
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            //打开select
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(host, port));

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    handles(key);
                }
                //清空keys，避免旧的key混杂在新里边
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //当客户端发送连接请求后
        // 即Connect 事件 - 连接就绪事件

        if (key.isConnectable()) {
            //返回true则是说明处于连接就绪状态
            SocketChannel client = (SocketChannel) key.channel();
            //false应当继续等待
            if (client.isConnectionPending()) {
                //正式建立连接
                client.finishConnect();
                //此时应当处理用户的输入
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector, SelectionKey.OP_READ);
        }
        // READ 事件 - 服务器转发消息
        else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            if (msg.isEmpty()) {
                //服务端异常
                close(selector);
            } else {
                System.out.println(msg);
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String msg) throws IOException {
        if (msg.isEmpty()) {
            return;
        }
        if (readyToQuit(msg)) {
            close(selector);
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }

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
        ChatClient client = new ChatClient(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
        client.start();
    }



}
