package com.lomoye.smartRpc.sampleProvider;

import com.lomoye.smartRpc.api.HelloService;
import com.lomoye.smartRpc.consumer.RpcProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lomoye on 2018/3/26.
 *
 */
public class HelloConsumer {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("world");
        System.out.println(result);

        System.exit(0);
    }
}
