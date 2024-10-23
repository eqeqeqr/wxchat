package com.hxw.wxchat.mappers;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.wxchat.entity.po.UserInfoBeauty;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 靓号表 数据库操作接口
 */
public interface UserInfoBeautyMapper extends BaseMapper<UserInfoBeauty> {

	/**
	 * 根据IdAndUserId更新
	 *
	 * 次方法有问题 字段没有对
	 */
	@Update("UPDATE user_info_beauty SET= #{bean.nickName}, gender = #{bean.gender}, age = #{bean.age} WHERE id = #{id} AND user_id = #{userId}")
	 Integer updateByIdAndUserId(@Param("bean")UserInfoBeauty userInfoBeauty, @Param("id") Integer id, @Param("userId") String userId);


	/**
	 * 根据IdAndUserId删除
	 */
	@Delete("DELETE FROM user_info_beauty WHERE id = #{id} AND user_id = #{userId}")
	 Integer deleteByIdAndUserId(@Param("id") Integer id,@Param("userId") String userId);


	/**
	 * 根据IdAndUserId获取对象
	 */
	@Select("SELECT * FROM user_info_beauty WHERE id = #{id} AND user_id = #{userId}")
	UserInfoBeauty selectByIdAndUserId(@Param("id") Integer id,@Param("userId") String userId);


	/**
	 * 根据UserId更新
	 */
	@Update("UPDATE user_info_beauty SET user_name = #{bean.userName}, gender = #{bean.gender}, age = #{bean.age} WHERE user_id = #{userId}")
	 Integer updateByUserId(@Param("bean") UserInfoBeauty userInfoBeauty,@Param("userId") String userId);


	/**
	 * 根据UserId删除
	 */
	@Delete("DELETE FROM user_info_beauty WHERE user_id = #{userId}")
	 Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * 根据UserId获取对象
	 */
	@Select("SELECT * FROM user_info_beauty WHERE user_id = #{userId}")
	UserInfoBeauty selectByUserId(@Param("userId") String userId);


	/**
	 * 根据Email更新
	 */
	@Update("UPDATE user_info_beauty SET user_name = #{bean.userName}, gender = #{bean.gender}, age = #{bean.age} WHERE email = #{email}")
	 Integer updateByEmail(@Param("bean") UserInfoBeauty userInfoBeauty,@Param("email") String email);


	/**
	 * 根据Email删除
	 */
	@Delete("DELETE FROM user_info_beauty WHERE email = #{email}")
	 Integer deleteByEmail(@Param("email") String email);


	/**
	 * 根据Email获取对象
	 */

	@Select("SELECT * FROM user_info_beauty WHERE email = #{email}")
	UserInfoBeauty selectByEmail(@Param("email") String email);

}
