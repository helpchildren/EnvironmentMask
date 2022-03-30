package com.zy.environment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lake.banner.BannerConfig;
import com.lake.banner.BannerStyle;
import com.lake.banner.HBanner;
import com.lake.banner.ImageGravityType;
import com.lake.banner.Transformer;
import com.lake.banner.VideoGravityType;
import com.lake.banner.loader.ViewItemBean;
import com.sesxh.appupdata.UpdataController;
import com.sesxh.appupdata.UpdateInfoService;
import com.sesxh.appupdata.bean.UpdateInfo;
import com.sesxh.appupdata.callback.UpdataCallback;
import com.sesxh.okwebsocket.OkWebSocket;
import com.sesxh.okwebsocket.WebSocketInfo;
import com.sesxh.okwebsocket.annotation.WebSocketStatus;
import com.sesxh.rxpermissions.RxPermissions;
import com.zy.environment.base.BaseActivity;
import com.zy.environment.bean.AdvBean;
import com.zy.environment.bean.MsgBean;
import com.zy.environment.bean.MsgType;
import com.zy.environment.config.GlobalSetting;
import com.zy.environment.machine.MachineController;
import com.zy.environment.utils.DownloadUtil;
import com.zy.environment.utils.FileStorage;
import com.zy.environment.utils.FylToast;
import com.zy.environment.utils.ToolsUtils;
import com.zy.environment.utils.Validate;
import com.zy.environment.utils.log.Logger;
import com.zy.environment.widget.DialogUtils;
import com.zy.machine.OnDataListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.lake.banner.BannerConfig.IMAGE;
import static com.lake.banner.BannerConfig.VIDEO;


public class MainActivity extends BaseActivity {

    private static final String TAG="MainActivity";

    private ImageView ivScanCode;
    private TextView tvDeviceid;
    private TextView tvVersion;

    private final Gson gson = new Gson();
    private MachineController machineController;//硬件连接
    private String order_sn = "";
    private String order_type = "";

    private List<AdvBean> mAdvList = new ArrayList<>();//本地广告列表
    private HBanner banner;//轮播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();//权限申请
        findViewById();
        initView();
        initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        banner.onResume();
    }

    @Override
    protected void onPause() {
        banner.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        banner.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkWebSocket.closeAllNow();
        if (machineController != null)
            machineController.destroy();
        DialogUtils.getInstance().releaseDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerEvent(String event) {
        if("testBag".equals(event)){
            Logger.d("MainActivity", "测试出袋子");
            if (machineController != null){
                machineController.setOutBagLength(GlobalSetting.outLenBag);
                machineController.outGoods("0");
            }
        }else if("testMask".equals(event)){
            Logger.d("MainActivity", "测试出口罩");
            if (machineController != null){
                machineController.setOutMaskLength(GlobalSetting.outLenMask);
                machineController.outGoods("1");
            }
        }else {
            Logger.d("MainActivity", "应用刷新");
            OkWebSocket.closeAllNow();
            if (machineController != null)
                machineController.destroy();
            initData();//刷新连接
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        GlobalSetting.deviceid = ToolsUtils.getDeviceId(activity);
        String versionName = ToolsUtils.getVersionName(activity);
        Logger.d("MainActivity", "应用启动 版本号："+versionName+" 设备号："+ GlobalSetting.deviceid);
        tvVersion.setText("版本号：v" + versionName);
        tvDeviceid.setText("设备号："+ GlobalSetting.deviceid);
        tvDeviceid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolsUtils.continuousClick(activity);//进入设置页面
            }
        });
        //轮播初始化
        List<ViewItemBean> list = new ArrayList<>();
        banner.setViews(list)
                .setBannerAnimation(Transformer.Default)//换场方式
                .setBannerStyle(BannerStyle.CIRCLE_INDICATOR_TITLE)//指示器模式
                .setCache(false)//可以不用设置，默认为true
