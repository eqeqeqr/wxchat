package com.hxw.wxchat.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerHeartBeat extends ChannelDuplexHandler {
    private static final Logger logger=LoggerFactory.getLogger(HandlerHeartBeat.class);
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent e=(IdleStateEvent) evt;
            if (e.state()== IdleState.READER_IDLE){
                Channel channel=ctx.channel();
              //  System.out.println(">>>>>>>>>>>>>>>>>>>>>心跳超时");
                Attribute<String> attribute= channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId=attribute.get();
                logger.info(">>>>>>>>>>>>>>>>>>>>>心跳超时"+userId);
                ctx.close();
            }else if (e.state()==IdleState.WRITER_IDLE){
                ctx.writeAndFlush("heart");
            }



        }
    }
}
