package com.hxw.wxchat.websocket.netty;

import com.hxw.wxchat.entity.config.AppConfig;
import com.hxw.wxchat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Component("nettyWebSocketStarter")
public class NettyWebSocketStarter implements Runnable{
    private static final Logger logger= LoggerFactory.getLogger(NettyWebSocketStarter.class);
    private static EventLoopGroup bossGroup=new NioEventLoopGroup();
    private static EventLoopGroup workGroup =new NioEventLoopGroup();

    @Resource
    private AppConfig appConfig;
    @Resource
    private HandlerWebsocket handlerWebsocket;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
 /*   @Async*/

    @Override
    public void run() {

    try {
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 对http协议的支持，使用http的编码器和解码器
                            // 设置几个重要的处理器
                            pipeline.addLast(new HttpServerCodec());

                            // 聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            // 保证接收到http请求的完整性

                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            // 心跳IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
                            // readerIdleTime 读超时事件，即测试端一定时间内未接收到被测试端发送消息
                            // writerIdleTime 写超时事件，测试端一定事件内想被测试端发送消息
                            // allIdleTime 所有类型的超时时间
                            // TimeUnit 单位
                            pipeline.addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            // 将http协议升级成ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,65536,true,true,10000L));
                            pipeline.addLast(handlerWebsocket);
                            //
                        }
                    });
            Integer wsPort= appConfig.getWsPort();
            String wsPortStr=System.getProperty("ws.port");
            if (!StringTools.isEmpty(wsPortStr)){
                wsPort=Integer.valueOf(wsPortStr);
            }
            ChannelFuture channelFuture=serverBootstrap.bind(wsPort).sync();
            System.out.println("--------------------------启动netty成功 端口"+appConfig.getWsPort());
            logger.info("启动netty成功");
            channelFuture.channel().closeFuture().sync();

        }catch (Exception e){
            System.out.println("--------------------------启动netty失败"+e);
            logger.info("启动netty失败"+e);
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
