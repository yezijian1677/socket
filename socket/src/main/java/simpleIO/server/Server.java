package simpleIO.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author augenye
 * @date 2019/11/12 11:22 下午
 */
public class Server {
    public static void main(String[] args) {
        final String QUIT = "quit";
        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;

        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口" + DEFAULT_PORT);

            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端【 " + socket.getPort() + " 】已经连接 ");
                //获取客户端输入输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String msg = null;
                while ((msg = bufferedReader.readLine()) != null) {
                    //读取客户端发送的消息, readline 读取一行数据
                    System.out.println("客户端【 " + socket.getPort() + " 】：" + msg);

                    //回送数据
                    bufferedWriter.write("服务器：" + msg + "\n");
                    //保证把缓冲区所有的数据发送出去
                    bufferedWriter.flush();

                    //直到退出
                    if (QUIT.equals(msg)) {
                        System.out.println("客户端【 " + socket.getPort() + "】已经断开连接");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                System.out.println("关闭socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
