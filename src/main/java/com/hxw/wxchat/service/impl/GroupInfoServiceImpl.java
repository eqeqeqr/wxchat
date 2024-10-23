package com.hxw.wxchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxw.wxchat.entity.config.AppConfig;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.SysSettingDto;
import com.hxw.wxchat.entity.enums.*;
import com.hxw.wxchat.entity.po.*;
import com.hxw.wxchat.entity.query.*;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.*;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.service.ChatSessionUserService;
import com.hxw.wxchat.service.GroupInfoService;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.utils.StringTools;
import com.hxw.wxchat.websocket.ChannelContextUtils;
import com.hxw.wxchat.websocket.MessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.util.EnumUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.List;


/**
 * 群组 业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

	@Resource
	private GroupInfoMapper groupInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserContactMapper userContactMapper;


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
	@Resource
	private AppConfig appConfig;
	@Resource
	private ChatSessionUserService chatSessionUserService;
	@Override
	public List<GroupInfo> findListByParam(GroupInfoQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return this.groupInfoMapper.selectList(param);
	}

	@Override
	public Integer findCountByParam(GroupInfoQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.groupInfoMapper.selectCount(queryWrapper));
	}

	@Override
	public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {

		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<GroupInfo> list = this.findListByParam(param);
		PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	@Override
	public Integer add(GroupInfo bean) {
		return null;
	}

	@Override
	public Integer addBatch(List<GroupInfo> listBean) {
		return null;
	}

	@Override
	public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
		return null;
	}

	@Override
	public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
		return null;
	}

	@Override
	public Integer deleteByParam(GroupInfoQuery param) {
		return null;
	}

	@Override
	public GroupInfo getGroupInfoByGroupId(String groupId) {
		return groupInfoMapper.selectByGroupId(groupId);
	}

	@Override
	public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
		return null;
	}

	@Override
	public Integer deleteGroupInfoByGroupId(String groupId) {
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		Date curDate=new Date();
		// 新增
		if (StringTools.isEmpty(groupInfo.getGroupId())){
			GroupInfoQuery groupInfoQuery=new GroupInfoQuery();
			groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
			//
			QueryWrapper queryWrapper=new QueryWrapper(groupInfoQuery);
			//
			Long count=this.groupInfoMapper.selectCount(queryWrapper);
			SysSettingDto sysSettingDto=redisComponent.getSysSetting();

			if (count>=sysSettingDto.getMaxGroupCount()){
				throw new BusinessException("最多只能创建"+sysSettingDto.getMaxGroupCount()+"群聊");
			}
			if (avatarFile==null){
				throw new BusinessException("saveGroup + 错误");
			}

			groupInfo.setCreateTime(curDate);
			groupInfo.setGroupId(StringTools.getGroupId());

			this.groupInfoMapper.insert(groupInfo);

			//将群组添加添加为联系人
			UserContact userContact=new UserContact();
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			userContact.setContactType(UserContactTypeEnum.GROUP.getType());
			userContact.setContactId(groupInfo.getGroupId());
			userContact.setUserId(groupInfo.getGroupOwnerId());
			userContact.setCreateTime(curDate);
			userContact.setLastUpdateTime(curDate);
			userContactMapper.insert(userContact);

			//TODO 创建会话
			String sessionId=StringTools.getChatSessionId4Group(groupInfo.getGroupId());
			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatMessage.setLastReceiveTime(curDate.getTime());
			this.chatSessionMapper.insert(chatMessage);

			ChatSessionUser chatSessionUser=new ChatSessionUser();
			chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
			chatSessionUser.setContactId(groupInfo.getGroupId());
			chatSessionUser.setContactName(groupInfo.getGroupName());
			chatSessionUser.setSessionId(sessionId);
			this.chatSessionMapper.insert(chatSessionUser);





			ChatMessage chatMessage1=new ChatMessage();
			chatMessage1.setSessionId(sessionId);
			chatMessage1.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
			chatMessage1.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatMessage1.setSendTime(curDate.getTime());
			chatMessage1.setContactId(groupInfo.getGroupId());
			chatMessage1.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage1.setStatus(MessageStatusEnum.SENDED.getStatus());


			chatMessageMapper.insert(chatMessage1);
			// 将群组添加到联系人
			redisComponent.addUserContact(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());
			// 将联系人通道添加到群组通道
			//TODO 待完善
			//channelContextUtils.addUser2Group();

			chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatSessionUser.setLastReceiveTime(curDate.getTime());
			//获取群人数
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setContactId(groupInfo.getGroupId());
			userContactQuery.setMemberCount(1);

			MessageSendDto messageSendDto= CopyTools.copy(chatMessage1, MessageSendDto.class);
			messageSendDto.setExtendData(chatSessionUser);
			messageSendDto.setLastMessage(chatSessionUser.getLastMessage());

			messageHandler.sendMessage(messageSendDto);


		}else {
			// 修改
			GroupInfo dbInfo=this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
			if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
				throw  new BusinessException("不是群组，无权修改");
			}
			this.groupInfoMapper.updateByGroupId(groupInfo,groupInfo.getGroupId());

			// 更新相关表冗余信息
		String contactNameUpdate=null;
		if (!dbInfo.getGroupName().equals(groupInfo.getGroupName())){
			contactNameUpdate =groupInfo.getGroupName();
		}
		if (contactNameUpdate==null){
			return;
		}
		chatSessionUserService.updateRedundanceInfo(contactNameUpdate,groupInfo.getGroupId());

			// 修改群内昵称发送ws消息



		}
		if (avatarFile== null){
			return;
		}
		String baseFolder=appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE;
		File targetFileFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
		if (!targetFileFolder.exists()){
			System.out.println("找不到目录，正在创建");
			targetFileFolder.mkdirs();
		}
		String filePath=targetFileFolder.getPath()+"/"+groupInfo.getGroupId();

		avatarFile.transferTo(new File(filePath+Constants.IMAGE_SUFFIX));

		avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));

	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void dissolutionGroup(String groupOwnerId, String groupId) {
		GroupInfo dbInfo=this.groupInfoMapper.selectByGroupId(groupId);
		if (dbInfo==null||!dbInfo.getGroupOwnerId().equals(groupOwnerId)){
			throw new BusinessException("错误 群主ID 无法解散");
		}
		// 删除群
		GroupInfo updateInfo=this.groupInfoMapper.selectByGroupId(groupId);
		updateInfo.setStatus(GroupStatusEnum.DISSOLUTION.getStatus());
		this.groupInfoMapper.updateByGroupId(updateInfo,groupId);

		// 更新联系人信息
		UserContactQuery userContactQuery=new UserContactQuery();
		userContactQuery.setContactId(groupId);
		userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

		UserContact updateUserContact=new UserContact();
		updateUserContact.setStatus(UserContactStatusEnum.DEL.getStatus());
		QueryWrapper queryWrapper=new QueryWrapper(userContactQuery);
		this.userContactMapper.update(updateUserContact,queryWrapper);

		// TODO 移动相关群员的联系人缓存

		// TODO 更新 1.更新会话信息 2。记录群消息 3.发送解散通知消息


	}
}