package simpleIO.client;

import java.io.*;
import java.net.Socket;

/**
 * @author augenye
 * @date 2019/11/13 11:31 上午
 */
public class Client {

    private static BufferedWriter bufferedWriter;
    private static BufferedReader bufferedReader;

    public static void main(String[] args) {
        final String QUIT = "quit";
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8888;
        Socket socket = null;

        // 创建socket
        try {
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            //创建io流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //等待用户输入信息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                // 发送消息给服务器
                bufferedWriter.write(input + "\n");
                bufferedWriter.flush();

                //读取服务器返回的消息
                String msg = bufferedReader.readLine();
                System.out.println(msg);

                //检查用户是否退出
                if (QUIT.equals(input)) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    System.out.println("关闭writer");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
