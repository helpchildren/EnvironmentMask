package com.sesxh.okwebsocket.cache;

import com.sesxh.okwebsocket.WebSocketInfo;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:55
 * @desc
 **/
public class WebSocketInfoPool extends BaseCachePool<WebSocketInfo> {
    @Override
    public WebSocketInfo onCreateCache() {
        return new WebSocketInfo();
    }

    @Override
    public int onSetupMaxCacheCount() {
        return 6;
    }
}
