package com.zy.environment.utils.log;

import android.os.Environment;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by lfn on 2020/5/18
 * use : 日志记录类
 * 使用方式： 在Application 中初始化  XLogUtils.getInstance().XlogInit();
 */
public class XLogUtils {

    private static volatile XLogUtils instance = null;
    public static XLogUtils getInstance(){
        synchronized (XLogUtils.class) {
            if (instance == null) {
                instance = new XLogUtils();
            }
        }
        return instance;
    }

    //记录保存文件及
    private String foldername = "sesxh/ToolXLog";
    public XLogUtils setFoldername(String foldername) {
        this.foldername = foldername;
        return this;
    }
    //是否存储到本地
    private boolean isSave = true;
    public XLogUtils setSave(boolean save) {
        this.isSave = save;
        return this;
    }
    //设置清除日期
    private int cleardate = 20;
    public XLogUtils setCleardate(int cleardate) {
        if (cleardate>20){
            cleardate = 20;
        }
        this.cleardate = cleardate;
        return this;
    }
    //是否开启debuglog
    private static boolean isDebugLog = true;
    public XLogUtils setDebugLog(boolean debugLog) {
        isDebugLog = debugLog;
        return this;
    }

    /*
    * xlog初始化
    * foldername 保存文件夹名
    * cleardate 指定清除间隔 单位 天
    * isSave 是否保存在本地
    * */
    public void XlogInit(){
        long MAX_TIME = cleardate * 24 * 3600 * 1000;
        String PATH_LOGCAT = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + foldername;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + foldername;
        }
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag("MY_TAG")                                         // 指定 TAG，默认为 "X-LOG"
                .t()                                                   // 允许打印线程信息，默认禁止
                .st(2)                                                 // 允许打印深度为2的调用栈信息，默认禁止
                .b()                                                   // 允许打印日志边框，默认禁止
                .build();
        Printer androidPrinter = new AndroidPrinter();             // 通过 android.util.Log 打印日志的打印器
//        Printer consolePrinter = new ConsolePrinter();               // 通过 System.out.println 打印日志的打印器
        Printer filePrinter;
        if (cleardate == 0){
            filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                    .Builder(PATH_LOGCAT)                              // 指定保存日志文件的路径
                    .fileNameGenerator(new DateFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                    .backupStrategy(new NeverBackupStrategy() )           // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
//                    .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // 指定日志文件清除策略，默认为 NeverCleanStrategy() 定期清除
                    .build();
        }else {
            filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                    .Builder(PATH_LOGCAT)                              // 指定保存日志文件的路径
                    .fileNameGenerator(new DateFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                    .backupStrategy(new NeverBackupStrategy() )           // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                    .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // 指定日志文件清除策略，默认为 NeverCleanStrategy() 定期清除
                    .build();
        }

        if (isSave){
            XLog.init(config,                                                // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
                    androidPrinter,                                        // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter(Android)/ConsolePrinter(java)
                    filePrinter);
        }else {
            XLog.init(config,                                                // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
                    androidPrinter);
        }

    }

    private static String nowtime(){
       SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
       return df.format(new Date());
    }


    public static void d(String tag, String msg){
        if (isDebugLog)
            XLog.Log.d(tag, nowtime()+" "+msg);
    }

    public static void d(String msg){
        if (isDebugLog)
            XLog.d(nowtime()+" "+msg);
    }

    public static void d(String msg, Exception e){
        if (isDebugLog){
            String exception = android.util.Log.getStackTraceString(e);
            XLog.d(nowtime()+" "+msg+" Exception："+exception);
        }

    }

    public static void i(String tag, String msg){
        XLog.Log.i(tag, nowtime()+" "+msg);
    }

    public static void i(String msg){
        XLog.i(nowtime()+" "+msg);
    }

    public static void e(String tag, String msg){
        XLog.Log.e(tag, nowtime()+" "+msg);
    }

    public static void e(String msg){
        XLog.e(nowtime()+" "+msg);
    }

    public static void e(String msg, Exception e){
        String exception = android.util.Log.getStackTraceString(e);
        XLog.e(nowtime()+" "+msg+" Exception："+exception);
    }


}
