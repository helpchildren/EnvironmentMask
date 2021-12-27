package com.zy.environment.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.zy.environment.utils.EventBusUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {
    private BaseApplication application;
    public Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        if (application == null) {
            // 得到Application对象
            application = (BaseApplication) getApplication();
        }
        application.addActivity_(activity);

        fullScreenAndLight();//全屏常量
        hideBottomUIMenu();//隐藏按键
        steepStatusBar();//隐藏状态栏
        EventBusUtils.register(this);
    }



    //销毁当个Activity方法
    public void removeActivity() {
        application.removeActivity_(activity);
    }

    //销毁所有Activity方法
    public void removeALLActivity() {
        application.removeALLActivity_();
    }

    /*
     * 全屏常亮
     * */
    protected void fullScreenAndLight() {
        //将activity设置为全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*
     * 隐藏虚拟按键
     * */
    protected void hideBottomUIMenu() {
        Window _window = getWindow();
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
        _window.setAttributes(params);
    }

    /**
     * [沉浸状态栏]
     */
    private void steepStatusBar() {
        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

}
