package com.lomoye.smartRpc.consumer;

import com.lomoye.smartRpc.common.ConsumerFilter;
import com.lomoye.smartRpc.common.RpcContext;
import com.lomoye.smartRpc.common.RpcRequest;
import com.lomoye.smartRpc.common.RpcResponse;
import com.lomoye.smartRpc.register.ServiceDiscovery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lomoye on 2018/3/26.
 */
public class RpcProxy implements ApplicationContextAware {
    private ServiceDiscovery serviceDiscovery;//服务发现

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    private final List<ConsumerFilter> consumerFilters = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    //执行调用前过滤
                    doFilter(consumerFilters);

                    //远程调用方法
                    //生成请求
                    RpcRequest req = new RpcRequest();
                    req.setRequestId(UUID.randomUUID().toString());
                    req.setClassName(interfaceClass.getName());
                    req.setMethodName(method.getName());
                    req.setParameters(args);
                    req.setParameterTypes(method.getParameterTypes());

                    //上下文信息
                    req.setContext(RpcContext.getContext().getAttachments());

                    String serviceAddress = serviceDiscovery.discover();
                    String[] array = serviceAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    RpcConsumer consumer = new RpcConsumer(host, port);

                    //发送请求 获取返回值
                    RpcResponse resp = consumer.send(req);

                    //清除上下文
                    RpcContext.getContext().remove();

                    if (resp.getError() != null) {
                        throw resp.getError();
                    } else {
                        return resp.getResult();
                    }
                });
    }

    private void doFilter(List<ConsumerFilter> consumerFilters) {
        if (CollectionUtils.isEmpty(consumerFilters)) {
            return;
        }
        for (ConsumerFilter filter : consumerFilters) {
            filter.doFilter();
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //设置过滤器
        Map<String, ConsumerFilter> filterMap = applicationContext.getBeansOfType(ConsumerFilter.class);
        this.consumerFilters.addAll(filterMap.values());
    }
}
