package com.lomoye.smartRpc.api;

/**
 * Created by lomoye on 2018/3/26.
 *
 */
public interface HelloService {
    String hello(String words);

    People makePeople(People man, People woman);
}
