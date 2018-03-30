package com.lomoye.smartRpc.sampleProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lomoye on 2018/3/30.
 *
 */
public class AppContext {
    private static final ThreadLocal<AppContext> LOCAL = ThreadLocal.withInitial(AppContext::new);

    private AppContext() {
    }

    public static AppContext getContext() {
        return LOCAL.get();
    }

    public void remove() {
        LOCAL.remove();
    }

    private Map<String, String> attachments = new HashMap<>();//远程传递的上下文信息

    public void setAttachment(String key, String value) {
        if (value == null) {
            attachments.remove(key);
            return;
        }
        attachments.put(key, value);
    }


    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }


    public void setAttachments(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        this.attachments = attachments;
    }
}
