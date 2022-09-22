package com.panxianhao.client;

import com.panxianhao.client.handler.GroupChatClientHandler;
import com.panxianhao.entity.SimpleMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * @author <a href="panxianhao@wxchina.com">panxianhao</a>
 * @createTime 2022/9/22 16:39
 * @description
 */
public class Client {

    private static final String HOST = "127.0.0.1";
    private static final Integer PORT = 8888;

    public void start() throws InterruptedException {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new ProtobufDecoder(SimpleMessage.getDefaultInstance()));
                        pipeline.addLast("encoder", new ProtobufEncoder());
                        pipeline.addLast(new GroupChatClientHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect(HOST, PORT).sync();
        Channel channel = channelFuture.channel();
        System.out.println("-----" + channel.localAddress() + "-----");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            SimpleMessage message = SimpleMessage.newBuilder().setContent(msg).setName("client").build();
            channel.writeAndFlush(message);
        }
        workerGroup.shutdownGracefully();
    }
}
