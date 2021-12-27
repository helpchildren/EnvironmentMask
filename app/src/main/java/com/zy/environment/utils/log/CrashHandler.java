package com.zy.environment.utils.log;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import com.elvishew.xlog.XLog.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by lfn on 2020/5/18
 * use : 应用异常处理类
 * 使用方式： 在Application 中初始化  CrashHandler.getInstance().init();
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    /**
     * 文件名
     */
    public static final String FILE_NAME = "crash";
    /**
     * 异常日志 存储位置为根目录下的 Crash文件夹
     */
    private static String PATH = Environment.getExternalStorageDirectory().getPath();
    /**
     * 文件名后缀
     */
    private static final String FILE_NAME_SUFFIX = ".txt";
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;


    private static volatile CrashHandler instance = null;
    public static CrashHandler getInstance(Context context){
        synchronized (CrashHandler.class) {
            if (instance == null) {
                instance = new CrashHandler(context);
            }
        }
        return instance;
    }

    public CrashHandler(Context context){
        mContext = context;
    }

    //记录保存文件及
    private String foldername = "sesxh/ToolCrash"; //记录保存文件夹
    public CrashHandler setFoldername(String foldername) {
        this.foldername = foldername;
        return this;
    }

    //是否设置崩溃重启 传参重启的class
    private boolean isRestart = false;
    private Class<?> cls;
    public CrashHandler setRestartActivity(Class<?> cls) {
        isRestart = true;
        this.cls = cls;
        return this;
    }

    /**
     * 初始化
     */
    public void init() {
        PATH = PATH + File.separator + foldername + File.separator;
        //得到系统的应用异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前应用异常处理器改为默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    /**
     * 这个是最关键的函数，当系统中有未被捕获的异常，系统将会自动调用 uncaughtException 方法
     *
     * @param thread 为出现未捕获异常的线程
     * @param ex     为未捕获的异常 ，可以通过e 拿到异常信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        //导入异常信息到SD卡中
        try {
            dumpExceptionToSDCard(ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //这里可以上传异常信息到服务器，便于开发人员分析日志从而解决Bug
//        uploadExceptionToServer();
        ex.printStackTrace();
        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (!isRestart && mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
//            Process.killProcess(Process.myPid());
            killProcess();
        }

    }

    /**
     * 退出应用并重启
     */
    private void killProcess() {
        try {

            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.e("application", "error : ", e);
        }
        // 退出程序
        Intent intent = new Intent(mContext,cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restartIntent); // 2秒钟后重启应用
        Process.killProcess(Process.myPid());
        System.exit(1);
    }



    /**
     * 将异常信息写入SD卡
     *
     * @param e
     */
    private void dumpExceptionToSDCard(Throwable e) throws IOException {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                //捕获到异常，弹出提示
                Toast.makeText(mContext, "很抱歉，应用遇到问题崩溃，即将重启！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //如果SD卡不存在或无法使用，则无法将异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "sdcard unmounted,skip dump exception");
            return;
        }
        File dir = new File(PATH);
        //如果目录下没有文件夹，就创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //得到当前年月日时分秒
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //在定义的Crash文件夹下创建文件
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //写入时间
            pw.println(time);
            //写入手机信息
            dumpPhoneInfo(pw);
            pw.println();//换行
            e.printStackTrace(pw);
            pw.close();//关闭输入流
        } catch (Exception e1) {
            Log.e(TAG, "dump crash info failed");
        }

    }

    /**
     * 获取手机各项信息
     *
     * @param pw
     */
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        //得到包管理器
        PackageManager pm = mContext.getPackageManager();
        //得到包对象
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        //写入APP版本号
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print("_");
        pw.println(pi.versionCode);
        //写入 Android 版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
        //CPU架构
        pw.print("CPU ABI: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.println(Build.SUPPORTED_ABIS);
        } else {
            pw.println(Build.CPU_ABI);
        }
    }

    /**
     * 将错误信息上传至服务器
     */
    private void uploadExceptionToServer() {

    }
}
