package com.sesxh.appupdata.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sesxh.appupdata.UpdateInfoService;
import com.sesxh.appupdata.bean.UpdateInfo;

import androidx.annotation.NonNull;
import com.sesxh.appupdata.R;

public class UpdateDialog extends Dialog implements View .OnClickListener{
    private Context context;
    private UpdateInfo info;
    private TextView textView, tv_title, size, cancle;
    private Button sj;

    public UpdateDialog(@NonNull Context activity, UpdateInfo info) {
        super(activity, R.style.BaseDialog);
        hideBottomUIMenu();
        this.context = activity;
        this.info = info;
//        setCanceledOnTouchOutside(true); // 点击空白区域可以Dismiss对话框
        setCancelable(true); // 点击返回按键可以Dismiss对话框
        init();
    }

    @SuppressLint("SetTextI18n")
    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.update_items, null);
        setContentView(view);

         textView = (TextView) findViewById(R.id.tv_msg);
         tv_title = (TextView) findViewById(R.id.tv_title);
         size = (TextView) findViewById(R.id.size);
         cancle = (TextView) findViewById(R.id.cancle);
         sj = (Button) findViewById(R.id.sj);

        if (info.isForce()) {
            cancle.setVisibility(View.INVISIBLE);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        } else {
            cancle.setVisibility(View.VISIBLE);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        }
        size.setText("更新大小：" + info.getSize() + "M");
        tv_title.setText("v"+info.getVersionName() + "版本更新");
        textView.setText(info.getDescription());

        sj.setOnClickListener(this);
        cancle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sj) {
//            if (info.isUpdate()) {
//                UpdateInfoService.getInstance().downLoadFile(info.getUrl());//前台下载
//            } else {
//                UpdateInfoService.getInstance().Filedownload(info.getUrl());//后台静默下载
//            }
            UpdateInfoService.getInstance().downLoadFile(info.getApkurl());//前台下载
        } else if (id == R.id.cancle) {
            this.dismiss();
        } else {
            this.dismiss();
        }
        this.dismiss();
    }

    //隐藏虚拟按键
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            Window _window = getWindow();
            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);
        }
    }

}
