package com.hxw.wxchat.entity.vo;

import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.po.UserContact;

import java.util.List;

public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactsList;

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }

    public List<UserContact> getUserContactsList() {
        return userContactsList;
    }

    public void setUserContactsList(List<UserContact> userContactsList) {
        this.userContactsList = userContactsList;
    }
}
