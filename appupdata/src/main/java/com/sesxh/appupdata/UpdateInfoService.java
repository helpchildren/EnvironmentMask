package com.sesxh.appupdata;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sesxh.appupdata.bean.UpdateInfo;
import com.sesxh.appupdata.callback.UpdataCallback;
import com.sesxh.appupdata.utils.FileProgressDialog;
import com.sesxh.appupdata.utils.PackageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class UpdateInfoService {
    FileProgressDialog progressDialog;

    UpdataCallback updataCallback;
    String apkname = "update_apk.apk";

    private static final String TAG = "UpdateInfoService";

    private Context context;
    private static UpdateInfoService updateInfoService;

    public static UpdateInfoService getInstance(){
        if (updateInfoService == null){
            updateInfoService = new UpdateInfoService();
        }
        return updateInfoService;
    }

    public UpdateInfoService() {
    }


    public void setApkname(String apkname) {
        this.apkname = apkname;
    }

    public void getUpDateInfo(Context context, final UpdateInfo updateInfo, final UpdataCallback updataCallback){
        this.context = context;
        this.updataCallback = updataCallback;
        isNeedUpdate(updateInfo);
    }


    private void isNeedUpdate(final UpdateInfo updateInfo) {
        String up_version = updateInfo.getVersionName(); // 服务器最新版本的版本名
        //获取当前版本号
        String now_version = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            now_version = packageInfo.versionName;
            Log.d(TAG, "now_version=== " + now_version);
            Log.d(TAG, "up_version=== " + up_version);
        } catch (NameNotFoundException e) {
            updataCallback.onError("版本号获取失败");
            return;
        }

//		判断：只有当当前版本号小于最新版本时才更新！
        if (now_version !=null && !now_version.equals(up_version)) {
            //如果需要更新增获取apk更新大小
            new Thread(new Runnable(){
                @Override
                public void run() {
                    URL u = null;
                    HttpURLConnection urlcon = null;
                    try {
                        u = new URL(updateInfo.getApkurl());
                        urlcon = (HttpURLConnection) u.openConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                        updataCallback.onError("读取更新apk失败："+e);
                        return;
                    }
                    int fileLength =  urlcon.getContentLength();
                    DecimalFormat dFormat = new DecimalFormat("#.00");
                    String yearString = dFormat.format(((int) fileLength) / (double) (1024 * 1024));
                    updateInfo.setSize(Double.valueOf(yearString));
                    //获取成功后回调
                    updataCallback.onUpdate(true, updateInfo);
                }
            }).start();
        } else {
            updataCallback.onUpdate(false, updateInfo);
        }
    }

    private static boolean checkup(String oldversion, String newversion) {
        boolean isok = false;
        boolean normal = false;
        int length = 0;
        String[] oldbb = (oldversion.replace(".", ",")).split(",");
        String[] newbb = (newversion.replace(".", ",")).split(",");
        if (oldbb.length < newbb.length) {
            length = oldbb.length;
            normal = true;
        } else {
            length = newbb.length;
        }

        for (int i = 0; i < length; i++) {
            if (Integer.parseInt(oldbb[i]) < Integer.parseInt(newbb[i])) {
                isok = true;
                break;
            } else if (Integer.parseInt(oldbb[i]) == Integer.parseInt(newbb[i])) {
                if (i == length - 1) {
                    if (normal) {
                        for (int j = length; j < newbb.length; j++) {
                            if (Integer.parseInt(newbb[j]) > 0) {
                                isok = true;
                                break;
                            } else {
                                isok = false;
                            }
                        }
                    } else {
                        isok = false;
                    }
                }
            } else {
                isok = false;
                break;
            }
        }
        return isok;
    }


    public void downLoadFile(final String url) {
        progressDialog = new FileProgressDialog(context);    //进度条，在下载的时候实时更新进度，提高用户友好度
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("正在下载");
        progressDialog.setMessage("请稍候...");
        progressDialog.setProgress(0);
        progressDialog.show();

        new Thread() {
            public void run() {
                URL u = null;
                try {
                    u = new URL(url);
                    HttpURLConnection urlcon = null;
                    urlcon = (HttpURLConnection) u.openConnection();
                    int length =  urlcon.getContentLength();//获取文件大小
                    progressDialog.setMax(length);                 //设置进度条的总长度
                    InputStream is = urlcon.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                apkname);
                        fileOutputStream = new FileOutputStream(file);
                        //这个是缓冲区，即一次读取10个比特，我弄的小了点，因为在本地，所以数值太大一下就下载完了,
                        //看不出progressbar的效果。
                        byte[] buf = new byte[512];//改为512bit就快多了
                        int ch = -1;
                        int process = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            process += ch;
//                            Log.e(TAG, "run: downLoadFile====process===============" + process);
                            progressDialog.setProgress(process);       //这里就是关键的实时更新进度了！
                        }
                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    down();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void down() {
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                progressDialog.cancel();
                update();
            }
        });
    }

    private void update() {
        String filetype = getFormatName(apkname);
        if ("apk".equals(filetype)){
            File apkfile = new File(Environment.getExternalStorageDirectory(), apkname);
            if (!apkfile.exists()) {
                return;
            }
            PackageUtils.install(context, apkfile.getAbsolutePath());
        }
    }

    /**
     * 获取文件格式名
     */
    public static String getFormatName(String fileName) {
        //去掉首尾的空格
        fileName = fileName.trim();
        String s[] = fileName.split("\\.");
        if (s.length >= 2) {
            return s[s.length - 1];
        }
        return "";
    }

    public void Filedownload(String url) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        // 设置下载路径和文件名
        request.setDestinationInExternalPublicDir(path, apkname);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        // 设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();
        // 设置为可见和可管理
        request.setVisibleInDownloadsUi(true);
        long refernece =dManager.enqueue(request);
        SharedPreferences sPreferences = context.getSharedPreferences("downloadcomplete", 0);
        sPreferences.edit().putLong("refernece", refernece).putString("filename",apkname).commit();
    }

}
