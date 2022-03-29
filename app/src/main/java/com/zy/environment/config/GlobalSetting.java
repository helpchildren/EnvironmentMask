package com.zy.environment.config;

import android.content.Context;
import android.os.Environment;

import com.sesxh.appupdata.UpdataController;
import com.zy.environment.utils.SpStorage;

public class GlobalSetting {

    public static final String externpath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String AdvPath = externpath + "/zy/adv";
    public static final String AdFile = "adv.txt";

    public static String wsurl = "ws://47.93.97.68:2348";//服务器地址
    public static String uploadUrl = "http://bag.cnwinall.cn/wechat/device/upload";//日志上传地址
    public static String AppUpdateUrl = "https://bag.cnwinall.cn/apk_aio/apk.txt";//App更新地址


    public static String deviceid;//设备id
    public static String serialPortBag = "dev/ttyS0";//
    public static String serialPortMask = "dev/ttyS1";//
    public static int outLenBag = 229;//出货长度
    public static int outLenMask = 210;//出货长度

    public static boolean isDugLog = true;//是否开启文件日志


    public static void getSetting(Context context){
        SpStorage mSp = new SpStorage(context, "zy-environment");
        wsurl = (String) mSp.getSharedPreference("wsurl", wsurl);
        serialPortBag = (String) mSp.getSharedPreference("serialPortBag", serialPortBag);
        serialPortMask = (String) mSp.getSharedPreference("serialPortMask", serialPortMask);
        outLenBag = (Integer) mSp.getSharedPreference("outLenBag", outLenBag);
        outLenMask = (Integer) mSp.getSharedPreference("outLenMask", outLenMask);
//        isDugLog = (Boolean) mSp.getSharedPreference("isDugLog", isDugLog);

        UpdataController.setUpdateUrl(AppUpdateUrl);
        putSetting(context);
    }

    public static void putSetting(Context context){
        SpStorage mSp = new SpStorage(context, "zy-environment");
        mSp.put("wsurl", wsurl);
        mSp.put("serialPortBag", serialPortBag);
        mSp.put("serialPortMask", serialPortMask);
        mSp.put("outLenBag", outLenBag);
        mSp.put("outLenMask", outLenMask);
//        mSp.put("isDugLog", isDugLog);
        mSp.apply();
    }



}
