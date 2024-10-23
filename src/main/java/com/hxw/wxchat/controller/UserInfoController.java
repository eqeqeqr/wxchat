package com.hxw.wxchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.entity.vo.UserInfoVO;
import com.hxw.wxchat.service.UserInfoService;
import com.hxw.wxchat.utils.CopyTools;
import com.hxw.wxchat.utils.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

    @Autowired
    private UserInfoService userInfoService;
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResultResponse getUserInfo(HttpServletRequest request) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(tokenUserInfoDt.getUserId());
        UserInfoVO userInfoVO=CopyTools.copy(userInfo,UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDt.getAdmin());

        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"获取用户信息",userInfoVO);
    }

    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResultResponse saveUserInfo(HttpServletRequest request, UserInfo userInfo,
                                       MultipartFile avatarFile,
                                       MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        userInfo.setUserId(tokenUserInfoDt.getUserId());
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);

        this.userInfoService.updateUserInfo(userInfo,avatarFile,avatarCover);
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"获取用户信息",userInfo);
    }

    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    /*@Pattern(regexp = Constants.REGEX_PASSWORD) 校验密码*/
    public ResultResponse updatePassword(HttpServletRequest request, @NotEmpty  String password) throws IOException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(tokenUserInfoDt.getUserId());
        userInfo.setPassword(StringTools.encodeMd5(password));
        this.userInfoService.updateUserInfoByUserId(userInfo,tokenUserInfoDt.getUserId());
        // TODO 强制退出
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"修改密码信息成功",null);

    }
    @RequestMapping("/logout")
    @GlobalInterceptor
    /*@Pattern(regexp = Constants.REGEX_PASSWORD) 校验密码*/
    public ResultResponse logout(HttpServletRequest request) throws IOException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"修改密码信息成功",null);

    }
}
