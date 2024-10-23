package com.hxw.wxchat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.SysSettingDto;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.dto.UserContactSearchResultDto;
import com.hxw.wxchat.entity.enums.*;
import com.hxw.wxchat.entity.po.*;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.*;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.service.UserContactApplyService;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.utils.StringTools;
import com.hxw.wxchat.websocket.ChannelContextUtils;
import com.hxw.wxchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.hxw.wxchat.entity.query.UserContactQuery;

import com.hxw.wxchat.service.UserContactService;


/**
 * 联系人 业务接口实现
 * @author qw1500292505
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

	@Resource
	private UserContactMapper userContactMapper;
	@Resource
	private UserInfoMapper userInfoMapper;
	@Resource
	private GroupInfoMapper groupInfoMapper;
	@Resource
	private UserContactApplyMapper userContactApplyMapper;

	@Resource
	private RedisComponent redisComponent;
	@Resource
	private ChatSessionMapper chatSessionMapper;
	@Resource
	private ChatSessionUserMapper chatSessionUserMapper;
	@Resource
	private ChatMessageMapper chatMessageMapper;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private ChannelContextUtils channelContextUtils;
	@Override
	public List<UserContact> findListByParam(UserContactQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		/*if (param.getQueryUserInfo()||param.getQueryContactUserInfo()){
			return userContactMapper.findListByParams(param.getContactId());
		}else {*/
			return userContactMapper.selectList(param);
		//}
	}

	@Override
	public Integer findCountByParam(UserContactQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(userContactMapper.selectCount(queryWrapper));
	}

	@Override
	public IPage<UserContact> findListByPage(Page<UserContact> page) {
		// 使用 MyBatis-Plus 分页插件进行分页查询
		// 构建查询条件
		QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
		return userContactMapper.selectPage(page,null);
	}

	@Override
	public Integer add(UserContact bean) {
		return null;
	}

	@Override
	public Integer addBatch(List<UserContact> listBean) {
		return null;
	}

	@Override
	public Integer addOrUpdateBatch(List<UserContact> listBean) {
		return null;
	}

	@Override
	public Integer updateByParam(UserContact bean, UserContactQuery param) {
		return null;
	}

	@Override
	public Integer deleteByParam(UserContactQuery param) {
		return null;
	}

	@Override
	public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
		return userContactMapper.selectByUserIdAndContactId(userId,contactId);
	}

	@Override
	public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
		return null;
	}

	@Override
	public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
		return null;
	}

	@Override
	public UserContactSearchResultDto searchContact(String userId, String contactId) {
		UserContactTypeEnum typeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (typeEnum==null){
			return null;
		}
		UserContactSearchResultDto resultDto=new UserContactSearchResultDto();
		switch (typeEnum){
			case USER :
				UserInfo userInfo=this.userInfoMapper.selectByUserId(contactId);
				if (userInfo==null){
					return null;
				}
				resultDto = CopyTools.copy(userInfo,UserContactSearchResultDto.class);
				break;
			case GROUP:
				GroupInfo groupInfo=this.groupInfoMapper.selectByGroupId(contactId);
				if (groupInfo==null){
					return null;
				}
				resultDto.setNickName(groupInfo.getGroupName());
				break;
		}
		resultDto.setContactType(typeEnum.toString());
		resultDto.setContactId(contactId);
		if (userId.equals(contactId)){
			resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			return resultDto;
		}
		//查询是否是好友
		UserContact userContact=this.userContactMapper.selectByUserIdAndContactId(userId,contactId);
		resultDto.setStatus(userContact==null?null:userContact.getStatus());
		return resultDto;
	}

	@Override
	@Transactional
	public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {

		UserContactTypeEnum typeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (typeEnum==null){
			throw new BusinessException("申请失败，请不要修改uid");
		}
		// 申请人
		String applyUserId=tokenUserInfoDto.getUserId();
		// 默认申请消息

		applyInfo = StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE,tokenUserInfoDto.getNickName()):applyInfo;

		Long curTime=System.currentTimeMillis();
		Integer joinType=JoinTypeEnum.JOIN.getType();

		// 接收人
		String receiveUserId=contactId;

		//查询对方是否已经添加，拉黑无法添加
		UserContact userContact=this.userContactMapper.selectByUserIdAndContactId(applyUserId,contactId);
		if (userContact!=null&&
				ArrayUtils.contains(new Integer[]{
						UserContactStatusEnum.BLACKLIST_BE.getStatus(),
						UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus(),

						},
						userContact.getStatus())) {
			throw  new BusinessException("对方拉黑了你");
		}

		if (UserContactTypeEnum.GROUP==typeEnum){
			GroupInfo groupInfo=groupInfoMapper.selectByGroupId(contactId);
			if (groupInfo==null|| GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())){
				throw new BusinessException("群聊不存在");
			}
			// 如果进入到这里，说明receiveUserId的接收人是群，所以需要从群中能拿到群主ID复制给接收人，也就换了相等于接收者从现在群ID-》群主ID
			receiveUserId=groupInfo.getGroupOwnerId();
			joinType=groupInfo.getJoinType();

		}else {
			UserInfo userInfo=userInfoMapper.selectByUserId(receiveUserId);
			if (userInfo==null){
				throw  new BusinessException("不存在");
			}
			joinType=userInfo.getJoinType();
		}

		// 直接加入不用申请进入
		if (JoinTypeEnum.JOIN.getType().equals(joinType)){
			//TODO 添加联系人
			this.addContact(applyUserId,receiveUserId,contactId,typeEnum.getType(),applyInfo);
			return joinType;
		}

		UserContactApply dbApply=this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		if (dbApply==null){
			UserContactApply contactApply=new UserContactApply();
			contactApply.setContactType(typeEnum.getType());
			contactApply.setApplyUserId(applyUserId);
			contactApply.setReceiveUserId(receiveUserId);
			contactApply.setContactId(contactId);
			contactApply.setLastApplyTime(curTime);
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			contactApply.setApplyInfo(applyInfo);

			this.userContactApplyMapper.insert(contactApply);

		}else {
			// 更新状态
			UserContactApply contactApply=new UserContactApply();
			contactApply=dbApply;
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			contactApply.setLastApplyTime(curTime);
			contactApply.setApplyUserId(applyUserId);
			this.userContactApplyMapper.updateByApplyId(contactApply,dbApply.getApplyId());
		}

		if (dbApply==null||!UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())){
			// TODO 发送ws消息
			MessageSendDto messageSendDto=new MessageSendDto();
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
			messageSendDto.setMessageContent(applyInfo);
			messageSendDto.setContactId(receiveUserId);
			messageHandler.sendMessage(messageSendDto);

		}
		return joinType;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
		// 移除好友
		UserContact userContact=userContactMapper.selectByUserIdAndContactId(userId,contactId);
		userContact.setStatus(statusEnum.getStatus());

		userContact.setLastUpdateTime(new Date());
		userContactMapper.updateByUserIdAndContactId(userContact,userId,contactId);

		// 将好友中也移除自己
		UserContact friendContact=userContactMapper.selectByUserIdAndContactId(contactId,userId);
		if (UserContactStatusEnum.DEL==statusEnum){
			friendContact.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
			friendContact.setLastUpdateTime(new Date());
		}else if (UserContactStatusEnum.BLACKLIST==statusEnum){
			friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
			friendContact.setLastUpdateTime(new Date());
		}
		userContactMapper.updateByUserIdAndContactId(friendContact,contactId,userId);
		// TODO 从我的好友列表缓存中删除好友
		// TODO 从好友列表缓存中山拿出我

	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void addContact4Robot(String userId) {
		Date curDate=new Date();
		SysSettingDto sysSettingDto=new SysSettingDto();
		String contactId=sysSettingDto.getRobotUid();
		String contactName=sysSettingDto.getRobotNickName();
		String sendMessage=sysSettingDto.getRobotWelcome();
		sendMessage =StringTools.cleanHtmlTag(sendMessage);
		//添加机器人为好友
		UserContact userContact=new UserContact();
		userContact.setUserId(userId);
		userContact.setContactId(contactId);
		userContact.setContactType(UserContactTypeEnum.USER.getType());
		userContact.setCreateTime(curDate);
		userContact.setLastUpdateTime(curDate);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		userContactMapper.insert(userContact);
		//增加会话信息
		String sessionId=StringTools.getChatSessionId4User(new String[]{userId,contactId});
		ChatSession chatSession=new ChatSession();
		chatSession.setLastMessage(sendMessage);
		chatSession.setSessionId(sessionId);
		chatSession.setLastReceiveTime(curDate.getTime());
		this.chatSessionMapper.insert(chatSession);
		//增加会话人信息
		ChatSessionUser chatSessionUser=new ChatSessionUser();
		chatSessionUser.setUserId(userId);
		chatSessionUser.setContactId(contactId);
		chatSessionUser.setContactName(contactName);
		chatSessionUser.setSessionId(sessionId);
		this.chatSessionUserMapper.insert(chatSessionUser);

		//增加 聊天消息
		ChatMessage chatMessage=new ChatMessage();
		chatMessage.setSessionId(sessionId);
		chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
		chatMessage.setMessageContent(sendMessage);
		chatMessage.setSendUserId(contactId);
		chatMessage.setSendUserNickName(contactName);
		chatMessage.setSendTime(curDate.getTime());
		chatMessage.setContactId(userId);
		chatMessage.setContactType(UserContactTypeEnum.USER.getType());
		chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
		this.chatMessageMapper.insert(chatMessage);
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
		// 群聊人数
		if (UserContactTypeEnum.GROUP.getType().equals(contactType)){
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			QueryWrapper queryWrapper=new QueryWrapper(userContactQuery);
			Long count=userContactMapper.selectCount(queryWrapper);
			SysSettingDto sysSettingDto=redisComponent.getSysSetting();
			if (count>=sysSettingDto.getMaxGroupCount()){
				throw  new BusinessException("群已经满了，无法加入");
			}

		}
		// 好友
		Date curDate=new Date();
		// 同意，双方添加好友
		List<UserContact> contactList=new ArrayList<>();
		// 申请人添加对方
		UserContact userContact=new UserContact();
		userContact.setUserId(applyUserId);
		userContact.setContactId(contactId);
		userContact.setContactType(contactType);
		userContact.setCreateTime(curDate);
		userContact.setLastUpdateTime(curDate);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactList.add(userContact);
		//如果是申请好友，接收人添加申请人，如果是群 群主不用添加对方好友
		if (UserContactTypeEnum.USER.getType().equals(contactType)){
			userContact=new UserContact();
			userContact.setUserId(receiveUserId);
			userContact.setContactId(applyUserId);
			userContact.setContactType(contactType);
			userContact.setCreateTime(curDate);
			userContact.setLastUpdateTime(curDate);
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contactList.add(userContact);
		}

		// 批量插入
		userContactMapper.insertOrUpdateBatch(contactList);
		//TODO 如果是好友，接收人也添加申请人为好友 添加缓存

		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			redisComponent.addUserContact(receiveUserId,contactId);

		}

		redisComponent.addUserContact(applyUserId,contactId);

		//创建会话
		String sessionId=null;
		if (UserContactTypeEnum.USER.getType().equals(contactType)){
			sessionId=StringTools.getChatSessionId4User(new String[]{applyUserId,contactId});

		}else {
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}

		List<ChatSessionUser> chatSessionUserList=new ArrayList<>();
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			// 创建会话
			ChatSession chatSession=new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(applyInfo);
			chatSession.setLastReceiveTime(curDate.getTime());
			// 插入或者删除
			this.chatSessionMapper.insert(chatSession);
			//申请人session
			ChatSessionUser applySessionUser=new ChatSessionUser();
			applySessionUser.setUserId(applyUserId);
			applySessionUser.setContactId(contactId);
			applySessionUser.setSessionId(sessionId);



			UserInfo contactUser=this.userInfoMapper.selectByUserId(contactId);
			applySessionUser.setContactName(contactUser.getNickName());
			chatSessionUserList.add(applySessionUser);


			//接收人session
			ChatSessionUser contactSessionUser=new ChatSessionUser();
			contactSessionUser.setUserId(contactId);
			contactSessionUser.setContactId(applyUserId);
			contactSessionUser.setSessionId(sessionId);
			contactSessionUser.setLastReceiveTime(curDate.getTime());
			contactSessionUser.setLastMessage(applyInfo);



			UserInfo applyUserInfo=this.userInfoMapper.selectByUserId(applyUserId);
			contactSessionUser.setContactName(applyUserInfo.getNickName());

			chatSessionUserList.add(contactSessionUser);
			// 添加和修改
			this.chatSessionUserMapper.insert(contactSessionUser);
			//记录消息
			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
			chatMessage.setMessageContent(applyInfo);
			chatMessage.setSendUserId(applyUserId);

			chatMessage.setSendUserNickName(applyUserInfo.getNickName());
			chatMessage.setSendTime(curDate.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.USER.getType());
			chatMessageMapper.insert(chatMessage);

			MessageSendDto messageSendDto=CopyTools.copy(chatMessage,MessageSendDto.class);
			// 发送给接收好友申请的人
			messageHandler.sendMessage(messageSendDto);

			// 发送给申请人，发送人就是接收人，联系人就是申请人
			messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
			messageSendDto.setContactId(applyUserId);
			messageSendDto.setExtendData(contactUser);

			messageHandler.sendMessage(messageSendDto);
		}else {
			// 申请人加入群组
			ChatSessionUser chatSessionUser=new ChatSessionUser();
			chatSessionUser.setUserId(applyUserId);
			chatSessionUser.setContactId(contactId);

			GroupInfo groupInfo=this.groupInfoMapper.selectByGroupId(contactId);
			chatSessionUser.setContactId(groupInfo.getGroupId());
			chatSessionUser.setSessionId(sessionId);
			this.chatSessionUserMapper.insert(chatSessionUser);
			//

			UserInfo applyUserInfo=this.userInfoMapper.selectByUserId(applyUserId);
			String sendMessage=String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(),applyUserInfo.getNickName());
			//增加session信息
			ChatSession chatSession=new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastReceiveTime(curDate.getTime());
			chatSession.setLastMessage(sendMessage);
			this.chatSessionMapper.insert(chatSession);
			//增加聊天消息
			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
			chatMessage.setMessageContent(sendMessage);
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
			chatSessionMapper.insert(chatMessage);

			// 将群组添加到联系人
			redisComponent.addUserContact(applyUserId,groupInfo.getGroupId());
			// 将联系人通道添加到群组通道
			//TODO 待完善
			//channelContextUtils.addUser2Group();

			//发送群消息
			MessageSendDto messageSendDto=CopyTools.copy(chatMessage,MessageSendDto.class);
			messageSendDto.setContactId(contactId);
			//获取群人数
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer memberCount= Math.toIntExact(this.userContactMapper.selectCount(userContactQuery));
			messageSendDto.setMemberCount(memberCount);
			messageSendDto.setContactName(groupInfo.getGroupName());
			//发消息
			messageHandler.sendMessage(messageSendDto);
		}

	}




}