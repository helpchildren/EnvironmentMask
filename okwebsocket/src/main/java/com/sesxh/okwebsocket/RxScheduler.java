package com.sesxh.okwebsocket;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * @author LYH
 * @date 2021/1/5
 * @time 15:34
 * @desc RxJava线程调度
 */


public class RxScheduler {




    /**
     * 网络请求过程线程转换器，io 线程发射 ui 线程观察
     *
     * @param <T> 数据类型
     * @return 指定了在 io 线程执行，UI 线程观察结果的观察对象
     */
    public static <T> ObservableTransformer<T, T> sync() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }



}
