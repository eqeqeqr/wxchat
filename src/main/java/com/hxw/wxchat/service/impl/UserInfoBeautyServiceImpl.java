package com.hxw.wxchat.service.impl;

import com.hxw.wxchat.entity.enums.BeautyAccountStatusEnum;
import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.po.UserInfoBeauty;
import com.hxw.wxchat.entity.query.SimplePage;
import com.hxw.wxchat.entity.query.UserInfoBeautyQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.mappers.UserInfoBeautyMapper;
import com.hxw.wxchat.mappers.UserInfoMapper;
import com.hxw.wxchat.service.UserInfoBeautyService;
import com.hxw.wxchat.utils.StringTools;
import org.apache.commons.lang3.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * 靓号表 业务接口实现
 */
@Service("userInfoBeautyService")
public class UserInfoBeautyServiceImpl implements UserInfoBeautyService {
	@Resource
	private UserInfoBeautyMapper userInfoBeautyMapper;
	@Resource
	private UserInfoMapper userInfoMapper;

	@Override
	public List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery param) {
		return null;
	}

	@Override
	public Integer findCountByParam(UserInfoBeautyQuery param) {
		return null;
	}

	@Override
	public PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery param) {
		return null;
	}

	@Override
	public Integer add(UserInfoBeauty bean) {
		return null;
	}

	@Override
	public Integer addBatch(List<UserInfoBeauty> listBean) {
		return null;
	}

	@Override
	public Integer addOrUpdateBatch(List<UserInfoBeauty> listBean) {
		return null;
	}

	@Override
	public Integer updateByParam(UserInfoBeauty bean, UserInfoBeautyQuery param) {
		return null;
	}

	@Override
	public Integer deleteByParam(UserInfoBeautyQuery param) {
		return null;
	}

	@Override
	public UserInfoBeauty getUserInfoBeautyByIdAndUserId(Integer id, String userId) {
		return null;
	}

	@Override
	public Integer updateUserInfoBeautyByIdAndUserId(UserInfoBeauty bean, Integer id, String userId) {
		return null;
	}

	@Override
	public Integer deleteUserInfoBeautyByIdAndUserId(Integer id, String userId) {
		return null;
	}

	@Override
	public UserInfoBeauty getUserInfoBeautyByUserId(String userId) {
		return null;
	}

	@Override
	public Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) {
		return null;
	}

	@Override
	public Integer deleteUserInfoBeautyByUserId(String userId) {
		return null;
	}

	@Override
	public Integer deleteUserInfoBeautyById(Integer id) {
		return userInfoBeautyMapper.deleteById(id);
	}

	@Override
	public UserInfoBeauty getUserInfoBeautyByEmail(String email) {
		return null;
	}

	@Override
	public Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) {
		return null;
	}

	@Override
	public Integer deleteUserInfoBeautyByEmail(String email) {
		return null;
	}

	@Override
	public void saveCount(UserInfoBeauty userInfoBeauty) {

		//userInfoBeauty.getId()!=null 是修改
		if (userInfoBeauty.getId()!=null){
			UserInfoBeauty dbInfo=this.userInfoBeautyMapper.selectById(userInfoBeauty.getId());
			if (BeautyAccountStatusEnum.USEED.getStatus().equals(dbInfo.getStatus())){
				System.out.println("----------------11111111");
				throw new BusinessException("参数错误，靓号添加失败");
			}
		}

		UserInfoBeauty dbInfo=this.userInfoBeautyMapper.selectByEmail(userInfoBeauty.getEmail());
		if (userInfoBeauty.getId()==null&&dbInfo!=null){
			System.out.println("----------------2222222222");
			throw new BusinessException("邮箱已经存在");
		}
		// 修改时判断你邮箱那个是否存在
		if (userInfoBeauty.getId()!=null&&dbInfo!= null&&dbInfo.getId()!=null&&!userInfoBeauty.getId().equals(dbInfo.getUserId())){
			System.out.println("----------------3333333");
			throw  new BusinessException("邮箱已经存在");
		}


		dbInfo=this.userInfoBeautyMapper.selectByUserId(userInfoBeauty.getUserId());
		if (userInfoBeauty.getId()==null&&dbInfo!=null){
			System.out.println("----------------444444444444444");
			throw  new BusinessException("靓号已经存在");
		}
		if (userInfoBeauty.getId()!=null&&dbInfo!= null&&dbInfo.getId()!=null&&!userInfoBeauty.getId().equals(dbInfo.getUserId())){
			System.out.println("----------------555555555555555");
			throw  new BusinessException("靓号已经存在");
		}


		//判断邮箱是否已经注册
		UserInfo userInfo=this.userInfoMapper.selectByEmail(userInfoBeauty.getEmail());
		if (userInfo!=null){
			System.out.println("----------------666666666666666");
			throw new BusinessException("邮箱已经注册");

		}
		 userInfo=this.userInfoMapper.selectByUserId(userInfoBeauty.getUserId());
		if (userInfo!=null){
			System.out.println("----------------7777777777777");
			throw new BusinessException("靓号已经注册");
		}

		if (userInfoBeauty.getId()!=null){
			this.userInfoBeautyMapper.updateById(userInfoBeauty);
		}else{
			userInfoBeauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());
			this.userInfoBeautyMapper.insert(userInfoBeauty);
		}

	}
}