package com.hxw.wxchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.dto.UserContactSearchResultDto;
import com.hxw.wxchat.entity.enums.PageSize;
import com.hxw.wxchat.entity.enums.UserContactStatusEnum;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.entity.po.UserContact;
import com.hxw.wxchat.entity.po.UserInfo;
import com.hxw.wxchat.entity.query.UserContactApplyQuery;
import com.hxw.wxchat.entity.query.UserContactQuery;
import com.hxw.wxchat.entity.vo.PaginationResultVO;
import com.hxw.wxchat.entity.vo.ResponseVO;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.entity.vo.UserInfoVO;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.service.UserContactApplyService;
import com.hxw.wxchat.service.UserContactService;
import com.hxw.wxchat.service.UserInfoService;
import com.hxw.wxchat.utils.CopyTools;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.openjdk.nashorn.internal.runtime.UserAccessorProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.PushBuilder;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.BufferOverflowException;
import java.util.List;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController{
    @Autowired
    private UserContactService userContactService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserContactApplyService userContactApplyService;

    @RequestMapping("/search")
    @GlobalInterceptor
    public ResultResponse search(HttpServletRequest request, @NotEmpty String contactId) throws JsonProcessingException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        UserContactSearchResultDto resultDto=this.userContactService.searchContact(tokenUserInfoDt.getUserId(),contactId);
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"查询成功",resultDto);
    }
    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResultResponse applyAdd(HttpServletRequest request, @NotEmpty String contactId,String applyInfo) throws JsonProcessingException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        Integer joinType=this.userContactService.applyAdd(tokenUserInfoDt,contactId,applyInfo);
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"申请成功",joinType);
    }
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResultResponse loadApply(HttpServletRequest request, Integer pageNo) throws JsonProcessingException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        UserContactApplyQuery applyQuery=new UserContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(tokenUserInfoDt.getUserId());
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        applyQuery.setQueryContactInfo(true);

        PaginationResultVO resultVO=userContactApplyService.findListByPage(applyQuery);

        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"申请详情展示",resultVO);
    }

    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResultResponse dealWithApply(HttpServletRequest request, @NotNull Integer applyId,@NotNull Integer status) throws JsonProcessingException {
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);

       this.userContactApplyService.dealWithApply(tokenUserInfoDt.getUserId(),applyId,status);

        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"好友操作成功",null);
    }

    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResultResponse loadContact(HttpServletRequest request, @NotNull String contactType) throws JsonProcessingException {


        UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByName(contactType);
        if (contactTypeEnum==null){
            return new ResultResponse(ResultResponse.FILE,ResultResponse.FILE_CODE,"参数错误",null);
        }
        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);
        UserContactQuery contactQuery=new UserContactQuery();
        contactQuery.setUserId(tokenUserInfoDt.getUserId());
        contactQuery.setContactType(contactTypeEnum.getType());
        if (contactTypeEnum==UserContactTypeEnum.USER){
            contactQuery.setQueryContactUserInfo(true);
        }else if (contactTypeEnum==UserContactTypeEnum.GROUP){
            contactQuery.setQueryGroupInfo(true);
            contactQuery.setExcludeMyGroup(true);
        }
        contactQuery.setOrderBy("last_update_time desc");
        contactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(),

        });
        List<UserContact> contactList=this.userContactService.findListByParam(contactQuery);






        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群和好友显示",contactList);
    }





    /**
     * 获取联系人信息,不一定是好友 ，比如群聊查看信息
     */

    @RequestMapping("/getContactInfo")
    @GlobalInterceptor
    public ResultResponse getContactInfo(HttpServletRequest request, @NotNull String contactId) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);


        System.out.println(contactId);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(contactId);

        UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());



        UserContact userContact=userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDt.getUserId(),contactId);
        if (userContact!=null){
            userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        }

        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群和好友显示",userInfoVO);
    }


    /**
     * 获取联系人信息,一定是好友 比如好友查看信息
     */
    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    public ResultResponse getContactUserInfo(HttpServletRequest request, @NotNull String contactId) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);

        UserContact userContact=userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDt.getUserId(),contactId);



        if (userContact==null|| !ArrayUtils.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()
        },userContact.getStatus())){
            throw new  BusinessException("不是好友，或者被删除，被拉黑状态");
        }

        UserInfo userInfo=userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"群和好友显示",userInfoVO);
    }

    @RequestMapping("/delContact")
    @GlobalInterceptor
    public ResultResponse delContact(HttpServletRequest request, @NotNull String contactId) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);

        userContactService.removeUserContact(tokenUserInfoDt.getUserId(),contactId,UserContactStatusEnum.DEL);


        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"删除好友成功",null);
    }
    @RequestMapping("/addContact2BlackList")
    @GlobalInterceptor
    public ResultResponse addContact2BlackList(HttpServletRequest request, @NotNull String contactId) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDt=getTokenUserInfo(request);

        userContactService.removeUserContact(tokenUserInfoDt.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);


        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"拉黑好友成功",null);
    }
}
