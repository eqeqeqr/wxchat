package com.hxw.wxchat.entity.constants;

import com.hxw.wxchat.entity.enums.UserContactTypeEnum;

import javax.faces.event.PostPutFlashValueEvent;

public class Constants {
    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT=6;
    public static final String REDIS_KEY_CHECK_CODE="wxchat:checkcode";
    public static final String REDIS_KEY_CHECK_HEART_BEAT="wxchat:ws:user:heartbeat";
    public static final String REDIS_KEY_WS_TOKEN="wxchat:ws:token";
    public static final String REDIS_KEY_WS_USERID="wxchat:ws:token:userid";
    public static final Integer REDIS_TIME_1MIN=60;
    public static final Integer REDIS_TIME_EXPIRES_DAY=REDIS_TIME_1MIN*60*24;
    public static final Integer LENGTH_11=11;
    public static final Integer LENGTH_20=20;

    public static final String ROBOT_UID= UserContactTypeEnum.USER.getPrefix()+"robot";
    public static final String REDIS_KEY_SYS_SETTING="wxchat:syssetting";

    public static final String FILE_FOLDER_FILE="/file/";
    public static final String FILE_FOLDER_AVATAR_NAME="avatar/";
    public static final String IMAGE_SUFFIX=".png";
    public static final String COVER_IMAGE_SUFFIX="_cover.png";
    public static final String APPLY_INFO_TEMPLATE="我是%s";

    public static final String REGEX_PASSWORD="^(?=.*\\d)(?=.*[a-zA-Z])[\\da-z-A-Z~!@#$%^&*_]{8-18}$";
    //用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT="wxchat:ws:user:contact:";


    public static final Long MILLIS_ECONDS_3days_age=3*24*60*60*1000L;
}
