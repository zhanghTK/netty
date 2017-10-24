package tk.zhangh.netty.ch2.aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * P48 AIO时间服务器服务端
 * Created by ZhangHao on 17/7/16.
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    /**
     * 处理成功
     *
     * @param result     IO 调用的结果
     * @param attachment 发起调用时传入的attachment
     */
    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
        // 继续接收其它客户端连接
        attachment.asynchronousServerSocketChannel.accept(attachment, this);
        // 异步读
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(
                buffer,  // 目标缓冲区
                buffer,  // ReadCompletionHandler.completed 的 AsyncTimeServerHandler 参数
                new ReadCompletionHandler(result)
        );
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown();
    }
}
