package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.wxchat.entity.po.UserInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户信息表 数据库操作接口
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {

	/**
	 * 根据UserId更新
	 */
	@Update("UPDATE user_info SET nick_name = #{bean.nickName}, last_login_time=#{bean.lastLoginTime},password=#{bean.password},sex = #{bean.sex},personal_signature=#{bean.personalSignature} WHERE user_id = #{userId}")
	 Integer updateByUserId(@Param("bean") UserInfo userInfo,@Param("userId") String userId);

	/**
	 * 根据UserId删除
	 */
	@Delete("DELETE FROM user_info WHERE user_id = #{userId}")
	Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * 根据UserId获取对象
	 */
	@Select("SELECT * FROM user_info WHERE user_id = #{userId}")
	 UserInfo selectByUserId(@Param("userId") String userId);


	/**
	 * 根据Email更新
	 */
	@Update("UPDATE user_info SET user_name = #{bean.userName}, gender = #{bean.gender}, age = #{bean.age} WHERE email = #{email}")
	 Integer updateByEmail(@Param("bean") UserInfo userInfo,@Param("email") String email);


	/**
	 * 根据Email删除
	 */
	@Delete("DELETE FROM user_info WHERE email = #{email}")
	 Integer deleteByEmail(@Param("email") String email);


	/**
	 * 根据Email获取对象
	 */
	@Select("SELECT * FROM user_info WHERE email = #{email}")
	 UserInfo selectByEmail(@Param("email") String email);

}
