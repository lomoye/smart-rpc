package com.lomoye.smartRpc.consumer;

import com.lomoye.smartRpc.common.RpcRequest;
import com.lomoye.smartRpc.common.RpcResponse;
import com.lomoye.smartRpc.register.ServiceDiscovery;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Created by lomoye on 2018/3/26.
 */
public class RpcProxy {
    private ServiceDiscovery serviceDiscovery;//服务发现

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    //远程调用方法
                    //生成请求
                    RpcRequest req = new RpcRequest();
                    req.setRequestId(UUID.randomUUID().toString());
                    req.setClassName(interfaceClass.getName());
                    req.setMethodName(method.getName());
                    req.setParameters(args);
                    req.setParameterTypes(method.getParameterTypes());

                    String serviceAddress = serviceDiscovery.discover();
                    String[] array = serviceAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    RpcConsumer consumer = new RpcConsumer(host, port);

                    //发送请求 获取返回值
                    RpcResponse resp = consumer.send(req);
                    if (resp.getError() != null) {
                        throw resp.getError();
                    } else {
                        return resp.getResult();
                    }
                });
    }
}
