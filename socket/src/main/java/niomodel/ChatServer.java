package niomodel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author augenye
 * @date 2019/11/15 12:31 上午
 */
public class ChatServer {
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);//处理通道的读取
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);//转发消息给其他用户的channel
    private Charset charset = StandardCharsets.UTF_8;
    private int port;


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
        } catch (IOException e) {
            e.printStackTrace();
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

}
