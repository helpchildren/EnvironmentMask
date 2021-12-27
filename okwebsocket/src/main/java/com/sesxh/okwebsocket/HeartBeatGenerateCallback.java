package com.sesxh.okwebsocket;

/**
 * @author LYH
 * @date 2021/2/5
 * @time 11:23
 * @desc
 **/
public interface HeartBeatGenerateCallback {

    /**
     * 当需要生成心跳信息时回调
     * @return 要发送的心跳信息
     */
    String onGenerateHeartBeatMsg();
}
