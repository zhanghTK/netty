package tk.zhangh.netty.ch2.aio.server;

import java.io.IOException;

/**
 * P46 AIO时间服务器服务端
 * Created by ZhangHao on 17/7/16.
 */
public class TimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
        new Thread(timeServer, "AIO-AsyncTimeServerHandler-001").start();
    }

}
