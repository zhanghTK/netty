package tk.zhangh.netty.ch2.nio;

import java.io.IOException;

/**
 * P30 NIO创建TimeServer
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
        MultiplexerTimeServer timeServer = new MultiplexerTimeServer(port);
        new Thread(timeServer, "NIO-MultiplexerTimeServer-001").start();    // 启动一个多路复用器 Selector。
    }
}
