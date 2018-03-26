package com.lomoye.smartRpc.provider;

import com.lomoye.smartRpc.common.*;
import com.lomoye.smartRpc.register.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by lomoye on 2018/3/22.
 * rpc服务提供者
 */
public class RpcProvider implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProvider.class);

    //注入服务注册器
    private ServiceRegistry serviceRegistry;

    private String serviceAddress;//服务地址

    private Map<String/*服务名称*/, Object> handlerMap;

    public RpcProvider(ServiceRegistry serviceRegistry, String serviceAddress) {
        this.serviceRegistry = serviceRegistry;
        this.serviceAddress = serviceAddress;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class)) // 将 RPC 请求进行解码（为了处理请求）
                                    .addLast(new RpcEncoder(RpcResponse.class)) // 将 RPC 响应进行编码（为了返回响应）
                                    .addLast(new RpcHandler(handlerMap)); // 处理 RPC 请求
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //启动bootstrap
            String[] address = serviceAddress.split(":");
            ChannelFuture future = bootstrap.bind(address[0], Integer.valueOf(address[1])).sync();

            //向zookeeper注册服务
            serviceRegistry.register(serviceAddress);

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> rpcServiceMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        if (MapUtils.isEmpty(rpcServiceMap)) {
            return;
        }

        rpcServiceMap.forEach((k, v) -> {
            String serviceName = v.getClass().getAnnotation(RpcService.class).value().getName();
            handlerMap.put(serviceName, v);
        });
    }
}
