package com.sesxh.okwebsocket.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_CONNECTED;
import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_RE_CONNECTED;
import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_ON_REPLY;
import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_ON_REPLY_BYTE;
import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_ON_FAILURE;
import static com.sesxh.okwebsocket.annotation.WebSocketStatus.STATUS_ON_CLOSED;


/**
 * @author LYH
 * @date 2021/1/26
 * @time 10:34
 * @desc
 **/

@IntDef({STATUS_CONNECTED,STATUS_RE_CONNECTED, STATUS_ON_REPLY,STATUS_ON_REPLY_BYTE,STATUS_ON_FAILURE,STATUS_ON_CLOSED})
@Retention(RetentionPolicy.SOURCE)
public @interface WebSocketStatus {

    int STATUS_CONNECTED= 0;//连接成功
    int STATUS_RE_CONNECTED = 1;//重连成功。
    int STATUS_ON_REPLY = 2;//收到服务器回复。
    int STATUS_ON_REPLY_BYTE = 3;//收到服务器回复。
    int STATUS_ON_FAILURE = 4;//出现异常
    int STATUS_ON_CLOSED = 5;//关闭

}
