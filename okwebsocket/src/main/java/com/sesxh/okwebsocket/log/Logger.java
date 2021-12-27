package com.sesxh.okwebsocket.log;

import android.util.Log;


/**
 * @author LYH
 * @date 2021/1/15
 * @time 14:19
 * @desc
 **/

public class Logger {
    public static boolean sDebug;


    public static void v(String tag, String message) {
        if (sDebug) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (sDebug) {
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
