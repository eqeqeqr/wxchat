package com.hxw.wxchat.utils;

import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.enums.UserContactTypeEnum;
import com.hxw.wxchat.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;


public class StringTools {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String getUserId(){
        return   UserContactTypeEnum.USER.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }
    public static String getGroupId(){
        return   UserContactTypeEnum.GROUP.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }
    public static final String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }

    public  static final String encodeMd5(String originString){
        return StringTools.isEmpty(originString)?null: DigestUtils.md5Hex(originString);
    }

    public static String cleanHtmlTag(String content){
        if (isEmpty(content)){
            return content;
        }
        content=content.replace("<","&lt");
        content=content.replace("\r\n","<br>");
        content=content.replace("\n","<br>");
        return content;

    }
    //保证会话id唯一且不变
    public static final String  getChatSessionId4User(String [] userIds){
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds,""));
    }
    //保证会话id唯一且不变
    public static final String  getChatSessionId4Group(String groupId){

        return encodeMd5(groupId);
    }
}
