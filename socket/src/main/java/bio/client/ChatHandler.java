package bio.client;

import bio.server.ChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author augenye
 * @date 2019/11/13 9:14 下午
 */
public class ChatHandler implements Runnable {

    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            //存储新上线用户
            server.addClient(socket);

            //读取用户发送来的消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String msg = null;
            //知道用户掉线或者主动退出以后
            while ((msg = reader.readLine()) != null) {
                String fwdMsg = "客户端【 " + socket.getPort() + " 】:" + msg + "\n";
                System.out.println(fwdMsg);
                //将收到的消息转发给聊天室里面的在线用户
                server.forwardMessage(socket, fwdMsg);
                //查看用户发送来的消息是否准备退出
                if (server.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
