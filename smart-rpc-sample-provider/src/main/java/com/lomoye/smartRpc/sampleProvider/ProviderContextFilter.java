package com.lomoye.smartRpc.sampleProvider;

import com.lomoye.smartRpc.common.ProviderFilter;
import com.lomoye.smartRpc.common.RpcContext;
import org.springframework.stereotype.Component;

/**
 * Created by lomoye on 2018/3/30.
 */
@Component
public class ProviderContextFilter implements ProviderFilter {
    @Override
    public void doFilter() {
        AppContext.getContext().setAttachments(RpcContext.getContext().getAttachments());
    }
}
