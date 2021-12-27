package com.zy.environment.base;

import android.app.Activity;
import android.app.Application;

import com.sesxh.okwebsocket.OkWebSocket;
import com.sesxh.okwebsocket.config.Config;
import com.zy.environment.utils.log.CrashHandler;
import com.zy.environment.utils.log.XLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class BaseApplication extends Application {
    private List<Activity> oList;//用于存放所有启动的Activity的集合

    @Override
    public void onCreate() {
        super.onCreate();
        oList = new ArrayList<Activity>();
        //日志打印初始化
        XLogUtils.getInstance()
                .setDebugLog(true)
                .setCleardate(3)
                .setFoldername("zy/"+getPackageName()+"/ToolXLog")
                .XlogInit();
        //初始化崩溃打印
        CrashHandler.getInstance(getApplicationContext())
                .setFoldername("zy/"+getPackageName()+"/ToolCrash")
                .init();
        //websocket初始化
        OkWebSocket.init(new Config.Builder(getApplicationContext())
                .debug(false)
                .pingInterval(10, TimeUnit.SECONDS)
                .reconnectInterval(10, TimeUnit.SECONDS)
                .build());
    }


    /**
     * 添加Activity
     */
    public void addActivity_(Activity activity) {
        // 判断当前集合中不存在该Activity
        if (!oList.contains(activity)) {
            oList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity_(Activity activity) {
        //判断当前集合中存在该Activity
        if (oList.contains(activity)) {
            oList.remove(activity);//从集合中移除
            activity.finish();//销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeALLActivity_() {
        //通过循环，把集合中的所有Activity销毁
        for (Activity activity : oList) {
            activity.finish();
        }
    }

}
