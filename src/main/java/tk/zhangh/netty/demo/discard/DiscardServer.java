package tk.zhangh.netty.demo.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * Created by ZhangHao on 2017/10/25.
 */
@AllArgsConstructor
public class DiscardServer {
    private int port;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        new DiscardServer(port).run();
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();  // 处理连接的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // 处理其他事件的线程池
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)  // 指定 channel 类型
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)  // 设置bossGroup参数
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  // 设置workerGroup参数

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();  // 同步绑定端口
            channelFuture.channel().closeFuture().sync();  // 同步等待服务器关闭
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
