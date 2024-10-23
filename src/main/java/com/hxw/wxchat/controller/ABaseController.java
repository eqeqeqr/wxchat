package com.hxw.wxchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.redis.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class ABaseController {
    @Autowired
    private RedisUtils  redisUtils;

    protected TokenUserInfoDto getTokenUserInfo(HttpServletRequest request) throws JsonProcessingException {
        String token = request.getHeader("token");
        String value =redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        ///////// 从redis中读取json的值解析成对象
        ObjectMapper objectMapper = new ObjectMapper();
        TokenUserInfoDto tokenUserInfoDto = objectMapper.readValue(value, TokenUserInfoDto.class);
        /////////
        return tokenUserInfoDto;
    }
}
