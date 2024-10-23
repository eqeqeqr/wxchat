package com.hxw.wxchat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.SysSettingDto;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.enums.MessageStatusEnum;
import com.hxw.wxchat.entity.enums.MessageTypeEnum;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.entity.po.ChatSession;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.ChatSessionMapper;
import com.hxw.wxchat.mappers.ChatSessionUserMapper;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.openjdk.nashorn.internal.objects.NativeUint8Array;
import org.springframework.stereotype.Service;

import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.query.ChatMessageQuery;
import com.hxw.wxchat.entity.po.ChatMessage;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.query.SimplePage;
import com.hxw.wxchat.mappers.ChatMessageMapper;
import com.hxw.wxchat.service.ChatMessageService;
import com.hxw.wxchat.utils.StringTools;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

	@Resource
	private ChatMessageMapper chatMessageMapper;

	@Resource
	private RedisComponent redisComponent;
	@Resource
	private ChatSessionMapper chatSessionMapper;
	@Resource
	private MessageHandler messageHandler;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatMessage> findListByParam(ChatMessageQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return this.chatMessageMapper.selectList(queryWrapper);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatMessageQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.chatMessageMapper.selectCount(queryWrapper));
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatMessage> list = this.findListByParam(param);
		PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatMessage bean) {
		return this.chatMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatMessage> listBean) {
		return null;
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
		return null;
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
		return null;
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatMessageQuery param) {
		return null;
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public ChatMessage getChatMessageByMessageId(Long messageId) {
		return null;
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
		return null;
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteChatMessageByMessageId(Long messageId) {
		return null;
	}

	@Override
	public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {

		// 如果不是机器人回复,判断好友状态
		if (Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())){
			List<String > contactList=redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
			if (!contactList.contains(chatMessage.getContactId())){
				UserContactTypeEnum userContactTypeEnum=UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
				if (UserContactTypeEnum.USER==userContactTypeEnum){
					new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"您不是对方好友1",null);
				}else {
					new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"您不是对方好友2", null);
				}
			}

		}
		String sessionId=null;

		String sendUserId=tokenUserInfoDto.getUserId();
		String contactId=chatMessage.getContactId();

		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (UserContactTypeEnum.USER==contactTypeEnum){
			sessionId=StringTools.getChatSessionId4User(new String[]{sendUserId,contactId});

		}else {
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}

		chatMessage.setSessionId(sessionId);
		Long curTime=System.currentTimeMillis();
		chatMessage.setSendTime(curTime);

		MessageTypeEnum messageTypeEnum=MessageTypeEnum.getByType(chatMessage.getMessageType());
		System.out.println(messageTypeEnum+""+chatMessage.getMessageType());
		if (messageTypeEnum==null|| !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(),MessageTypeEnum.MEDIA_CHAT.getType()},chatMessage.getMessageType())){
			throw new BusinessException("发送失败");
		}
		Integer status=MessageTypeEnum.MEDIA_CHAT==messageTypeEnum? MessageStatusEnum.SENDED.getStatus() :MessageStatusEnum.SENDED.getStatus();
		chatMessage.setStatus(status);

		String messageContent=StringTools.cleanHtmlTag(chatMessage.getMessageContent());
		chatMessage.setMessageContent(messageContent);

		//更新会话
		ChatSession chatSession=new ChatSession();
		chatSession.setLastMessage(messageContent);
		if (UserContactTypeEnum.GROUP==contactTypeEnum){
			chatSession.setLastMessage(tokenUserInfoDto.getNickName()+":"+messageContent);
		}

		chatMessage.setLastReceiveTime(curTime);
		System.out.println(">>>>><<<<<<>>>>>>>"+chatSession.getSessionId()+" = "+chatSession.getLastMessage()+" = "+chatSession.getLastReceiveTime()+" = "+sessionId);

		chatSessionMapper.updateBysessionId(chatSession,sessionId);


		// 记录消息表
		chatMessage.setSendUserId(sendUserId);
		chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
		chatMessage.setContactType(contactTypeEnum.getType());
		chatMessageMapper.insert(chatMessage);




		// 发送消息
		MessageSendDto messageSendDto= CopyTools.copy(chatMessage,MessageSendDto.class);


		if (Constants.ROBOT_UID.equals(contactId)){
			SysSettingDto sysSettingDto=redisComponent.getSysSetting();
			TokenUserInfoDto robot=new TokenUserInfoDto();
			robot.setUserId(sysSettingDto.getRobotUid());
			robot.setNickName(sysSettingDto.getRobotNickName());
			ChatMessage robotChatMessage=new ChatMessage();
			robotChatMessage.setContactId(sendUserId);

			//这里可以对接AI
			robotChatMessage.setMessageContent("我只是一个机器人无法识别");
			robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
			saveMessage(robotChatMessage,robot);

		}else {
			messageHandler.sendMessage(messageSendDto);
		}


		return messageSendDto;
	}
}