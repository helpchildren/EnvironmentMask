package com.zy.environment.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.zy.environment.widget.SettingDialog;

import java.io.IOException;
import java.util.List;

public class ToolsUtils {

    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;

    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    /*
    * 获取设备id
    * */
    public static String getDeviceId(Context context){
        String str = Settings.System.getString(context.getContentResolver(), "android_id").toUpperCase();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DJ");
        stringBuilder.append(str.substring(str.length() - 8, str.length()));
        return stringBuilder.toString();
    }

    /*
    * 进入设置页面
    * */
    final static int COUNTS = 3;// 点击次数
    final static long DURATION = 1000;// 规定有效时间
    static long[] mHits = new long[COUNTS];
    public static void continuousClick(Activity activity) {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //为数组最后一位赋值
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
            mHits = new long[COUNTS];//重新初始化数组
            //弹出密码框
            SettingDialog dialog = new SettingDialog(activity);
            dialog.show();
        }
    }

    /*
    * 获取视频时长
    * */
    public static int getVideoTime(String path){
        int time = 0;
        MediaPlayer mediaPlayer = new MediaPlayer();
        long startT = System.currentTimeMillis();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            time = mediaPlayer.getDuration();
            Log.e("lfntest","获取视频时长："+time+"  耗时："+(System.currentTimeMillis()-startT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 比较两个List集合是否相等
     */
    public static <E>boolean isListEqual(List<E> list1, List<E> list2) {
        // 两个list引用相同（包括两者都为空指针的情况）
        if (list1 == list2) {
            return true;
        }
        // 两个list都为空（包括空指针、元素个数为0）
        if (list1 == null && list2.size() == 0 || list2 == null && list1.size() == 0) {
            return true;
        }
        if (list1 != null && list2 != null){
            // 两个list元素个数不相同
            if (list1.size() != list2.size()) {
                return false;
            }
            // 两个list元素个数已经相同，再比较两者内容
            // 采用这种可以忽略list中的元素的顺序
            // 涉及到对象的比较是否相同时，确保实现了equals()方法
            return list1.containsAll(list2);
        }
        return true;
    }

}
