package com.hxw.wxchat.service;

import com.hxw.wxchat.entity.po.UserInfoBeauty;
import com.hxw.wxchat.entity.query.UserInfoBeautyQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;


/**
 * 靓号表 业务接口
 */
public interface UserInfoBeautyService {

	/**
	 * 根据条件查询列表
	 */
	List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserInfoBeautyQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery param);

	/**
	 * 新增
	 */
	Integer add(UserInfoBeauty bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserInfoBeauty> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserInfoBeauty> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserInfoBeauty bean,UserInfoBeautyQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserInfoBeautyQuery param);

	/**
	 * 根据IdAndUserId查询对象
	 */
	UserInfoBeauty getUserInfoBeautyByIdAndUserId(Integer id,String userId);


	/**
	 * 根据IdAndUserId修改
	 */
	Integer updateUserInfoBeautyByIdAndUserId(UserInfoBeauty bean,Integer id,String userId);


	/**
	 * 根据IdAndUserId删除
	 */
	Integer deleteUserInfoBeautyByIdAndUserId(Integer id,String userId);


	/**
	 * 根据UserId查询对象
	 */
	UserInfoBeauty getUserInfoBeautyByUserId(String userId);


	/**
	 * 根据UserId修改
	 */
	Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean,String userId);


	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoBeautyByUserId(String userId);
	/**
	 * 根据Id删除
	 */
	Integer deleteUserInfoBeautyById(Integer id);


	/**
	 * 根据Email查询对象
	 */
	UserInfoBeauty getUserInfoBeautyByEmail(String email);


	/**
	 * 根据Email修改
	 */
	Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean,String email);


	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoBeautyByEmail(String email);
	/**
	 * 增加靓号
	 */
	void saveCount(UserInfoBeauty userInfoBeauty);
}