package tk.zhangh.netty.demo.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ZhangHao on 2017/10/25.
 */
public class TimeClient {
    public static void main(String[] args) {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    private static final class TimeClientHandler extends ChannelInboundHandlerAdapter {

//        private ByteBuf buf;
//
//        @Override
//        public void handlerAdded(ChannelHandlerContext ctx) // 生命周期初始化监听方法
//                throws Exception {
//            buf = ctx.alloc().buffer(4);
//        }
//
//        @Override
//        public void handlerRemoved(ChannelHandlerContext ctx) // 生命周期销毁监听方法
//                throws Exception {
//            buf.release();
//            buf = null;
//        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
            Date currentTime = new Date(currentTimeMillis);
            System.out.println("Default Date Format:" + currentTime.toString());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(currentTime);
            System.out.println("Date Format:" + dateString);
            ctx.close();

//            ByteBuf m = (ByteBuf) msg;
//            buf.writeBytes(m);
//            m.release();
//            // 检查 buf 中是否有足够的数据，不满足时netty将再次调用channelRead()最终累积到达4个字节，再执行
//            if (buf.readableBytes() >= 4) {
//                long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
//                System.out.println(new Date(currentTimeMillis));
//                ctx.close();
//            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static final class TimeDecoder
            extends ByteToMessageDecoder {  // ByteToMessageDecoder是ChannelInboundHandler的一个实现，它使得处理碎片问题变得容易

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            if (byteBuf.readableBytes() < 4) {
                return;
            }
            list.add(byteBuf.readBytes(4));  // 将对象添加到list意味着解码器成功解码，已被读取部分将被丢弃
        }
    }
}
