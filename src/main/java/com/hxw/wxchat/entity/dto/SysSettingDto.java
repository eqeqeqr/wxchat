package com.hxw.wxchat.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hxw.wxchat.entity.constants.Constants;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {
    private static final long serialVersionUID=-4052701002357141142L;
    // 最大群主数
    private Integer maxGroupCount=5;
    // 最大成员数
    private Integer maxGroupMemberCount=500;
    // 最大图片
    private Integer maxImageSize=2;
    // 视频大小
    private Integer maxVideoSize=5;
    // 最大文件
    private Integer maxFileSize=5;
    // 机器人昵称
    private String robotNickName="EasyChat";
    // 机器人id
    private String robotUid= Constants.ROBOT_UID;
    // 机器人欢迎语
    private String robotWelcome="欢迎使用EasyChat";

    public Integer getMaxGroupCount() {
        return maxGroupCount;
    }

    public void setMaxGroupCount(Integer maxGroupCount) {
        this.maxGroupCount = maxGroupCount;
    }

    public Integer getMaxGroupMemberCount() {
        return maxGroupMemberCount;
    }

    public void setMaxGroupMemberCount(Integer maxGroupMemberCount) {
        this.maxGroupMemberCount = maxGroupMemberCount;
    }

    public Integer getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(Integer maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public Integer getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(Integer maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getRobotNickName() {
        return robotNickName;
    }

    public void setRobotNickName(String robotNickName) {
        this.robotNickName = robotNickName;
    }

    public String getRobotUid() {
        return robotUid;
    }

    public void setRobotUid(String robotUid) {
        this.robotUid = robotUid;
    }

    public String getRobotWelcome() {
        return robotWelcome;
    }

    public void setRobotWelcome(String robotWelcome) {
        this.robotWelcome = robotWelcome;
    }
}
