package com.hxw.wxchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hxw.wxchat.entity.config.AppConfig;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.enums.*;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.po.UserContactApply;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.po.UserInfoBeauty;
import com.hxw.wxchat.entity.query.*;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.vo.UserInfoVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.UserContactMapper;
import com.hxw.wxchat.mappers.UserInfoBeautyMapper;
import com.hxw.wxchat.mappers.UserInfoMapper;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.service.ChatSessionUserService;
import com.hxw.wxchat.service.UserContactService;
import com.hxw.wxchat.service.UserInfoBeautyService;
import com.hxw.wxchat.service.UserInfoService;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.utils.StringTools;
import org.apache.catalina.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 用户信息表 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private UserInfoBeautyMapper userInfoBeautyMapper;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private RedisComponent redisComponent;
	@Autowired
	private UserContactMapper userContactMapper;
	@Autowired
	private UserContactService userContactService;
	@Autowired
	private ChatSessionUserService chatSessionUserService;
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);

		return this.userInfoMapper.selectList(queryWrapper);
	}

	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.userInfoMapper.selectCount(queryWrapper));
	}

	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {

		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}
	@Override
	public Integer add(UserInfo bean) {
		return null;
	}

	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		return null;
	}

	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		return null;
	}

	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		return null;
	}

	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		return null;
	}

	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return userInfoMapper.selectByUserId(userId);
	}

	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return userInfoMapper.updateByUserId(bean,userId);
	}

	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return null;
	}

	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return null;
	}

	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return null;
	}

	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void register(String email, String nickName, String password) {

		System.out.println(">>>>>>>>>>"+email+nickName+password);
		// 判断是否有该用户
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);

		if (null!=userInfo){
			throw  new BusinessException("邮箱账号已经存在");
		}

			String userId=StringTools.getUserId();
			UserInfoBeauty beautyAccount=this.userInfoBeautyMapper.selectByEmail(email);
			// 靓号不为空，且未使用
			Boolean useBeautyAccount=null!=beautyAccount&& BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());

			if (useBeautyAccount){
					// 拿到靓号
					userId=UserContactTypeEnum.USER.getPrefix()+beautyAccount.getUserId();

			}

			Date curDate =new Date();
			userInfo=new UserInfo();
			userInfo.setEmail(email);
			userInfo.setUserId(userId);
			userInfo.setNickName(nickName);
			userInfo.setPassword(StringTools.encodeMd5(password));
			userInfo.setCreateTime(curDate);
			userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
			userInfo.setLastOffTime(curDate.getTime());
			userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
			this.userInfoMapper.insert(userInfo);


			if (useBeautyAccount){
				UserInfoBeauty updateBeauty=new UserInfoBeauty();
				updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
				this.userInfoBeautyMapper.updateByUserId(updateBeauty,beautyAccount.getUserId());
			}
			//TODO 创建机器人好友
			userContactService.addContact4Robot(userId);


	}

	@Override
	public UserInfoVO login(String email, String password) throws JsonProcessingException {
		UserInfo userInfo=this.userInfoMapper.selectByEmail(email);
		System.out.println(userInfo+">>>>>>"+password);
		if (userInfo==null||!userInfo.getPassword().equals(StringTools.encodeMd5(password))){
			throw new BusinessException("账号密码错误");
		}
		if (UserStatusEnum.DISABLE.equals(userInfo.getStatus())){
			throw new BusinessException("账号已经禁用");
		}
		//TODO 查询我的联系人
		UserContactQuery contactQuery=new UserContactQuery();
		contactQuery.setUserId(userInfo.getUserId());
		contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		List<UserContact> contactList=userContactMapper.selectList(contactQuery);
		System.out.println("contactList>>>>>>>>>>>"+contactList);
		List<String> conntactIdList=contactList.stream().map(item->item.getContactId()).collect(Collectors.toList());
		redisComponent.cleanUserContact(userInfo.getUserId());
		if (!conntactIdList.isEmpty()){
			redisComponent.addUserContactBatch(userInfo.getUserId(), conntactIdList);
		}

		//TODO 查询我的群组



		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(userInfo);
		Long lastHeartBeat=redisComponent.getUserHeartBeat(userInfo.getUserId());
		if (lastHeartBeat!=null){
			throw new BusinessException("此账号已经在别处登入，请退出后登录");
		}

		//保存登录信息到redis中
		String token=StringTools.encodeMd5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_20));
		tokenUserInfoDto.setToken(token);
		tokenUserInfoDto.setNickName(userInfo.getNickName());
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

		UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);

		userInfoVO.setToken(tokenUserInfoDto.getToken());
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());

		return userInfoVO;


	}
	private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo){
		TokenUserInfoDto tokenUserInfoDto=new TokenUserInfoDto();
		tokenUserInfoDto.setUserId(userInfo.getUserId());
		tokenUserInfoDto.setNickName(userInfo.getNickName());
		String adminEmails=appConfig.getAdminEmails();
		if (!StringTools.isEmpty(adminEmails)&& ArrayUtils.contains(adminEmails.split(","),userInfo.getEmail())){
			tokenUserInfoDto.setAdmin(true);
		}else {
			tokenUserInfoDto.setAdmin(false);
		}
		return tokenUserInfoDto;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		if (avatarFile!=null){
			String baseFolder=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
			File targetFileFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
			if (!targetFileFolder.exists()){
				System.out.println("找不到目录，正在创建");
				targetFileFolder.mkdirs();
			}
			String filePath=targetFileFolder.getPath()+"/"+userInfo.getUserId();
			avatarFile.transferTo(new File(filePath+Constants.IMAGE_SUFFIX));

			avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
		}
		

		UserInfo dbInfo=this.userInfoMapper.selectByUserId(userInfo.getUserId());

		this.userInfoMapper.updateByUserId(userInfo,userInfo.getUserId());
		String contactNameUpdate=null;
		if (dbInfo.getNickName().equals(userInfo.getUserId())){
			contactNameUpdate=userInfo.getNickName();
		}
		// 更新会话信息中的昵称
		if (contactNameUpdate ==null){
			return;
		}
		//更新token中的昵称
		TokenUserInfoDto tokenUserInfoDto=redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
		tokenUserInfoDto.setNickName(contactNameUpdate);
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
		chatSessionUserService.updateRedundanceInfo(contactNameUpdate,userInfo.getUserId());
	}

	@Override
	public void updateUserStatus(Integer status, String userId) {
		UserStatusEnum userStatusEnum=UserStatusEnum.getByStatus(status);
		if (userStatusEnum==null){
			throw new BusinessException("修改用户状态的错误，");
		}
		UserInfo userInfo=userInfoMapper.selectByUserId(userId);
		userInfo.setStatus(userStatusEnum.getStatus());
		this.userInfoMapper.updateByUserId(userInfo,userId);
	}

	@Override
	public void forceOffLine(String userId) {
		// TODO 强制下线
	}
}

