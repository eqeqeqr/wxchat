package com.hxw.wxchat.controller;

import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.po.GroupInfo;
import com.hxw.wxchat.entity.query.GroupInfoQuery;
import com.hxw.wxchat.entity.query.UserInfoBeautyQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.service.GroupInfoService;
import org.apache.tomcat.util.buf.ByteChunk;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@RestController("adminGroupController")
@RequestMapping("/admin")
public class AdminGroupController {

    @Resource
    private GroupInfoService groupInfoService;
    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse loadGroup(GroupInfoQuery groupInfoQuery) throws IOException {
        groupInfoQuery.setOrderBy("create_time desc");
        groupInfoQuery.setQueryMemberCount(true);
        groupInfoQuery.setQueryGroupOwnerName(true);
        PaginationResultVO resultVO=groupInfoService.findListByPage(groupInfoQuery);
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统显示群组信息成功",resultVO);

    }
    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResultResponse dissolutionGroup(@NotEmpty String groupId) throws IOException {
        GroupInfo groupInfo=this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (groupInfo==null){
            new ResultResponse(ResultResponse.FILE,ResultResponse.FILE_CODE,"错误解散",null);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(),groupId);
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"管理员系统显示群组解散成功",null);

    }



}
