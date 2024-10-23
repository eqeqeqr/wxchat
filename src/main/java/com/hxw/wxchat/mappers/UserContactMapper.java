package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.query.UserContactQuery;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 联系人 数据库操作接口
 */
@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {

	/**
	 * 根据UserIdAndContactId更新
	 */
	@Update("UPDATE user_contact SET contact_type = #{bean.contactType}, create_time = #{bean.createTime}, status = #{bean.status}, last_update_time = #{bean.lastUpdateTime} WHERE user_id = #{userId} AND contact_id = #{contactId}")
	 Integer updateByUserIdAndContactId(@Param("bean") UserContact userContact,@Param("userId") String userId,@Param("contactId") String contactId);


	/**
	 * 根据UserIdAndContactId删除
	 */
	@Delete("DELETE FROM user_contact WHERE user_id = #{userId} AND contact_id = #{contactId}")
	 Integer deleteByUserIdAndContactId(@Param("userId") String userId,@Param("contactId") String contactId);


	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Select("SELECT * FROM user_contact WHERE user_id = #{userId} AND contact_id = #{contactId}")
	UserContact selectByUserIdAndContactId(@Param("userId") String userId,@Param("contactId") String contactId);
	@Select("SELECT c.*,u.nick_name ,u.sex FROM `user_contact` c INNER JOIN user_info u on u.user_id=c.user_id  WHERE contact_id=#{contactId}")
	@Results({
			@Result(property = "contactName", column = "nick_name"),
	})
	List<UserContact> findListByParams (@Param("contactId") String contactId);
	@Select("SELECT * FROM user_contact WHERE contact_id = #{contactId}")
	UserContact selectByContactId(@Param("contactId") String contactId);

	/**
	 * 插入如果主键冲突就改成修改
	 */
	@Insert("INSERT INTO user_contact (user_id, contact_id, contact_type, create_time, status, last_update_time) " +
			"VALUES (#{bean.userId}, #{bean.contactId}, #{bean.contactType}, #{bean.createTime}, #{bean.status}, #{bean.lastUpdateTime}) " +
			"ON DUPLICATE KEY UPDATE " +
			"status = #{bean.status}, " +
			"last_update_time = #{bean.lastUpdateTime};")
	Integer insertOrUpdate(@Param("bean") UserContact userContact);

	/**
	 * 批量插入或者修改
	 * 使用xml方式
	 */
	Integer insertOrUpdateBatch(List<UserContact> list);


	List<UserContact> selectList(@Param("query") UserContactQuery query);
	Long selectCount(@Param("query") UserContactQuery userContactQuery);
}
