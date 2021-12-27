package com.sesxh.okwebsocket;

import com.sesxh.okwebsocket.annotation.WebSocketStatus;
import com.sesxh.okwebsocket.cache.ICacheTarget;

import java.io.Serializable;

import okhttp3.WebSocket;
import okio.ByteString;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:39
 * @desc
 **/
public class WebSocketInfo implements Serializable, ICacheTarget<WebSocketInfo> {

    private static final long serialVersionUID = -880481254453932113L;

    private WebSocket mWebSocket;
    private String mStringMsg;
    private ByteString mByteStringMsg;
    private @WebSocketStatus int mStatus;
    /**
     * 连接成功
     */
    private boolean isConnect;
    /**
     * 重连成功
     */
    private boolean isReconnect;
    /**
     * 准备重连
     */
    private boolean isPrepareReconnect;

    /**
     * 重置
     */
    @Override
    public WebSocketInfo reset() {
        this.mWebSocket = null;
        this.mStringMsg = null;
        this.mByteStringMsg = null;
        this.isConnect = false;
        this.isReconnect = false;
        this.isPrepareReconnect = false;
        return this;
    }


    public WebSocketInfo setConnect(boolean connect) {
        isConnect = connect;
        return this;
    }

    public WebSocketInfo setReconnect(boolean reconnect) {
        isReconnect = reconnect;
        return this;
    }

    public WebSocketInfo setPrepareReconnect(boolean prepareReconnect) {
        isPrepareReconnect = prepareReconnect;
        return this;
    }

    public WebSocketInfo setStringMsg(String stringMsg) {
        mStringMsg = stringMsg;
        return this;
    }

    public WebSocketInfo setByteStringMsg(ByteString byteStringMsg) {
        mByteStringMsg = byteStringMsg;
        return this;
    }

    public WebSocketInfo setWebSocket(WebSocket webSocket) {
        mWebSocket = webSocket;
        return this;
    }

    public WebSocketInfo setStatus(@WebSocketStatus int status) {
        mStatus=status;
        return this;
    }

    public WebSocket getWebSocket() {
        return mWebSocket;
    }

    public String getStringMsg() {
        return mStringMsg;
    }

    public ByteString getByteStringMsg() {
        return mByteStringMsg;
    }


    public boolean isConnect() {
        return isConnect;
    }

    public boolean isReconnect() {
        return isReconnect;
    }

    public boolean isPrepareReconnect() {
        return isPrepareReconnect;
    }

    public int getStatus() {
        return mStatus;
    }

    @Override
    public String toString() {
        return "WebSocketInfo{" +
                "mStringMsg='" + mStringMsg + '\'' +
                ", mStatus=" + mStatus +
                ", isConnect=" + isConnect +
                ", isReconnect=" + isReconnect +
                ", isPrepareReconnect=" + isPrepareReconnect +
                '}';
    }
}
