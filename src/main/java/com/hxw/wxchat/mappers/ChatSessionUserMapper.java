package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.wxchat.entity.po.ChatSessionUser;
import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.query.ChatSessionUserQuery;
import com.hxw.wxchat.entity.query.GroupInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话用户 数据库操作接口
 */
public interface ChatSessionUserMapper extends BaseMapper{

	/**
	 * 根据UserIdAndContactId更新
	 */
	 Integer updateByUserIdAndContactId(@Param("bean") ChatSessionUser t, @Param("userId") String userId, @Param("contactId") String contactId);


	/**
	 * 根据UserIdAndContactId删除
	 */
	 Integer deleteByUserIdAndContactId(@Param("userId") String userId,@Param("contactId") String contactId);


	/**
	 * 根据UserIdAndContactId获取对象
	 */
	ChatSessionUser selectByUserIdAndContactId(@Param("userId") String userId,@Param("contactId") String contactId);

	List<ChatSessionUser> selectList(@Param("query") ChatSessionUserQuery query);
	@Override
	int insert(@Param("bean") Object entity);
}
