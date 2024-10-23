package com.hxw.wxchat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.query.ChatSessionQuery;
import com.hxw.wxchat.entity.po.ChatSession;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.query.SimplePage;
import com.hxw.wxchat.mappers.ChatSessionMapper;
import com.hxw.wxchat.service.ChatSessionService;
import com.hxw.wxchat.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService {

	@Resource
	private ChatSessionMapper chatSessionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSession> findListByParam(ChatSessionQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return this.chatSessionMapper.selectList(queryWrapper);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatSessionQuery param) {
		QueryWrapper queryWrapper=new QueryWrapper(param);
		return Math.toIntExact(this.chatSessionMapper.selectCount(queryWrapper));
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatSession> list = this.findListByParam(param);
		PaginationResultVO<ChatSession> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatSession bean) {
		return this.chatSessionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatSession> listBean) {
		return null;
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatSession> listBean) {
		return null;
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatSession bean, ChatSessionQuery param) {
		return null;
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatSessionQuery param) {
		return null;
	}

	/**
	 * 根据sessionId获取对象
	 */
	@Override
	public ChatSession getChatSessionBysessionId(String sessionId) {
		return this.chatSessionMapper.selectBysessionId(sessionId);
	}

	/**
	 * 根据sessionId修改
	 */
	@Override
	public Integer updateChatSessionBysessionId(ChatSession bean, String sessionId) {
		return this.chatSessionMapper.updateBysessionId(bean, sessionId);
	}

	/**
	 * 根据sessionId删除
	 */
	@Override
	public Integer deleteChatSessionBysessionId(String sessionId) {
		return this.chatSessionMapper.deleteBysessionId(sessionId);
	}
}