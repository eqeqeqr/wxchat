package com.hxw.wxchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@MapperScan(basePackages = {"com.hxw.wxchat.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class WxchatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxchatApplication.class, args);
    }

}
