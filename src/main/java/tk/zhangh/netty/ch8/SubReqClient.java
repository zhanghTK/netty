package tk.zhangh.netty.ch8;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * Created by ZhangHao on 17/7/18.
 */
public class SubReqClient {

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // TODO 请自行扩展.
            }
        }

        new SubReqClient().connect(port, "localhost");
    }

    public void connect(int port, String host) {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ProtobufVarint32FrameDecoder())  // 解码
                                .addLast(new ProtobufDecoder(SubscribeRespProto.SubscribeResp.getDefaultInstance()))  // 解码
                                .addLast(new ProtobufVarint32LengthFieldPrepender())  // 编码
                                .addLast(new ProtobufEncoder())
                                .addLast(new SubReqClientHandler());
                    }
                });
        try {
            ChannelFuture f = b.connect(host, port).sync(); // 发起异步连接操作
            f.channel().closeFuture().sync();// 等待客户端链路关闭。
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            group.shutdownGracefully();
        }
    }

}
