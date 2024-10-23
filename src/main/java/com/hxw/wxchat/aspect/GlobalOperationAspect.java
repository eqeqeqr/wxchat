package com.hxw.wxchat.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxw.wxchat.annotation.GlobalInterceptor;
import com.hxw.wxchat.entity.constants.Constants;
import com.hxw.wxchat.entity.dto.TokenUserInfoDto;
import com.hxw.wxchat.entity.enums.ResponseCodeEnum;
import com.hxw.wxchat.exception.BusinessException;
import com.hxw.wxchat.redis.RedisUtils;
import com.hxw.wxchat.utils.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.logging.Logger;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    // private static final Logger logger= (Logger) LoggerFactory.getLogger(GlobalInterceptor.class);
   @Autowired
   private RedisUtils redisUtils;
   /*@Pointcut("@annotation(com.hxw.wxchat.annotation.GlobalInterceptor)")
    public void pointcut(){}*/
    @Before("@annotation(com.hxw.wxchat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint point){
        try {
            Method method=((MethodSignature)point.getSignature()).getMethod();
            GlobalInterceptor interceptor=method.getAnnotation(GlobalInterceptor.class);
            if (interceptor==null){
                return;
            }
            if (interceptor.checkLogin()||interceptor.checkAdmin()){
                System.out.println(">>>>>>>>>>"+"开始检查");
                checkLogin(interceptor.checkAdmin());
            }
        }catch (BusinessException e){
            System.out.println("全局拦截异常BusinessException"+e);
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    private void checkLogin(Boolean checkAdmin) throws JsonProcessingException {
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request= (HttpServletRequest) attributes.getRequest();
        String token=request.getHeader("token");

        String value =redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);


        ///////// 从redis中读取json的值解析成对象
        ObjectMapper objectMapper = new ObjectMapper();
        TokenUserInfoDto tokenUserInfoDto = objectMapper.readValue(value, TokenUserInfoDto.class);
        /////////
        if (StringTools.isEmpty(token)){

            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (tokenUserInfoDto==null){

            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (checkAdmin&&!tokenUserInfoDto.getAdmin()){

            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
