package com.hxw.wxchat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.SysSettingDto;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.utils.StringTools;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     *
     * @Param userId
     * @Reaturn
     */

    public Long getUserHeartBeat(String userId) {
        Object value = redisUtils.get(Constants.REDIS_KEY_CHECK_HEART_BEAT + userId);
        return (Long) value;
    }

    public void saveUserHeartBeat(String userId) {
        redisUtils.setEx(Constants.REDIS_KEY_CHECK_HEART_BEAT + userId, String.valueOf(System.currentTimeMillis()), Constants.REDIS_KEY_EXPIRES_HEART_BEAT, TimeUnit.SECONDS);
    }
    public void removeUserHeartBeat(String userId){
        redisUtils.delete(Constants.REDIS_KEY_CHECK_HEART_BEAT + userId);

    }

    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) throws JsonProcessingException {
        ///////// 以json写入到redis中
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonValue = objectMapper.writeValueAsString(tokenUserInfoDto);
        redisUtils.setEx(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), jsonValue, Constants.REDIS_TIME_EXPIRES_DAY * 2, TimeUnit.SECONDS);
        redisUtils.setEx(Constants.REDIS_KEY_WS_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_TIME_EXPIRES_DAY * 2, TimeUnit.SECONDS);
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) throws JsonProcessingException {
        String value = redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        ///////// 从redis中读取json的值解析成对象
        if (StringTools.isEmpty(value)) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        TokenUserInfoDto tokenUserInfoDto = objectMapper.readValue(value, TokenUserInfoDto.class);
        return tokenUserInfoDto;
    }
    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) throws JsonProcessingException {
        String token=(String) redisUtils.get(Constants.REDIS_KEY_WS_USERID+userId);
        return getTokenUserInfoDto(token);
    }
    public void clearUserTokenByUserId(String userId){
        String token=redisUtils.get(Constants.REDIS_KEY_WS_USERID +userId);
        if (StringTools.isEmpty(token)){
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN + token);
    }
    public SysSettingDto getSysSetting() {

        Object value = redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        SysSettingDto sysSettingDto = (SysSettingDto) value;
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    // 清空联系热
    public void cleanUserContact(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    //批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactList) {
        redisUtils.lLeftPushAll(Constants.REDIS_KEY_USER_CONTACT + userId, contactList);
        redisUtils.expire(Constants.REDIS_KEY_USER_CONTACT + userId, Constants.REDIS_TIME_EXPIRES_DAY, TimeUnit.DAYS);
    }
    //单独添加
    public void addUserContact(String userId,String contactId){
        List<String> contactIdList=getUserContactList(userId);
        if (contactIdList.contains(contactId)){
            return;
        }
        redisUtils.lLeftPush(Constants.REDIS_KEY_USER_CONTACT+userId,contactId);
        redisUtils.expire(Constants.REDIS_KEY_USER_CONTACT+userId,Constants.REDIS_TIME_1MIN*100000,TimeUnit.SECONDS);
    }


    public List<String> getUserContactList(String userId){
        List<String> value = redisUtils.lRange(Constants.REDIS_KEY_USER_CONTACT + userId, 0, -1);
        System.out.println("value" + value);
        return value;
    }


}
