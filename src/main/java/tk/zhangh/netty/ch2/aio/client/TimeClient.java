package tk.zhangh.netty.ch2.aio.client;

/**
 * P46 AIO时间服务器客户端
 * Created by ZhangHao on 17/7/16.
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "AIO-AsyncTimeClientHandler-001").start();
    }
}
