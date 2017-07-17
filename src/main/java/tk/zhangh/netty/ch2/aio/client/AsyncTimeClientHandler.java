package tk.zhangh.netty.ch2.aio.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * P46 AIO时间服务器客户端
 * Created by ZhangHao on 17/7/16.
 */
public class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {

    private AsynchronousSocketChannel client;
    private String host;
    private int port;
    private CountDownLatch latch;

    public AsyncTimeClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        // 发起异步操作
        // attachment：回调通知时作为入参传递
        // handler：异步操作回调通知接口
        client.connect(new InetSocketAddress(host, port), this, this);
        try {
            latch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, AsyncTimeClientHandler attachment) {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        // 异步写
        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            // 异步写的回调
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    client.write(buffer, buffer, this);
                } else {
                    // 异步读取服务端应答消息处理
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    // 异步读
                    client.read(
                            readBuffer,
                            readBuffer,
                            new CompletionHandler<Integer, ByteBuffer>() {
                                @Override
                                public void completed(Integer result, ByteBuffer buffer) {
                                    buffer.flip();
                                    byte[] bytes = new byte[buffer.remaining()];
                                    buffer.get(bytes);
                                    String body;
                                    try {
                                        body = new String(bytes, "UTF-8");
                                        System.out.println("Now is : " + body);
                                        latch.countDown();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, ByteBuffer attachment) {
                                    try {
                                        client.close();
                                        latch.countDown();
                                    } catch (IOException e) {
                                        // ingnore on close
                                    }
                                }
                            });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                    latch.countDown();
                } catch (IOException e) {
                    // ingnore on close
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
        exc.printStackTrace();
        try {
            client.close();
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
