package com.zy.environment.utils;

import android.os.CountDownTimer;

/**
 * @desc 倒计时封装
 */

public class CountDownTimerUtil {

    private final static long ONE_SECOND = 1000;

    /**
     * 总倒计时时间
     */
    private long mMillisInFuture = 0;

    /**
     * 定期回调的时间 必须大于0 否则会出现ANR
     */
    private long mCountDownInterval;

    /**
     * 当前时间
     */
    private static long mCurrentTime;

    /**
     * 回调
     */
    private Delegate mDelegate;
    private MyCountDownTimer mCountDownTimer;

    /**
     * 获取 CountDownTimerUtils
     * @return CountDownTimerUtils
     */
    public static CountDownTimerUtil getCountDownTimer() {
        return new CountDownTimerUtil();
    }

    /**
     * 设置定期回调的时间 调用{@link #setDelegate(Delegate)}
     * @param pCountDownInterval 定期回调的时间 必须大于0
     * @return CountDownTimerUtils
     */
    public CountDownTimerUtil setCountDownInterval(long pCountDownInterval) {
        this.mCountDownInterval=pCountDownInterval;
        return this;
    }



    /**
     * 设置总倒计时时间
     * @param pMillisInFuture 总倒计时时间
     * @return CountDownTimerUtils
     */
    public CountDownTimerUtil setMillisInFuture(long pMillisInFuture) {
        this.mMillisInFuture=pMillisInFuture;
        return this;
    }

    /**
     * 设置定期回调
     * @param pTickDelegate 定期回调接口
     * @return CountDownTimerUtils
     */
    public CountDownTimerUtil setDelegate(Delegate pTickDelegate) {
        this.mDelegate=pTickDelegate;
        return this;
    }

    public CountDownTimerUtil create() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        if (mCountDownInterval <= 0) {
            mCountDownInterval = mMillisInFuture + ONE_SECOND;
        }
        mCountDownTimer = new MyCountDownTimer(mMillisInFuture, mCountDownInterval);
        mCountDownTimer.setDelegate(mDelegate);
        return this;
    }

    /**
     * 开始倒计时
     */
    public void start() {
        if (mCountDownTimer == null) {
            create();
        }
        mCountDownTimer.start();
    }




    /**
     * 取消倒计时
     */
    public void cancel() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private static class MyCountDownTimer extends CountDownTimer {
        private Delegate mDelegate;
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            if (mDelegate != null) {
                mCurrentTime=millisUntilFinished;
                mDelegate.onTick(millisUntilFinished);
            }
        }
        @Override
        public void onFinish() {
            if (mDelegate != null) {
                mDelegate.onFinish();
            }
        }

        void setDelegate(Delegate delegate) {
            this.mDelegate =delegate;
        }

    }

    /**
     * 定期回调的接口
     */
    public interface Delegate {
        void onTick(long pMillisUntilFinished);
        void onFinish();
    }

    public static long getmCurrentTime() {
        return mCurrentTime;
    }
}
