package tk.zhangh.netty.ch2.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * P31 NIO时间服务器
 * Created by ZhangHao on 17/7/16.
 */
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;
    private ServerSocketChannel servChannel;
    private volatile boolean stop;

    /**
     * 初始化多路复用器、绑定监听端口
     */
    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();
            servChannel.configureBlocking(false);  // 设置为异步阻塞模式
            servChannel.socket().bind(new InetSocketAddress(port), 1024);// 设置 backlog =1024  ， requested maximum length of the queue of incoming connections.
            servChannel.register(selector, SelectionKey.OP_ACCEPT); // 服务端socket监听链接操作
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        /*
         * 循环遍历 selector ，休眠 1s。
         */
        while (!stop) {
            try {
                // 每一秒唤醒一次
                selector.select(1000);
                // 返回所有就绪状态的channel集合
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                // 遍历就绪状态集合
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            // 根据操作位获知网络事件的类型
            if (key.isAcceptable()) {
                // 处理新接入的请求消息
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();  // 类型为ACCEPT建立连接（相当于TCP 3 次握手），
                sc.configureBlocking(false);  // 设置客户端链接为非阻塞的

                sc.register(selector, SelectionKey.OP_READ); // 客户端socket监听读操作

            }

            if (key.isReadable()) {
                // 读取客户端的请求消息
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024); // 1MB 的缓冲区
                int readBytes = sc.read(readBuffer); // 读取请求流
                if (readBytes > 0) {
                    // 读到字节
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];  // 根据缓冲区可读的数组复制到新创建的字节数组中
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    // 直接回写
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    // 链路已关闭
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    // 没有读到
                    //  读到 0 字节， 忽略
                }

            }
        }
    }

    private void doWrite(SocketChannel sc, String response) throws IOException {
        // 可能出现"写半包"，未处理
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();

            sc.write(writeBuffer);
        }
    }

    public void stop() {
        this.stop = true;
    }

}
