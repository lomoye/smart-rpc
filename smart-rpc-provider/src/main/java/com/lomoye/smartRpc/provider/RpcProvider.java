package com.lomoye.smartRpc.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by lomoye on 2018/3/22.
 * rpc服务提供者
 */
public class RpcProvider implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProvider.class);

    //TODO 注入服务注册器

    private Map<String/*服务名称*/, Object> serviceMap;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> rpcServiceMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        serviceMap.putAll(rpcServiceMap);

        Object o = new Object();
        Class clazz = o.getClass().getAnnotation(RpcService.class).value();
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            try {
                Object invoke = m.invoke(o, new Object());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
