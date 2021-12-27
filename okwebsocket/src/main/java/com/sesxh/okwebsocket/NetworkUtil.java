package com.sesxh.okwebsocket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author LYH
 * @date 2021/2/5
 * @time 11:25
 * @desc
 **/
public class NetworkUtil {
    private NetworkUtil() {
    }

    /**
     * 当前是否有网络状态
     *
     * @param context  上下文
     * @param needWifi 是否需要wifi网络
     */
    public static boolean hasNetWorkStatus(Context context, boolean needWifi) {
        NetworkInfo info = getActiveNetwork(context);
        if (info == null) {
            return false;
        }
        if (!needWifi) {
            return info.isAvailable();
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 获取活动网络连接信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    public static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager mConnMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnMgr == null) {
            return null;
        }
        // 获取活动网络连接信息
        return mConnMgr.getActiveNetworkInfo();
    }
}
