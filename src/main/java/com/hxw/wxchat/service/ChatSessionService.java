package com.hxw.wxchat.service;

import java.util.List;

import com.hxw.wxchat.entity.query.ChatSessionQuery;
import com.hxw.wxchat.entity.po.ChatSession;
import com.hxw.wxchat.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface ChatSessionService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatSession> findListByParam(ChatSessionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ChatSessionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param);

	/**
	 * 新增
	 */
	Integer add(ChatSession bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatSession> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ChatSession> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ChatSession bean,ChatSessionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ChatSessionQuery param);

	/**
	 * 根据sessionId查询对象
	 */
	ChatSession getChatSessionBysessionId(String sessionId);


	/**
	 * 根据sessionId修改
	 */
	Integer updateChatSessionBysessionId(ChatSession bean,String sessionId);


	/**
	 * 根据sessionId删除
	 */
	Integer deleteChatSessionBysessionId(String sessionId);

}