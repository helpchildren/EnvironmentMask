package com.sesxh.okwebsocket;

import com.sesxh.okwebsocket.config.Config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okio.ByteString;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 14:51
 * @desc
 **/
public class OkWebSocket {

    private static OkWebSocketAPI sOkWebSocket;


    public static void init(Config config){
        sOkWebSocket=new OkWebSocketAPI(config);
    }

    /**
     * 获取连接，并返回观察对象
     */
    public static Observable<WebSocketInfo> get(String url){
        return sOkWebSocket.get(url);
    }

    /**
     * 发送，url的WebSocket已打开的情况下使用，否则会抛出异常
     *
     * @param msg 消息，String类型
     */
    public static Observable<Boolean> send(String url, String msg){
        return sOkWebSocket.send(url, msg);
    }

    /**
     * 发送，同上
     *
     * @param byteString 信息类型为ByteString
     */
    public static Observable<Boolean> send(String url, ByteString byteString){
        return sOkWebSocket.send(url, byteString);
    }

    /**
     * 不关心WebSocket是否连接，直接发送
     */
    public static Observable<Boolean> asyncSend(String url, String msg){
        return sOkWebSocket.asyncSend(url,msg);
    }

    /**
     * 同上，只是消息类型为ByteString
     */
    public static Observable<Boolean> asyncSend(String url, ByteString byteString){
        return sOkWebSocket.asyncSend(url,byteString);
    }

    /**
     * 关闭指定Url的连接
     */
    public static Observable<Boolean> close(String url){
        return sOkWebSocket.close(url);
    }

    /**
     * 马上关闭指定Url的连接
     */
    public static boolean closeNow(String url){
        return sOkWebSocket.closeNow(url);
    }

    /**
     * 关闭当前所有连接
     */
    public static Observable<List<Boolean>> closeAll(){
        return sOkWebSocket.closeAll();
    }

    /**
     * 马上关闭所有连接
     */
    public static void closeAllNow(){
        sOkWebSocket.closeAllNow();
    }

}
