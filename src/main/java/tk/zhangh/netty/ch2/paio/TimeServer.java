package tk.zhangh.netty.ch2.paio;

import tk.zhangh.netty.ch2.bio.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * P19 伪异步IO的TimeServer
 * Created by ZhangHao on 17/7/16.
 */
public class TimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port : " + port);
            Socket socket;
            TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50, 1000);
            while (true) {
                socket = server.accept();
                singleExecutor.execute(new TimeServerHandler(socket));    // 在此处多了个线程池模型
            }
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
            }
        }
    }
}
