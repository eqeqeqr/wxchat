package com.hxw.wxchat.entity.dto;

import com.hxw.wxchat.entity.po.ChatMessage;
import com.hxw.wxchat.entity.po.ChatSessionUser;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public class WsInitData {
    private List<ChatSessionUser> chatSessionUserList;
    private List<ChatMessage> chatMessageList;
    private Integer applyCount;

    public List<ChatSessionUser> getChatSessionUserList() {
        return chatSessionUserList;
    }

    public void setChatSessionUserList(List<ChatSessionUser> chatSessionUserList) {
        this.chatSessionUserList = chatSessionUserList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}