package com.hxw.wxchat.annotation;

import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {
    // 登录检查
    boolean checkLogin() default  true;
    // 管理员检查
    boolean checkAdmin() default false;

}
