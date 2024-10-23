package com.hxw.wxchat.websocket;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxw.wxchat.controller.ABaseController;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.WsInitData;
import com.hxw.wxchat.entity.enums.MessageTypeEnum;
import com.hxw.wxchat.entity.enums.UserContactApplyStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.entity.po.ChatMessage;
import com.hxw.wxchat.entity.po.ChatSessionUser;
import com.hxw.wxchat.entity.po.UserContactApply;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.query.ChatMessageQuery;
import com.hxw.wxchat.entity.query.ChatSessionUserQuery;
import com.hxw.wxchat.entity.query.UserContactApplyQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.mappers.*;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.service.ChatSessionUserService;
import com.hxw.wxchat.utils.JsonUtils;
import com.hxw.wxchat.utils.StringTools;
import com.hxw.wxchat.websocket.netty.HandlerWebsocket;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.AnnotationUtils;
import org.aspectj.lang.annotation.DeclareError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Component
public class ChannelContextUtils {
    private static final Logger logger= LoggerFactory.getLogger(ChannelContextUtils.class);


    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP=new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP=new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;
    @Resource
    private UserContactApplyMapper userContactApplyMapper;
    public void addContext(String userId, Channel channel){
        String channelId=channel.id().toString();
        logger.info(">>>>>>>>>>>>>>channnelId:"+channelId);
        AttributeKey attributeKey=null;
        if (!AttributeKey.exists(channelId)){
            attributeKey=AttributeKey.newInstance(channelId);
        }else {
            attributeKey=AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
         List<String> contactIdList=redisComponent.getUserContactList(userId);

           for (String groupId : contactIdList) {
               if (groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())) {
                   add2Group(groupId, channel);
               }
           }

        USER_CONTEXT_MAP.put(userId,channel);
        redisComponent.saveUserHeartBeat(userId);

        // 更新用户最后连接的时间
        UserInfo updateInfo=userInfoMapper.selectByUserId(userId);
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo,userId);

        //给用户发送消息  最多三天
        UserInfo userInfo=userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime=userInfo.getLastOffTime();
        Long lastOffTime=sourceLastOffTime;
        if (sourceLastOffTime!=null&&(System.currentTimeMillis()- Constants.MILLIS_ECONDS_3days_age)>sourceLastOffTime){
            lastOffTime=Constants.MILLIS_ECONDS_3days_age;
        }
        /**
         * 1.查询会话消息  查询用户的所有消息，保证会话同步
         */
        ChatSessionUserQuery sessionUserQuery=new ChatSessionUserQuery();
        sessionUserQuery.setUserId(userId);
        sessionUserQuery.setOrderBy("last_receive_time desc");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>uuu"+userId);
        List<ChatSessionUser> chatSessionUserList=chatSessionUserMapper.selectList(sessionUserQuery);

        WsInitData wsInitData=new WsInitData();
        wsInitData.setChatSessionUserList(chatSessionUserList);
        /**
         * 2.查询聊天消息
         */
        //查询所有联系人
        List<String> groupIdList=contactIdList.stream().filter(item->item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);//加上自己
        ChatMessageQuery chatMessageQuery=new ChatMessageQuery();
        chatMessageQuery.setContactIdList(groupIdList);
        chatMessageQuery.setLastReceiveTime(lastOffTime);


        List<ChatMessage> chatMessageList=chatMessageMapper.selectList(chatMessageQuery);
        wsInitData.setChatMessageList(chatMessageList);
        /**
         * 3.查询好友申请
         */

        UserContactApplyQuery applyQuery=new UserContactApplyQuery();
        applyQuery.setReceiveUserId(userId);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        applyQuery.setLastApplyTime(lastOffTime);
        Integer applyCount= Math.toIntExact(userContactApplyMapper.selectCount(applyQuery));
        wsInitData.setApplyCount(applyCount);

        // 发送消息
        MessageSendDto messageSendDto=new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);
        sendMsg(messageSendDto,userId);




    }
//TODO 添加用户到群组 还不会
    public void addUser2Group(String userId,String groupId){
        Channel userChannel=USER_CONTEXT_MAP.get(userId);

        if (userChannel==null){
              return;
        }

        add2Group(groupId,userChannel);

    }

    private void add2Group(String groupId,Channel channel){
        ChannelGroup group=GROUP_CONTEXT_MAP.get(groupId);
        if (group==null){
            group=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId,group);
        }
        if (channel==null){
            return;
        }
        group.add(channel);
    }

    public void removeContext( Channel channel){
        Attribute<String> attribute= channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId=attribute.get();
        if (!StringTools.isEmpty(userId)){
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        //更新用户最后离线时间
        UserInfo userInfo=userInfoMapper.selectByUserId(userId);
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(userInfo,userId);
    }



    public void sendMessage(MessageSendDto messageSendDto){
        UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contactTypeEnum){
            case USER :
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
        }
    }
    // 发送给用户
    private void  send2User(MessageSendDto messageSendDto) {
        String contactId=messageSendDto.getContactId();
        if (StringTools.isEmpty(contactId)){
       return;
        }
        sendMsg(messageSendDto,contactId);
        //强制下线
        if(MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())){
            closeContext(contactId);
        }
    }
    public void closeContext(String userId){
        if (StringTools.isEmpty(userId)){
            return;
        }
        redisComponent.clearUserTokenByUserId(userId);
        Channel channel=USER_CONTEXT_MAP.get(userId);
        if (channel==null){
            return;
        }
        channel.close();
    }
    // 发送给群组
    private void send2Group(MessageSendDto messageSendDto){
        String contactId=messageSendDto.getContactId();
        if (StringTools.isEmpty(contactId)) {
            return;
        }
        ChannelGroup channelGroup=GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (channelGroup==null){
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.beanToJson(messageSendDto)));

    }
    // 发送消息
    public void sendMsg(MessageSendDto messageSendDto,String receiveId){

        Channel userChannel=USER_CONTEXT_MAP.get(receiveId);
        if (userChannel==null){
            return;
        }
        //相对于客户端 而言 联系人就是发送人，所以这里转一下再发送
        if (!MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
           UserInfo userInfo= (UserInfo) messageSendDto.getExtendData();
           if (userInfo!=null) {
               messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
               messageSendDto.setContactId(userInfo.getUserId());
               messageSendDto.setContactName(userInfo.getNickName());
               messageSendDto.setExtendData(null);
           }
        }else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactName(messageSendDto.getSendUserNickName());

        }
        userChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.beanToJson(messageSendDto)));



    }

}
