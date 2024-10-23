package com.hxw.wxchat.controller;

import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.query.UserInfoQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.awt.print.PageFormat;
import java.io.IOException;

@RestController("adminUserInfoController")
@RequestMapping("/admin")
public class AdminUserInfoController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;
    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    /*@Pattern(regexp = Constants.REGEX_PASSWORD) 校验密码*/
    public ResultResponse loadUser(UserInfoQuery userInfoQuery) throws IOException {

        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO=userInfoService.findListByPage(userInfoQuery);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统显示用户信息成功",resultVO);

    }
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse updateUserStatus(@NotNull Integer status, @NotEmpty String userId) throws IOException {

        userInfoService.updateUserStatus(status,userId);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统用户状态修改成功",null);

    }
    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse forceOffLine( @NotEmpty String userId) throws IOException {

        userInfoService.forceOffLine(userId);
        // TODO 退出登录 ，关闭ws连接
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统用户强制下线成功",null);

    }
}
