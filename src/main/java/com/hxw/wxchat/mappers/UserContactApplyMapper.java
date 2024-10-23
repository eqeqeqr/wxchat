package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.po.UserContactApply;
import com.hxw.wxchat.entity.query.UserContactApplyQuery;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 联系人申请 数据库操作接口
 */
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

	/**
	 * 根据ApplyId更新
	 */
	@Update("UPDATE user_contact_apply SET apply_user_id = #{bean.applyUserId}, receive_user_id = #{bean.receiveUserId}, contact_id = #{bean.contactId}, last_apply_time = #{bean.lastApplyTime}, contact_type = #{bean.contactType}, status = #{bean.status}, apply_info = #{bean.applyInfo} WHERE apply_id = #{applyId}")
	 Integer updateByApplyId(@Param("bean") UserContactApply userContactApply,@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyId删除
	 */
	@Delete("DELETE FROM user_contact_apply WHERE apply_id = #{applyId}")
	 Integer deleteByApplyId(@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyId获取对象
	 */
	@Select("SELECT * FROM user_contact_apply WHERE apply_id = #{applyId}")
	UserContactApply selectByApplyId(@Param("applyId") Integer applyId);


	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId更新
	 */
	@Update("UPDATE user_contact_apply SET last_apply_time = #{bean.lastApplyTime}, contact_type = #{bean.contactType}, status = #{bean.status}, apply_info = #{bean.applyInfo} WHERE apply_user_id = #{applyUserId} AND receive_user_id = #{receiveUserId} AND contact_id = #{contactId}")
	 Integer updateByApplyUserIdAndReceiveUserIdAndContactId(@Param("bean") UserContactApply userContactApply,@Param("applyUserId") String applyUserId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);


	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	@Delete("DELETE FROM user_contact_apply WHERE apply_user_id = #{applyUserId} AND receive_user_id = #{receiveUserId} AND contact_id = #{contactId}")
	 Integer deleteByApplyUserIdAndReceiveUserIdAndContactId(@Param("applyUserId") String applyUserId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);


	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId获取对象
	 */
	@Select("SELECT * FROM user_contact_apply WHERE apply_user_id = #{applyUserId} AND receive_user_id = #{receiveUserId} AND contact_id = #{contactId}")
	UserContactApply selectByApplyUserIdAndReceiveUserIdAndContactId(@Param("applyUserId") String applyUserId,@Param("receiveUserId") String receiveUserId,@Param("contactId") String contactId);
	/**
	 * 查询申请的名称
	 */
	@Select("SELECT a.*, " +
			"CASE " +
			"WHEN a.contact_type = 0 THEN u.nick_name " +
			"WHEN a.contact_type = 1 THEN g.group_name " +
			"END AS contact_name " +
			"FROM user_contact_apply a " +
			"LEFT JOIN user_info u ON u.user_id = a.apply_user_id " +
			"LEFT JOIN group_info g ON g.group_id = a.contact_id " +
			"WHERE a.receive_user_id = #{userId}")
	@Results(
			{@Result (property = "contactName",column = "contact_name")}
	)
	List<UserContactApply> findListByParams(@Param("userId") String userId);

	Integer selectCount(@Param("query") UserContactApplyQuery userContactApplyQuery);
	@Override
	int insert(@Param("bean") UserContactApply entity);
}
