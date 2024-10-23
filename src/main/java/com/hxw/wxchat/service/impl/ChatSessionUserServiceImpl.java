package com.hxw.wxchat.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.PushBuilder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.enums.MessageTypeEnum;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.query.UserContactQuery;
import com.hxw.wxchat.mappers.UserContactMapper;
import com.hxw.wxchat.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.query.ChatSessionUserQuery;
import com.hxw.wxchat.entity.po.ChatSessionUser;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.query.SimplePage;
import com.hxw.wxchat.mappers.ChatSessionUserMapper;
import com.hxw.wxchat.service.ChatSessionUserService;
import com.hxw.wxchat.utils.StringTools;


/**
 * 会话用户 业务接口实现
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

	@Resource
	private ChatSessionUserMapper chatSessionUserMapper;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private UserContactMapper userContactMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSessionUser> findListByParam(ChatSessionUserQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return this.chatSessionUserMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatSessionUserQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.chatSessionUserMapper.selectCount(queryWrapper));
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatSessionUser> list = this.findListByParam(param);
		PaginationResultVO<ChatSessionUser> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatSessionUser bean) {
		return this.chatSessionUserMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatSessionUser> listBean) {
		return null;
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
		return null;
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatSessionUser bean, ChatSessionUserQuery param) {
		return null;
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatSessionUserQuery param) {
		return null;
	}

	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Override
	public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId修改
	 */
	@Override
	public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
		return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
	}
	@Override
	public void updateRedundanceInfo(String contactName,String contactId){

		ChatSessionUser updateInfo=new ChatSessionUser();
		updateInfo.setContactName(contactName);
		ChatSessionUserQuery chatSessionUserQuery=new ChatSessionUserQuery();
		chatSessionUserQuery.setContactId(contactId);
		this.chatSessionUserMapper.update(updateInfo,new QueryWrapper(chatSessionUserQuery));

		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if (contactTypeEnum==UserContactTypeEnum.GROUP){
			MessageSendDto messageSendDto=new MessageSendDto();
			messageSendDto.setContactType(UserContactTypeEnum.getByPrefix(contactId).getType());
			messageSendDto.setContactId(contactId);
			messageSendDto.setExtendData(contactName);
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
			messageHandler.sendMessage(messageSendDto);
		}else {
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			List<UserContact> userContactList=userContactMapper.selectList(userContactQuery);
			for (UserContact userContact:userContactList){
				MessageSendDto messageSendDto=new MessageSendDto();
				messageSendDto.setContactType(contactTypeEnum.getType());
				messageSendDto.setContactId(userContact.getUserId());
				messageSendDto.setExtendData(contactName);
				messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
				messageSendDto.setSendUserId(contactId);
				messageSendDto.setSendUserNickName(contactName);
				messageHandler.sendMessage(messageSendDto);
			}
		}


	}
}