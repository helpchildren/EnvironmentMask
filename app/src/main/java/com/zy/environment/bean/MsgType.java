package com.zy.environment.bean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;


import static com.zy.environment.bean.MsgType.TYPE_OUT;
import static com.zy.environment.bean.MsgType.TYPE_HEART;
import static com.zy.environment.bean.MsgType.TYPE_LOGIN;
import static com.zy.environment.bean.MsgType.TYPE_OUTBACK;
import static com.zy.environment.bean.MsgType.TYPE_QRMSG;
import static com.zy.environment.bean.MsgType.TYPE_OTHER;
import static com.zy.environment.bean.MsgType.TYPE_UPLOG;


@StringDef({TYPE_OUT, TYPE_HEART, TYPE_LOGIN, TYPE_OUTBACK, TYPE_QRMSG, TYPE_OTHER, TYPE_UPLOG})
@Retention(RetentionPolicy.SOURCE)
public @interface MsgType {

    String TYPE_OUT = "1";//出袋
    String TYPE_HEART = "2";//心跳
    String TYPE_LOGIN = "3";//设备登录
    String TYPE_OUTBACK = "4";//出袋回调
    String TYPE_QRMSG = "5";//获取二维码广告
    String TYPE_OTHER = "6";//其他

    String TYPE_UPLOG = "11";//拉取本地log

}
