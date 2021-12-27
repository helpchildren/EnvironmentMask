package com.sesxh.okwebsocket.cache;

import java.io.Serializable;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:42
 * @desc 缓存模型基类
 **/
public class CacheItem<T> implements Serializable {

    /**
     * 缓存的对象
     */
    private T cacheTarget;
    /**
     * 最近使用时间
     */
    private long recentlyUsedTime;

    public CacheItem(T cacheTarget, long recentlyUsedTime) {
        this.cacheTarget = cacheTarget;
        this.recentlyUsedTime = recentlyUsedTime;
    }

    public T getCacheTarget() {
        return cacheTarget;
    }

    public void setCacheTarget(T cacheTarget) {
        this.cacheTarget = cacheTarget;
    }

    public long getRecentlyUsedTime() {
        return recentlyUsedTime;
    }

    public void setRecentlyUsedTime(long recentlyUsedTime) {
        this.recentlyUsedTime = recentlyUsedTime;
    }

}