//                .setCachePath(GlobalSetting.AdvPath + File.separator + "hbanner")
                .setVideoGravity(VideoGravityType.CENTER)//视频布局方式
                .setImageGravity(ImageGravityType.FIT_XY)//图片布局方式
                .setPageBackgroundColor(Color.BLACK)//设置背景
                .setShowTitle(false)//是否显示标题
                .setViewPagerIsScroll(false)//是否支持手滑
                .start();

        //本地广告获取
        String advJson = FileStorage.getFileText(GlobalSetting.AdvPath, GlobalSetting.AdFile);
        if (Validate.noNull(advJson)){
            Type listType = new TypeToken<List<AdvBean>>() {}.getType();
            mAdvList = gson.fromJson(advJson, listType);
            Logger.d(TAG,"本地广告列表获取 mAdvList："+ Arrays.toString(mAdvList.toArray()));
        }else {
            mAdvList = new ArrayList<>();
        }
        updateAdv();
    }

    private void initData() {
        GlobalSetting.getSetting(activity);
        websocketConnect();
        //获取硬件控制
        machineController = new MachineController();
        machineController.init(mListener);
    }

    /*
    * 设备登录
    * */
    private void deviceLogin(){
        MsgBean msgBean = new MsgBean("login");
        socketSend(msgBean);
    }

    /*
    * 定时拉取广告
    * */
    @SuppressLint("CheckResult")
    private void getAdv(){
        //定时获取广告信息5分钟一次
        Flowable.interval(1, 5, TimeUnit.MINUTES).takeWhile(aLong -> isSocketConn).subscribe(aLong -> {
            Logger.i(TAG,"定时任务 拉取广告："+aLong);
            MsgBean msgBean = new MsgBean("qrcode");
            socketSend(msgBean);
        });
    }

    /*
     * 定时发送心跳
     * */
    @SuppressLint("CheckResult")
    private void putHeart(){
        //定时获取广告信息5分钟一次
        Flowable.interval(5, 20, TimeUnit.SECONDS).takeWhile(aLong -> isSocketConn).subscribe(aLong -> {
            MsgBean msgBean = new MsgBean("heartbeat");
            socketSend(msgBean);
        });
    }

    private void socketSend(MsgBean msgBean){
        if (isSocketConn){
            Logger.d(TAG, "客户端发送消息：" + gson.toJson(msgBean));
            OkWebSocket.send(GlobalSetting.wsurl, gson.toJson(msgBean)).subscribe(new Observer<Boolean>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) { }

                @Override
                public void onNext(@NonNull Boolean aBoolean) { }

                @Override
                public void onError(@NonNull Throwable e) { }

                @Override
                public void onComplete() { }
            });
        }else {
            Logger.d(TAG, "客户端发送消息失败：socket断开 type:"+msgBean.getType());
        }
    }

    /*
     * 连接服务器
     * */
    private boolean isSocketConn = false;
    @SuppressLint("CheckResult")
    private void websocketConnect() {
        OkWebSocket.get(GlobalSetting.wsurl).subscribe(new Consumer<WebSocketInfo>() {
            @Override
            public void accept(WebSocketInfo webSocketInfo) throws Exception {
                Logger.d(TAG, "客户端收到消息：" + webSocketInfo.toString());
                switch(webSocketInfo.getStatus()){
                    case WebSocketStatus.STATUS_CONNECTED://连接成功
                    case WebSocketStatus.STATUS_RE_CONNECTED://重连成功
                        isSocketConn = true;
                        DialogUtils.getInstance().closeErrDialog();
                        deviceLogin();
                        getAdv();
                        putHeart();
                        showText("服务器连接成功");
                        break;
                    case WebSocketStatus.STATUS_ON_CLOSED://关闭
                        isSocketConn = false;
                        break;
                    case WebSocketStatus.STATUS_ON_FAILURE://连接异常
                        isSocketConn = false;
                        showText("服务器连接失败，等待重连中...", true);
                        break;
                    case WebSocketStatus.STATUS_ON_REPLY://收到服务端消息
                        Type listType = new TypeToken<MsgBean>() {}.getType();
                        try {
                            MsgBean msgBean = gson.fromJson(webSocketInfo.getStringMsg(), listType);
                            cmdHandle(msgBean);
                        }catch (Exception e){
                            showText("服务器参数错误");
                        }
                        break;
                    default:
                        break;
                }

            }
        });
    }

    /*
    * 解析服务器指令
    * */
    private void cmdHandle(MsgBean msgBean){
        switch(msgBean.getType()){
            case MsgType.TYPE_OUT://出货
                order_sn = msgBean.getOrder_sn();
                order_type = msgBean.getOrder_type();
                //调用硬件部分
                machineController.outGoods(order_type);
                break;
            case MsgType.TYPE_HEART://心跳

                break;
            case MsgType.TYPE_LOGIN://登录
                updateQRcode(msgBean.getQrcode_url());
                //获取版本更新信息
                getAppUpDataInfo();
                break;
            case MsgType.TYPE_OUTBACK://出货回调

                break;
            case MsgType.TYPE_QRMSG://二维码广告
                updateQRcode(msgBean.getQrcode_url());//更新二维码
                comparisonList(msgBean.getAdv());//合并广告
                break;
            case MsgType.TYPE_UPLOG://拉取本地log
                uploadLog(msgBean.getMsg());
                break;
            case MsgType.TYPE_OTHER://其他
                showText("消息提示："+msgBean.getMsg());
                break;
            default:

                break;

        }
    }

    /*
    * 设备控制回调
    * */
    private final OnDataListener mListener =
            new OnDataListener() {

                @Override
                public void onError(int errcode,String err) {
                    Logger.d(TAG,"onError:"+err);
                    if (Validate.noNull(order_sn)){
                        MsgBean msgBean = new MsgBean("back");
                        msgBean.setOrder_sn(order_sn);
                        msgBean.setOrder_type(order_type);
                        msgBean.setResult("2");
                        order_sn = "";
                        socketSend(msgBean);
                    }
                    if (errcode == 1000 || errcode == 1001 || errcode == 1003){
                        showText(err+",请联系管理员。", true);
                    }else {
                        showText(err);
                    }
                }

                @Override
                public void onStart(int type) {
                    Logger.d(TAG, "开始出货-出货类型："+type);
                }

                @Override
                public void onSuccess() {
                    Logger.d(TAG,"出货成功");
                    showText("出货成功");
                    MsgBean msgBean = new MsgBean("back");
                    msgBean.setOrder_sn(order_sn);
                    msgBean.setOrder_type(order_type);
                    msgBean.setResult("1");
                    socketSend(msgBean);

                }
            };




    private int downCount = 0;//下载计数
    /*
    * 广告列表下载比对
     * */
    private void comparisonList(List<AdvBean> advList){
        //先判断两个列表是否相同
        if (!ToolsUtils.isListEqual(mAdvList, advList)){
            //不相同开始处理
            // 先去重
            mAdvList.removeAll(advList);
            List<AdvBean> delList = new ArrayList<>(mAdvList);
            if (delList.size() > 0){
                //有多余的就删除
                for (int i = 0; i < delList.size(); i++) {
                    FileStorage.delete(GlobalSetting.AdvPath +File.separator+ delList.get(i).getDirName());
                }
            }
            //重新赋值
            mAdvList = new ArrayList<>(advList);
            //统计新增得广告并下载
            List<AdvBean> downAdvList = new ArrayList<>();//广告临时表
            for (int i = 0; i < mAdvList.size(); i++) {
                AdvBean advBean = mAdvList.get(i);
                String dirName;
                if (advBean.isVideo()){
                    dirName = "Video_"+advBean.getId()+ DownloadUtil.VIDEO;
                }else {
                    dirName = "Image_"+advBean.getId()+ DownloadUtil.IMAGE;
                }
                advBean.setDirName(dirName);
                if (!DownloadUtil.fileIsExists(dirName, GlobalSetting.AdvPath, advBean.isVideo()? DownloadUtil.VIDEO: DownloadUtil.IMAGE)) {
                    downAdvList.add(advBean);//未下载的先记录
                }
            }

            if (downAdvList.size() > 0){
                downCount = 0;
                for (int i = 0; i < downAdvList.size(); i++) {
                    AdvBean advBean = downAdvList.get(i);
                    Logger.d(TAG, "准备下载:"+advBean.getId()+" "+downCount);
                    //文件不存在则下载
                    DownloadUtil.get().download(advBean.getUrl(), GlobalSetting.AdvPath, advBean.getDirName(), new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(File file, String fileName) {
                            downCount++;
                            Logger.d(TAG, "下载成功:"+fileName+" "+downCount);
                            if (downCount == downAdvList.size()){
                                composeList();
                            }
                        }

                        @Override
                        public void onDownloading(int progress) {
                            // 进度条
                        }

                        @Override
                        public void onDownloadFailed(Exception e, String fileName) {
                            e.printStackTrace();
                            downCount++;
                            Logger.d(TAG, "下载失败:"+e.getMessage()+" "+downCount);
                            AdvBean faildAdvBean = getFaildAdvBean(downAdvList, fileName);
                            if (faildAdvBean != null) {
                                mAdvList.remove(faildAdvBean);
                            }
                            if (downCount == downAdvList.size()){
                                composeList();
                            }
                        }
                    });

                }
            }else {
                composeList();
            }
        }
    }

    private AdvBean getFaildAdvBean(List<AdvBean> downAdvList, String fileName){
        if (downAdvList!=null && downAdvList.size()>0){
            for (int i = 0; i < downAdvList.size(); i++) {
                if (fileName.equals(downAdvList.get(i).getDirName())){
                    return downAdvList.get(i);
                }
            }
        }
        return null;
    }

    /*
    * 组合List
    * */
    private void composeList(){
        Logger.e("lfntest","下载完成后的 mAdvList："+ Arrays.toString(mAdvList.toArray()));
        //缓存到本地
        FileStorage.saveToFile(GlobalSetting.AdvPath, GlobalSetting.AdFile, gson.toJson(mAdvList));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateAdv();
            }
        });
    }

    /*
    * 更新广告
    * */
    private void updateAdv() {
        if (mAdvList != null && mAdvList.size()>0 ){
            List<ViewItemBean> bannerList = new ArrayList<>();
            for (int i = 0; i < mAdvList.size(); i++) {
                AdvBean advBean = mAdvList.get(i);
//                Uri uri = ToolsUtils.getUriForFileName(advBean.getDirName());
                if (advBean.isVideo()){
                    int time = ToolsUtils.getVideoTime(advBean.getDirPath());
                    time = time !=0 ? time: BannerConfig.TIME;
                    bannerList.add(new ViewItemBean(VIDEO, advBean.getScreen_name(), advBean.getDirPath(), time));
                }else {
                    bannerList.add(new ViewItemBean(IMAGE, advBean.getScreen_name(), advBean.getDirPath(), BannerConfig.TIME));
                }
            }
            banner.update(bannerList);
            banner.setVisibility(View.VISIBLE);
        }else {
            banner.onPause();
            banner.setVisibility(View.GONE);
        }
    }

    /*
    * 更新二维码
    * */
    private void updateQRcode(String qrcode){
        Glide.with(activity)
                .load(qrcode)
                .centerCrop()
                .into(ivScanCode);
    }

    /*
    * 获取apk更新
    * */
    private void getAppUpDataInfo() {
        UpdataController.getUpDataInfo(activity, new UpdataCallback() {
            @Override
            public void onUpdate(boolean isNeed, UpdateInfo info) {
                Logger.d("lfntest", "获取更新信息成功：" + info.toString());
                if (isNeed){//判断是否需要更新
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateInfoService.getInstance().downLoadFile(info.getApkurl());//前台下载
                        }
                    });
                }
            }

            @Override
            public void onError(String msg) {
                Logger.d("lfntest", "获取更新失败：" + msg);
            }
        });
    }

    /**
     * 创建线程实现文件的上传
     */
    public void uploadLog(String filename){
        File file = new File(GlobalSetting.externpath+"/zy/"+getPackageName()+"/ToolXLog/"+filename);
        // 如果文件路径所对应的文件存在，并且是一个文件 开始上传
        if (file.exists() && file.isFile()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DownloadUtil.get().upload(GlobalSetting.uploadUrl, file);
                    } catch (IOException e) {
                        Logger.d("uploadLog","日志上传失败"+e.toString());
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void showText(String msg){
        showText(msg, false);
    }

    private void showText(String msg, boolean isErr){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isErr){
                    Logger.d("ErrDialog", "showErrDialog:"+msg);
                    DialogUtils.getInstance().showErrDialog(activity,msg);
                }else {
                    FylToast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findViewById() {
        ivScanCode = (ImageView) findViewById(R.id.iv_scan_code);
        tvDeviceid = (TextView) findViewById(R.id.tv_deviceid);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        banner = (HBanner) findViewById(R.id.banner);
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    @SuppressLint("CheckResult")
    private void initPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe();
    }

}