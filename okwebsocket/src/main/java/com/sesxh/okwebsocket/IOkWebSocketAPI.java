package com.sesxh.okwebsocket;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okio.ByteString;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:38
 * @desc
 **/
public interface IOkWebSocketAPI {

    /**
     * 获取连接，并返回观察对象
     */
    Observable<WebSocketInfo> get(String url);
    /**
     * 发送，url的WebSocket已打开的情况下使用，否则会抛出异常
     *
     * @param msg 消息，String类型
     */
    Observable<Boolean> send(String url, String msg);

    /**
     * 发送，同上
     *
     * @param byteString 信息类型为ByteString
     */
    Observable<Boolean> send(String url, ByteString byteString);

    /**
     * 不关心WebSocket是否连接，直接发送
     */
    Observable<Boolean> asyncSend(String url, String msg);

    /**
     * 同上，只是消息类型为ByteString
     */
    Observable<Boolean> asyncSend(String url, ByteString byteString);

    /**
     * 发送心跳包
     */
    Observable<Boolean> heartBeat(String url, int period, TimeUnit unit, HeartBeatGenerateCallback heartBeatGenerateCallback);

    /**
     * 关闭指定Url的连接
     */
    Observable<Boolean> close(String url);

    /**
     * 马上关闭指定Url的连接
     */
    boolean closeNow(String url);

    /**
     * 关闭当前所有连接
     */
    Observable<List<Boolean>> closeAll();

    /**
     * 马上关闭所有连接
     */
    void closeAllNow();

}
