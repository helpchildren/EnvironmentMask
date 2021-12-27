package com.zy.environment.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;


import com.zy.environment.R;
import com.zy.environment.utils.CountDownTimerUtil;
import com.zy.environment.utils.KeyboardUtils;
import com.zy.environment.utils.SizeUtils;

import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;


public class BaseDialog extends Dialog {
    public static int  dialogTimer = 30; //默认弹窗倒计时
    protected int millis = dialogTimer;

    private Context context;
    private int height = WindowManager.LayoutParams.WRAP_CONTENT, width = WindowManager.LayoutParams.WRAP_CONTENT, gravity = getGravity();
    private View view;
    private static int resStyle = R.style.dialog;
    private Handler mHandler = new Handler();
    private CountDownTimerUtil mTimer;
    private CountDownListener mCountDownListener;

    private final Runnable mAutoDelayed = new Runnable() {

        @Override
        public void run() {
            hideDialog();
        }
    };

    public BaseDialog(@NonNull Context context) {
        super(context, resStyle);
        this.context = context;
    }

    public BaseDialog(@NonNull Context context, @LayoutRes int resView) {
        this(context,resStyle,resView);
    }

    public BaseDialog(@NonNull Context context, View view) {
        this(context,resStyle,view);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId, @LayoutRes int resView) {
        super(context, themeResId);
        this.context = context;
        view(resView);
    }



    public BaseDialog(@NonNull Context context, @StyleRes int themeResId, View view) {
        super(context, themeResId);
        this.context = context;
        view(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();
        setContentView(view);
        setCanceledOnTouchOutside(getCanceledOnTouchOutside());
        setCancelable(getCancelable());
        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.gravity = gravity;
        lp.height = height;
        lp.width = width;
        win.setAttributes(lp);
        bindView(view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, event)) {//点击editText控件外部
                KeyboardUtils.hideSoftInput(context, v);
            }
            if(millis>0){
                cancelTimer();
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            if(millis>0){
                startCountDown(millis);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }


    public BaseDialog countDownListener(CountDownListener mCountDownListener) {
        this.mCountDownListener = mCountDownListener;
        return this;
    }

    public void showDialog() {
        showDialog(millis);
    }

    /*
     * 添加倒计时的Dialog 单位 秒
     * */
    public void showDialog(int hideDelay) {
        show();
        startCountDown(hideDelay);
//        autoHide(hideDelay);
    }

    public void autoHide(long hideDelay) {
        if (hideDelay > 0) {
            mHandler.postDelayed(mAutoDelayed, hideDelay *1000);
        }
    }

    protected void startCountDown(int millisInFuture) {
        millis=millisInFuture;
        if (millisInFuture > 0) {
            startTimer(millisInFuture);
        }
    }

    public void hideDialog() {
        mHandler.removeCallbacks(mAutoDelayed);
        dismiss();
    }


    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
    }

    private void startTimer(int millisInFuture) {
        cancelTimer();
        mTimer = CountDownTimerUtil.getCountDownTimer()
                .setMillisInFuture(millisInFuture * 1000 + 1999)
                .setCountDownInterval(1000)
                .setDelegate(new CountDownTimerUtil.Delegate() {
                    @Override
                    public void onTick(long pMillisUntilFinished) {
                        long time = pMillisUntilFinished / 1000 - 1;
                        if (time>0){
                            if (mCountDownListener != null) {
                                mCountDownListener.onTick(time);
                            }
                        }else {
                            onFinish();
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (mCountDownListener != null) {
                            mCountDownListener.onFinish();
                        }
                        cancelTimer();
                        hideDialog();
                    }
                }).create();
        mTimer.start();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mAutoDelayed);
        cancelTimer();
    }


    public BaseDialog view(int resView) {
        view = LayoutInflater.from(context).inflate(resView, null);
        return this;
    }

    public BaseDialog view(View view) {
        this.view = view;
        return this;
    }

    public BaseDialog height(int val) {
        if (val != ViewGroup.LayoutParams.MATCH_PARENT && val != ViewGroup.LayoutParams.WRAP_CONTENT)
            height = SizeUtils.dp2px(val);
        else
            height = val;
        return this;
    }

    public BaseDialog heightPx(int val) {
        height = val;
        return this;
    }

    public BaseDialog width(int val) {
        if (val != ViewGroup.LayoutParams.MATCH_PARENT && val != ViewGroup.LayoutParams.WRAP_CONTENT)
            width = SizeUtils.dp2px(val);
        else
            width = val;
        return this;
    }

    public BaseDialog widthPx(int val) {
        width = val;
        return this;
    }

    public BaseDialog heightDimen(@DimenRes int dimenRes) {
        height = context.getResources().getDimensionPixelOffset(dimenRes);
        return this;
    }

    public BaseDialog widthDimen(@DimenRes int dimenRes) {
        width = context.getResources().getDimensionPixelOffset(dimenRes);
        return this;
    }

    public  <T extends View> T  findViewById(@IdRes int id) {
        return view.findViewById(id);
    }

    public int getGravity() {
        return this.gravity;
    }

    protected void bindView(View v) {

    }


    public boolean getCanceledOnTouchOutside() {
        return false;
    }

    public boolean getCancelable() {
        return false;
    }

    public interface CountDownListener {
        void onTick(long time);

        void onFinish();
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
