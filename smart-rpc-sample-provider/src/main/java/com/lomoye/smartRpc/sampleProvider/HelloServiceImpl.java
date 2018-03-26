package com.lomoye.smartRpc.sampleProvider;

import com.lomoye.smartRpc.api.HelloService;
import com.lomoye.smartRpc.api.People;
import com.lomoye.smartRpc.common.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String words) {
        return "欢迎使用智能rpc...收到您的消息:" + words;
    }

    @Override
    public People makePeople(People man, People woman) {
        People people = new People();
        people.setName(man.getName() + "与" + woman.getName());
        people.setAge(1);
        return people;
    }
}