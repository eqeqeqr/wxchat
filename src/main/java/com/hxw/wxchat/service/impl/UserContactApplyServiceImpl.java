package com.hxw.wxchat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxw.wxchat.entity.dto.SysSettingDto;
import com.hxw.wxchat.entity.enums.UserContactApplyStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.query.UserContactQuery;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.UserContactMapper;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.service.UserContactService;
import org.springframework.stereotype.Service;

import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.query.UserContactApplyQuery;
import com.hxw.wxchat.entity.po.UserContactApply;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.query.SimplePage;
import com.hxw.wxchat.mappers.UserContactApplyMapper;
import com.hxw.wxchat.service.UserContactApplyService;
import com.hxw.wxchat.utils.StringTools;


/**
 * 联系人申请 业务接口实现
 */
@Service("userContactApplyService")
@Transactional
public class UserContactApplyServiceImpl implements UserContactApplyService {

	@Resource
	private UserContactApplyMapper userContactApplyMapper;

	@Resource
	private UserContactMapper userContactMapper;
	@Resource
	private UserContactService userContactService;

	@Resource
	private RedisComponent redisComponent;
	@Override
	public List<UserContactApply> findListByParam(UserContactApplyQuery param) {

		QueryWrapper queryWrapper=new QueryWrapper(param);
		if (param.getQueryContactInfo()){
			return this.userContactApplyMapper.findListByParams(param.getReceiveUserId()) ;
		}
		return this.userContactApplyMapper.selectList(queryWrapper);
	}

	@Override
	public Integer findCountByParam(UserContactApplyQuery param) {

		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.userContactApplyMapper.selectCount(queryWrapper));
	}

	@Override
	public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery param) {

		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserContactApply> list = this.findListByParam(param);
		PaginationResultVO<UserContactApply> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	@Override
	public Integer add(UserContactApply bean) {
		return null;
	}

	@Override
	public Integer addBatch(List<UserContactApply> listBean) {
		return null;
	}

	@Override
	public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
		return null;
	}

	@Override
	public Integer updateByParam(UserContactApply bean, UserContactApplyQuery param) {
		return null;
	}

	@Override
	public Integer deleteByParam(UserContactApplyQuery param) {
		return null;
	}

	@Override
	public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
		return null;
	}

	@Override
	public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
		return null;
	}

	@Override
	public Integer deleteUserContactApplyByApplyId(Integer applyId) {
		return null;
	}

	@Override
	public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return null;
	}

	@Override
	public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
		return null;
	}

	@Override
	public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
		return null;
	}

	@Override
	@Transactional(rollbackOn = Exception.class)
	public void dealWithApply(String userId, Integer applyId, Integer status) {
		UserContactApplyStatusEnum statusEnum=UserContactApplyStatusEnum.getByStatus(status);
		if (statusEnum==null||UserContactApplyStatusEnum.INIT==statusEnum){
			throw new BusinessException("参数错误");
		}
		UserContactApply applyInfo =this.userContactApplyMapper.selectByApplyId(applyId);


		if (applyInfo==null||!userId.equals(applyInfo.getReceiveUserId())){
			throw  new BusinessException("需要是本人接收处理");
		}
		UserContactApply updateInfo=new UserContactApply();
		updateInfo.setStatus(statusEnum.getStatus());
		updateInfo.setLastApplyTime(System.currentTimeMillis());

		UserContactApplyQuery applyQuery=new UserContactApplyQuery();
		applyQuery.setApplyId(applyId);
		applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
		QueryWrapper queryWrapper=new QueryWrapper(applyQuery);
		Integer count =userContactApplyMapper.update( updateInfo,queryWrapper);

		if (count==0){
			throw new BusinessException("修改申请错误");
		}

		if (UserContactApplyStatusEnum.PASS.getStatus().equals(status)){
			// TODO 添加联系人
			this.userContactService.addContact(applyInfo.getApplyUserId(),applyInfo.getReceiveUserId(),applyInfo.getContactId(),applyInfo.getContactType(),applyInfo.getApplyInfo());
			return;
		}

		if (UserContactApplyStatusEnum.BLACKLIST==statusEnum){
			Date curDate=new Date();

			UserContact userContact=new UserContact();
			userContact.setUserId(applyInfo.getApplyUserId());
			userContact.setContactId(applyInfo.getContactId());
			userContact.setContactType(applyInfo.getContactType());
			userContact.setCreateTime(curDate);
			userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
			userContact.setLastUpdateTime(curDate);

			userContactMapper.insertOrUpdate(userContact);

		}
	}

}