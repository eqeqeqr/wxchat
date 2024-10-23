package com.hxw.wxchat.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;


import com.hxw.wxchat.entity.enums.*;
import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.query.GroupInfoQuery;
import com.hxw.wxchat.entity.query.UserContactQuery;
import com.hxw.wxchat.entity.vo.GroupInfoVO;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.service.GroupInfoService;

import com.hxw.wxchat.service.UserContactService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 群组 Controller
 */
@RestController("groupInfoController")
@RequestMapping("/groupInfo")
public class GroupInfoController extends ABaseController{

	@Autowired
	private GroupInfoService groupInfoService;

	@Autowired
	private UserContactService userContactService;
	@RequestMapping("/saveGroup")
	@GlobalInterceptor
	public ResultResponse saveGroup(HttpServletRequest request, String groupId,
									@NotEmpty String groupName,
									String groupNotice,
									@NotNull Integer joinType,
									MultipartFile avatarFile,
									MultipartFile avatarCover

									) throws IOException {
		System.out.println(">>>>>>"+request);
		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
		GroupInfo groupInfo=new GroupInfo();
		groupInfo.setGroupId(groupId);
		groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfo.setGroupName(groupName);
		groupInfo.setGroupNotice(groupNotice);
		groupInfo.setJoinType(joinType);
		groupInfo.setStatus(JoinTypeEnum.APPLY.getType());

		this.groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);


		return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群组保存成功",tokenUserInfoDto);

	}

	@RequestMapping("/loginMyGroup")
	@GlobalInterceptor
	public ResultResponse loginMyGroup(HttpServletRequest request) throws IOException {
		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
		GroupInfoQuery groupInfoQuery=new GroupInfoQuery();
		groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfoQuery.setOrderBy("create_time desc");
		List<GroupInfo> groupInfoList=this.groupInfoService.findListByParam(groupInfoQuery);
		return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群组显示成功",groupInfoList);

	}

	@RequestMapping("/getGroupInfo")
	@GlobalInterceptor
	public ResultResponse getGroupInfo(HttpServletRequest request,
									   @NotEmpty String groupId

										) throws IOException {
		GroupInfo groupInfo=getGroupDetailCommon(request,groupId);
		UserContactQuery  userContactQuery=new UserContactQuery();
		userContactQuery.setContactId(groupId);
		Integer memberCount =this.userContactService.findCountByParam(userContactQuery);
		groupInfo.setMemberCount(memberCount);
		return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群组详情显示成功",groupInfo);

	}
	private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) throws JsonProcessingException {
		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
		UserContact userContact=this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),groupId);
		if (userContact==null||!UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
			throw  new BusinessException("你不在群聊或者群聊不存在");
		}
		if (!UserContactTypeEnum.GROUP.getType().equals(userContact.getContactType())){
			throw  new BusinessException("不是群关系");
		}
		GroupInfo groupInfo=this.groupInfoService.getGroupInfoByGroupId(groupId);
		if (groupInfo==null||GroupStatusEnum.NORMAL.equals(groupInfo.getStatus())){
			throw  new BusinessException("群聊解散或者不存在");
		}
		return groupInfo;
	}
	@RequestMapping("/getGroupInfo4Chat")
	@GlobalInterceptor
	public ResultResponse getGroupInfo4Chat(HttpServletRequest request,
									   @NotEmpty String groupId
	) throws JsonProcessingException {
		GroupInfo groupInfo=getGroupDetailCommon(request,groupId);

		UserContactQuery userContactQuery=new UserContactQuery();
		userContactQuery.setContactId(groupId);
		userContactQuery.setQueryUserInfo(true);
		userContactQuery.setOrderBy("create_time asc");
		userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		List<UserContact> userContacts=this.userContactService.findListByParam(userContactQuery);



		GroupInfoVO groupInfoVO=new GroupInfoVO();
		groupInfoVO.setGroupInfo(groupInfo);
		groupInfoVO.setUserContactsList(userContacts);

		return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群组详情显示成功",groupInfoVO);

	}
}