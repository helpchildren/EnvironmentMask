package com.sesxh.okwebsocket;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 10:54
 * @desc
 **/
public enum WebSocketCloseEnum {
    ACTIVE_CLOSE(1000,"主动关闭连接");

    private final int mCode;
    private final String mReason;
    WebSocketCloseEnum(int code, String reason) {
        mCode = code;
        mReason = reason;
    }

    public int getCode() {
        return mCode;
    }

    public String getReason() {
        return mReason;
    }
}
