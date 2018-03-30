package com.lomoye.smartRpc.sampleConsumer;

import com.lomoye.smartRpc.common.ConsumerFilter;
import com.lomoye.smartRpc.common.RpcContext;

/**
 * Created by lomoye on 2018/3/30.
 * 上下文过滤器
 */
public class ConsumerContextFilter implements ConsumerFilter {
    @Override
    public void doFilter() {
        //上下文信息
        System.out.println("设置上下文中");
        RpcContext.getContext().setAttachment("shard_id", "1");
    }
}
