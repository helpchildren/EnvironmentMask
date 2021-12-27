package com.zy.environment.utils.log;


import android.util.Log;

import com.zy.environment.config.GlobalSetting;

/**
 * @author LYH
 * @date 2021/1/15
 * @time 14:19
 * @desc
 **/

public class Logger {
    public static boolean sDebug = true;


    public static void v(String tag, String message) {
        if (sDebug) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (GlobalSetting.isDugLog) {
            XLogUtils.d(tag, message);
        }else {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (sDebug) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (sDebug) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (sDebug) {
            Log.e(tag, message);
        }
    }
}
