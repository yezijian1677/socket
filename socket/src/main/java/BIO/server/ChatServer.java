package BIO.server;

import BIO.client.ChatHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author augenye
 * @date 2019/11/13 8:43 下午
 */
public class ChatServer {
    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    // 方便接收各个客户端发起的请求
    private ServerSocket serverSocket;
    //端口 ： 输出的writer
    private Map<Integer, Writer> connectedClients;

    public ChatServer() {
        connectedClients = new ConcurrentHashMap<>();
    }

    //添加客户端
    public void addClient(Socket socket) throws IOException {
        // 对应的端口
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端【 " + port + " 】连接到服务器");

        }
    }

    /**
     * 移除客户端
     *
     * @param socket socket
     * @throws IOException thr
     */
    public void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            if (connectedClients.containsKey(port)) {
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端【 " + port + " 】断开连接");
        }
    }

    /**
     * 传递消息
     *
     * @param socket socket
     * @param fwdMsg 转发的消息
     * @throws IOException thr
     */
    public void forwardMessage(Socket socket, String fwdMsg) throws IOException {
        for (Integer id : connectedClients.keySet()) {
            if (!id.equals(socket.getPort())) {
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    public synchronized void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                System.out.println("关闭server socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器， 监听端口：" + DEFAULT_PORT + "...");

            //监听客户端请求的连接
            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建一个handler线程
                new Thread(new ChatHandler(this, socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }


}
