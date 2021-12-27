package com.zy.environment.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zy.environment.MainActivity;


public class AppReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";//开机广播

    public AppReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            //自启动应用
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
