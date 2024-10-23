package com.hxw.wxchat.controller;

import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.config.CustomArithmeticCaptcha;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.vo.ResultResponse;

import com.hxw.wxchat.entity.vo.UserInfoVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.redis.RedisComponent;
import com.hxw.wxchat.redis.RedisUtils;
import com.hxw.wxchat.service.UserInfoService;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController {

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    private RedisComponent redisComponent;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MessageHandler messageHandler;
    @RequestMapping("/checkCode")
    public ResultResponse checkCode(){
        // 算术类型
        CustomArithmeticCaptcha captcha = new CustomArithmeticCaptcha(100, 42);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        String text = captcha.text();
        System.out.println(text);
        String checkCodeKey= UUID.randomUUID().toString();
        redisUtils.setEx(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,text, Constants.REDIS_TIME_1MIN*30, TimeUnit.SECONDS);
        String checkCodeBase64=captcha.toBase64();

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        System.out.println(checkCodeKey);
        System.out.println(text);
        return new ResultResponse(ResultResponse.SUCCESS, ResultResponse.SUCCESS_CODE, "验证成功", result);
    }

    @RequestMapping("register")
    public ResultResponse register(@NotEmpty @Param("checkCodeKey") String checkCodeKey,
                                   @NotEmpty @Email @Param("email")  String email,
                                   @NotEmpty @Param("password")  String password,
                                   @NotEmpty @Param("nickName")  String nickName,
                                   @NotEmpty @Param("checkCode") String checkCode){
        try {
            if (!checkCode.equalsIgnoreCase(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email,nickName,password);
            return new ResultResponse(ResultResponse.SUCCESS, ResultResponse.SUCCESS_CODE, "注册成功", null);
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }
        return  new ResultResponse(ResultResponse.FILE, ResultResponse.FILE_CODE, "注册失败", null);
    }

    @RequestMapping("login")
    public ResultResponse login(@NotEmpty String checkCodeKey,
                                   @NotEmpty @Email String email,
                                   @NotEmpty String password,
                                   @NotEmpty String checkCode){
        try {
            if (!checkCode.equalsIgnoreCase(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
                throw new BusinessException("图片验证码不正确");
            }
            UserInfoVO userInfoVO=userInfoService.login(email,password);

            return new ResultResponse(ResultResponse.SUCCESS, ResultResponse.SUCCESS_CODE, "登录成功", userInfoVO);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }
        return  new ResultResponse(ResultResponse.FILE, ResultResponse.FILE_CODE, "登录失败", null);
    }

    @RequestMapping("getSysSetting")
    @GlobalInterceptor
    public ResultResponse getSysSetting(){
        return new ResultResponse(ResultResponse.SUCCESS, ResultResponse.SUCCESS_CODE, "成功", redisComponent.getSysSetting());
    }

    @RequestMapping("/test")
    public ResultResponse test(){
        MessageSendDto sendDto=new MessageSendDto();
        sendDto.setMessageContent("哈哈哈哈哈哈"+System.currentTimeMillis());
        messageHandler.sendMessage(sendDto);
        return new ResultResponse(ResultResponse.SUCCESS, ResultResponse.SUCCESS_CODE, "测试成功",null);
    }
}
