package com.sesxh.okwebsocket.cache;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:40
 * @desc 缓存模型接口
 **/
public interface ICacheTarget<T> {

    /**
     * 重置方法
     *
     * @return 重置后的对象
     */
    T reset();

}
