package com.hxw.wxchat;

import com.hxw.wxchat.redis.RedisUtils;
import com.hxw.wxchat.websocket.netty.NettyWebSocketStarter;
import io.lettuce.core.RedisConnectionException;
import org.apache.ibatis.session.SqlSessionException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Component("initRunn")
public class InitRun implements ApplicationRunner {


    @Resource
    private DataSource dataSource;
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread(nettyWebSocketStarter).start();
            System.out.println(">>>>>>>>>>>>>>>>>>>>>服务器启动成功");
        }catch (SqlSessionException e){
            System.out.println("》》》》》》》》》》》》》》》》》》数据库配置错误，请检查");
        }catch (RedisConnectionException e){
            System.out.println("》》》》》》》》》》》》》》》》》》Redis配置错误，请检查");
        }catch (Exception e){
            System.out.println("》》》》》》》》》》》》》》》》》》初始化异常"+e);
        }
    }
}
