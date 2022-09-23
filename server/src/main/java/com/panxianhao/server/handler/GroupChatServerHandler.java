package com.panxianhao.server.handler;

import com.panxianhao.entity.SimpleMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
    private int times = 0;

    private static final int MAX_TIMES = 3;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleMessage simpleMessage) throws Exception {
        Channel channel = ctx.channel();
        for (Channel c : channels) {
            if (c != channel) {
                c.writeAndFlush(addPrefix(simpleMessage, c.remoteAddress().toString(), false));
            } else {
                c.writeAndFlush(addPrefix(simpleMessage, c.remoteAddress().toString(), true));
            }
        }
        times = 0;
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                System.out.println(ctx.channel().remoteAddress() + " write time out");
                times++;
                if (times == MAX_TIMES) {
                    System.out.println("closed " + ctx.channel().remoteAddress() + " due to write time out " + MAX_TIMES + "times");
                    ctx.channel().close();
                }
            }
        }
    }
}
