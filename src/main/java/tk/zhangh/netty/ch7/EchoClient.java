package tk.zhangh.netty.ch7;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by ZhangHao on 2017/10/26.
 */
public class EchoClient {

    static int port = 8080;
    static String host = "localhost";
    public static void main(String[] args) {
        port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
            }
        }

        new EchoClient().run();
    }

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 处理半包
                        ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0,2,0,2));
                        ch.pipeline().addLast("msgpack decoder", new MsgpackDecoder());  // 解码器
                        ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));  // 增加消息长度
                        ch.pipeline().addLast("msgpack encoder", new MsgpackEncoder());  // 编码器
                        ch.pipeline().addLast(new EchoClientHandler());
                    }
                });
        try {
            ChannelFuture f = bootstrap.connect(host, port).sync(); // 发起异步连接操作
            f.channel().closeFuture().sync();// 等待客户端链路关闭。
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            group.shutdownGracefully();
        }
    }

    private static final class EchoClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            for (UserInfo userInfo : userinfos()) {
                ctx.write(userInfo);
            }
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Client receive the msgpack message:" + msg);
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private UserInfo[] userinfos() {
            UserInfo[] userInfos = new UserInfo[20];
            for (int i = 0; i < userInfos.length; i++) {
                UserInfo userInfo = new UserInfo();
                userInfo.setAge(i);
                userInfo.setName("TEST" + i);
                userInfos[i] = userInfo;
            }
            return userInfos;
        }
    }
}
