package com.hxw.wxchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hxw.wxchat.entity.config.AppConfig;
import com.hxw.wxchat.entity.dto.MessageSendDto;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.enums.MessageTypeEnum;
import com.hxw.wxchat.entity.po.ChatMessage;
import com.hxw.wxchat.entity.vo.ResultResponse;
import com.hxw.wxchat.service.ChatMessageService;
import com.hxw.wxchat.service.ChatSessionUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/chat")
public class ChatController extends ABaseController{
    private static final Logger logger= LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatMessageService chatMessageService;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private AppConfig appConfig;
    @RequestMapping("/sendMassage")
    public ResultResponse sendMessage(HttpServletRequest request, @NotEmpty String contactId,
                                      @NotEmpty @Max(500) String messageContent,
                                      @NotNull Integer messageType,
                                      Long fileSize,
                                      String fileName,
                                      Integer fileType
                                      ) throws JsonProcessingException {



        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfo(request);
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize );
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);

        MessageSendDto messageSendDto=chatMessageService.saveMessage(chatMessage,tokenUserInfoDto);



        return new ResultResponse(ResultResponse.SUCCESS,ResultResponse.SUCCESS_CODE,"发送成功",messageSendDto);

    }
}
