package com.hxw.wxchat.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.dto.UserContactSearchResultDto;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.query.UserContactQuery;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.vo.PaginationResultVO;


/**
 * 联系人 业务接口
 */
public interface UserContactService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContact> findListByParam(UserContactQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserContactQuery param);

	/**
	 * 分页查询
	 */
	IPage<UserContact> findListByPage(Page<UserContact> page);

	/**
	 * 新增
	 */
	Integer add(UserContact bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserContact> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserContact> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserContact bean,UserContactQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserContactQuery param);

	/**
	 * 根据UserIdAndContactId查询对象
	 */
	UserContact getUserContactByUserIdAndContactId(String userId,String contactId);


	/**
	 * 根据UserIdAndContactId修改
	 */
	Integer updateUserContactByUserIdAndContactId(UserContact bean,String userId,String contactId);


	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteUserContactByUserIdAndContactId(String userId,String contactId);

	/**
	 * 搜索
	 */
	UserContactSearchResultDto searchContact(String userId, String contactId);

	/**
	 * 申请添加
	 */
	Integer applyAdd(TokenUserInfoDto tokenUserInfoDto,String contactId,String applyInfo);
	/**
	 * 删除联系人
	 */
	void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);
	/**
	 * 添加联系人
	 */
	void addContact(String applyUserId,String receiveUserId,String contactId,Integer contactType,String applyInfo);
	/**
	 *  添加机器人
	 */

	void  addContact4Robot(String userId);
}