package com.lomoye.smartRpc.provider;


import com.lomoye.smartRpc.common.ProviderFilter;
import com.lomoye.smartRpc.common.RpcContext;
import com.lomoye.smartRpc.common.RpcRequest;
import com.lomoye.smartRpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Created by lomoye on 2018/3/22.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    private final List<ProviderFilter> providerFilters;

    public RpcHandler(Map<String, Object> handlerMap, List<ProviderFilter> providerFilters) {
        this.handlerMap = handlerMap;
        this.providerFilters = providerFilters;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            //入口设置上细纹信息
            RpcContext.getContext().setAttachments(rpcRequest.getContext());

            doFilter(providerFilters);//过滤

            Object result = handle(rpcRequest);
            response.setResult(result);
            channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Throwable t) {
            response.setError(t);
        } finally {
            RpcContext.getContext().remove();
        }
    }

    private void doFilter(List<ProviderFilter> providerFilters) {
        if (CollectionUtils.isEmpty(providerFilters)) {
            return;
        }

        for (ProviderFilter providerFilter : providerFilters) {
            providerFilter.doFilter();
        }
    }

    private Object handle(RpcRequest rpcRequest) throws InvocationTargetException {
        String className = rpcRequest.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
