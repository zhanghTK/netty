package tk.zhangh.netty.demo.time;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * Created by ZhangHao on 2017/10/25.
 */
@AllArgsConstructor
public class TimeServer {
    private int port;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        new TimeServer(port).run();
    }

    private void run() {
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boosGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    // TIME 协议：只发送包含32位整数的消息，二部接收任何请求，消息发送完成立刻关闭连接
    private static final class TimeServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) // 链接被建立并准备进行通信时被调用
                throws Exception {
            ByteBuf buffer = ctx.alloc().buffer(4);
            buffer.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

            // ChannelHandlerContext多数方法为异步，执行顺序不确定，需要添加监听器关闭
            ChannelFuture channelFuture = ctx.writeAndFlush(buffer);
            channelFuture.addListener(ChannelFutureListener.CLOSE);
//            channelFuture.addListener((ChannelFutureListener) future -> {
//                assert channelFuture == future;
//                ctx.close();
//            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
