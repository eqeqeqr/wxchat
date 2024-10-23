package com.hxw.wxchat.websocket;

import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.utils.JsonUtils;
import org.redisson.Redisson;
import org.redisson.RedissonStream;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.PushBuilder;

/**
 *  消息处理
 */
@Component("messageHandler")
public class MessageHandler {
    private static final Logger logger= LoggerFactory.getLogger(MessageHandler.class);
    private static final String MESSAGE_TOPIC="message.topic";
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;
    @PostConstruct
    public void LisMessage(){

        RTopic rTopic=redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class,(MessageSendDto,sendDto)->{
            logger.info("收到广播消息："+ JsonUtils.beanToJson(sendDto));
           channelContextUtils.sendMessage(sendDto);
        });
    }



    public void sendMessage(MessageSendDto sendDto){
        RTopic rTopic=redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(sendDto);

    }
}
