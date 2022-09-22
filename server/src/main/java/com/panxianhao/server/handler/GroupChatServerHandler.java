package com.panxianhao.server.handler;

import com.panxianhao.entity.SimpleMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="panxianhao@wxchina.com">panxianhao</a>
 * @createTime 2022/9/22 14:46
 * @description
 */
public class GroupChatServerHandler extends SimpleChannelInboundHandler<SimpleMessage> {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleMessage simpleMessage) throws Exception {
        Channel channel = ctx.channel();
        for (Channel c : channels) {
            if (c != channel) {
                c.writeAndFlush(addPrefix(simpleMessage, c.remoteAddress().toString(), false));
            }
            else {
                c.writeAndFlush(addPrefix(simpleMessage, c.remoteAddress().toString(), true));
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[Client] " + ctx.channel().remoteAddress() + " On Line");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[Client] " + ctx.channel().remoteAddress() + " Off Line");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        SimpleMessage message = SimpleMessage.newBuilder().setName("server").setContent("[Client] " + channel.remoteAddress() + " Joined Chat ").build();
        channels.writeAndFlush(message);
        channels.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        SimpleMessage message = SimpleMessage.newBuilder().setName("server").setContent("[Client] " + ctx.channel().remoteAddress() + " Left Chat ").build();
        channels.writeAndFlush(message);
    }

    private SimpleMessage addPrefix(SimpleMessage simpleMessage, String address, boolean isSender) {
        if (!isSender) {
            return SimpleMessage.newBuilder().setName(address).setContent("[Client] " + address + ":" + simpleMessage.getContent()).build();
        }
        return SimpleMessage.newBuilder().setName(address).setContent("You Say:" + simpleMessage.getContent()).build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
