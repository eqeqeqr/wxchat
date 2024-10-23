package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.wxchat.entity.po.ChatMessage;
import com.hxw.wxchat.entity.po.ChatSession;
import com.hxw.wxchat.entity.query.ChatMessageQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息表 数据库操作接口
 */
public interface ChatMessageMapper extends BaseMapper {

	/**
	 * 根据MessageId更新
	 */

	 Integer updateByMessageId(@Param("bean") ChatMessage t, @Param("messageId") Long messageId);


	/**
	 * 根据MessageId删除
	 */

	 Integer deleteByMessageId(@Param("messageId") Long messageId);


	/**
	 * 根据MessageId获取对象
	 */
	ChatMessage selectByMessageId(@Param("messageId") Long messageId);
	@Override
	int insert(@Param("bean") Object entity);


	List selectList(@Param("query") ChatMessageQuery chatMessageQuery);


}
