package com.hxw.wxchat.websocket.netty;

import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.utils.StringTools;
import com.hxw.wxchat.websocket.ChannelContextUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebsocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger logger= LoggerFactory.getLogger(HandlerWebsocket.class);
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChannelContextUtils channelContextUtils;
    /**
     * 通道就绪后，一般用户做初始化
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       // System.out.println(">>>>>>>>>>>>>>>>>>>>>有新的连接加入");
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>有新的连接加入");
       // System.out.println(ctx);
    }
    /**
     * 连接断开
     * */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      //  System.out.println(">>>>>>>>>>>>>>>>>>>>>连接断开");
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>连接断开");
        channelContextUtils.removeContext(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel=ctx.channel();
        Attribute<String> attribute= channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId=attribute.get();
        logger.info("收到userid消息()"+userId+":"+textWebSocketFrame.text());
        redisComponent.saveUserHeartBeat(userId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete complete=(WebSocketServerProtocolHandler.HandshakeComplete)evt;
            String url=complete.requestUri();
            String token=getToken(url);
            if (token==null){
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto tokenUserInfoDto=redisComponent.getTokenUserInfoDto(token);
            if (tokenUserInfoDto==null){
                ctx.channel().close();
                System.out.println("token 为null 断开连接。。。。。。。。。。。。。。。");
                return;
            }
            logger.info("url:()"+url);
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(),ctx.channel());


        }

    }

    private  String getToken(String url){
        if (StringTools.isEmpty(url)||url.indexOf("?")==-1){
            return null;
        }
        String [] queryParams=url.split("\\?");
        if (queryParams.length!=2){
            return null;
        }
        String [] params=queryParams[1].split("=");
        if (params.length!=2){
            return null;
        }
        return params[1];
    }
}
