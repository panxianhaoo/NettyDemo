package com.panxianhao.client.handler;

import com.panxianhao.entity.SimpleMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author <a href="panxianhao@wxchina.com">panxianhao</a>
 * @createTime 2022/9/22 16:56
 * @description
 */
public class GroupChatClientHandler extends SimpleChannelInboundHandler<SimpleMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleMessage simpleMessage) throws Exception {
        System.out.println(simpleMessage.getContent());
    }
}
