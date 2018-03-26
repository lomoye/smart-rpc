package com.lomoye.smartRpc.consumer;

import com.lomoye.smartRpc.common.RpcDecoder;
import com.lomoye.smartRpc.common.RpcEncoder;
import com.lomoye.smartRpc.common.RpcRequest;
import com.lomoye.smartRpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lomoye on 2018/3/26.
 * rpc服务消费者
 */
public class RpcConsumer extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumer.class);

    private String host;

    private int port;

    private RpcResponse rpcResponse;

    private final Object lock = new Object();

    public RpcConsumer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest req) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new RpcEncoder(RpcRequest.class))
                .addLast(new RpcDecoder(RpcResponse.class))
                .addLast(RpcConsumer.this);
            }
        }).option(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(req).sync();

            synchronized (lock) {
                lock.wait(); // 未收到响应，使线程等待
            }

            if (rpcResponse != null) {
                future.channel().closeFuture().sync();
            }
            return rpcResponse;
        } finally {
            group.shutdownGracefully();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.rpcResponse = rpcResponse;

        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
