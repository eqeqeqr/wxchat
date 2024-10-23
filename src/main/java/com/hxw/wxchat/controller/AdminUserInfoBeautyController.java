package com.hxw.wxchat.controller;

import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.po.UserInfoBeauty;
import com.hxw.wxchat.entity.query.UserInfoBeautyQuery;
import com.hxw.wxchat.entity.query.UserInfoQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.service.UserInfoBeautyService;
import com.hxw.wxchat.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
public class AdminUserInfoBeautyController {
    @Resource
    private UserInfoBeautyService userInfoBeautyService;
    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse loadBeautyAccounntList(UserInfoBeautyQuery beautyQuery) throws IOException {
        beautyQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO=userInfoBeautyService.findListByPage(beautyQuery);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统显示用户信息成功",resultVO);

    }
    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse saveBeautyAccount(UserInfoBeauty userInfoBeauty) throws IOException {
        userInfoBeautyService.saveCount(userInfoBeauty);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统 靓号添加信息成功",null);

    }
    @RequestMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse delBeautyAccount(@NotNull Integer id) throws IOException {
        userInfoBeautyService.deleteUserInfoBeautyById(id);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统 靓号删除信息成功",null);

    }
}
