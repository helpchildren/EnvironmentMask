package com.sesxh.appupdata;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.sesxh.appupdata.bean.UpdateInfo;
import com.sesxh.appupdata.callback.UpdataCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UpdataController {

    private final static String TAG = "UpdataController";
    public static String updateUrl = "https://bag.cnwinall.cn/apk/apk.txt";

    public static void setUpdateUrl(String updateUrl) {
        UpdataController.updateUrl = updateUrl;
    }


    public static void getUpDataInfo(final Context context, final UpdataCallback updataCallback){
        new Thread(new Runnable(){
            @Override
            public void run() {
                StringBuffer strB = new StringBuffer();   //strB用来存储txt文件里的内容
                String str = "";
                URL url = null;
                try {
                    url = new URL(updateUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
                    while ((str = br.readLine()) != null) {
                        strB.append(str);   //将读取的内容放入strB
                    }
                    br.close();
                    try {
                        getApkVersion(context, strB.toString(),updataCallback);
                    } catch (Exception e) {
                        if (updataCallback != null) updataCallback.onError("数据格式错误："+strB.toString());
                    }
                } catch (IOException e) {
                    if (updataCallback != null) updataCallback.onError("获取更新信息失败,请检查服务器："+updateUrl+"路径下是否有更新文件！");
                }
            }
        }).start();
    }

    /**
     * 获取app版本
     */
    public static void getApkVersion(Context context, String updataString, UpdataCallback updataCallback) throws Exception{
        UpdateInfo updateInfo = JSON.parseObject(updataString, UpdateInfo.class);
        UpdateInfoService.getInstance().getUpDateInfo(context, updateInfo, updataCallback);
    }


    /*
     * 此代码放入mainactivity中 加入安装未知应用权限
     * */
  /*  private final static int INSTALL_PACKAGES_REQUESTCODE = 0x12;//安装未知应用的权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGES_REQUESTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //installApk();
                    Log.e("结果", "开始安装");
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, INSTALL_PACKAGES_REQUESTCODE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == INSTALL_PACKAGES_REQUESTCODE) {
            checkIsAndroidO();
        }
    }

    *//**
     * 判断是否是8.0系统,是的话需要获取此权限，判断开没开，没开的话处理未知应用来源权限问题,否则直接安装
     *//*
    public void checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //PackageManager类中在Android Oreo版本中添加了一个方法：判断是否可以安装未知来源的应用
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (!b) {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
            }
        }
    }*/

}
