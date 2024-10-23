package com.hxw.wxchat.mappers;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.hxw.wxchat.entity.po.ChatSession;
import com.hxw.wxchat.entity.query.ChatSessionQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface ChatSessionMapper extends BaseMapper {

	/**
	 * 根据sessionId更新
	 */
	 Integer updateBysessionId(@Param("bean") ChatSession t, @Param("sessionId") String sessionId);


	/**
	 * 根据sessionId删除
	 */
	 Integer deleteBysessionId(@Param("sessionId") String sessionId);


	/**
	 * 根据sessionId获取对象
	 */
	ChatSession selectBysessionId(@Param("sessionId") String sessionId);

	@Override
	int insert(@Param("bean") Object entity);
	List selectList(@Param("query")ChatSessionQuery chatSessionQuery);

}
