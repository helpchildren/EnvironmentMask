package com.sesxh.okwebsocket;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;

import com.sesxh.okwebsocket.annotation.WebSocketStatus;
import com.sesxh.okwebsocket.cache.WebSocketInfoPool;
import com.sesxh.okwebsocket.config.Config;
import com.sesxh.okwebsocket.log.Logger;
import com.sesxh.okwebsocket.ssl.OkX509TrustManager;
import com.sesxh.okwebsocket.ssl.SSLManager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:45
 * @desc WebSocket实现类
 **/
public class OkWebSocketAPI implements IOkWebSocketAPI {
    private static final String TAG = OkWebSocketAPI.class.getName();

    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 支持外部传入OkHttpClient
     */
    private OkHttpClient mClient;
    /**
     * 是否需要重连
     */
    private boolean mIsAutoReconnect;
    /**
     * 重连间隔时间
     */
    private long mReconnectInterval;
    /**
     * 重连间隔时间的单位
     */
    private TimeUnit mReconnectIntervalTimeUnit;

    /**
     * 缓存观察者对象，Url对应一个Observable
     */
    private Map<String, Observable<WebSocketInfo>> mObservableCacheMap;
    /**
     * 缓存Url和对应的WebSocket实例，同一个Url共享一个WebSocket连接
     */
    private Map<String, WebSocket> mWebSocketPool;
    /**
     * WebSocketInfo缓存池
     */
    private final WebSocketInfoPool mWebSocketInfoPool;

    OkWebSocketAPI(Config config) {
        this.mContext = config.getContext();
        this.mClient =config.getClient();
        if(mClient==null){
            initOkHttpClient(config);
        }
        Logger.sDebug=config.isDebug();
        //重试时间配置
        this.mIsAutoReconnect=config.isAutoReconnect();
        if(mIsAutoReconnect) {
            this.mReconnectInterval = config.getReconnectInterval();
            this.mReconnectIntervalTimeUnit = config.getReconnectIntervalTimeUnit();
        }
        this.mObservableCacheMap = new HashMap<>(16);
        this.mWebSocketPool = new HashMap<>(16);
        mWebSocketInfoPool = new WebSocketInfoPool();
    }

    @Override
    public Observable<WebSocketInfo> get(String url) {
        return getWebSocketInfo(url);
    }

    @Override
    public Observable<Boolean> send(String url, String msg) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                WebSocket webSocket = mWebSocketPool.get(url);
                if (webSocket == null) {
                    emitter.onError(new IllegalStateException("The WebSocket not open"));
                } else {
                    emitter.onNext(webSocket.send(msg));
                }
            }
        }).compose(RxScheduler.sync());
    }

    @Override
    public Observable<Boolean> send(String url, ByteString byteString) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                WebSocket webSocket = mWebSocketPool.get(url);
                if (webSocket == null) {
                    emitter.onError(new IllegalStateException("The WebSocket not open"));
                } else {
                    emitter.onNext(webSocket.send(byteString));
                }
            }
        }).compose(RxScheduler.sync());
    }

    @Override
    public Observable<Boolean> asyncSend(String url, String msg) {
        return getWebSocket(url)
                .take(1)
                .map(new Function<WebSocket, Boolean>() {
                    @Override
                    public Boolean apply(WebSocket webSocket) throws Exception {
                        return webSocket.send(msg);
                    }
                }).compose(RxScheduler.sync());
    }

    @Override
    public Observable<Boolean> asyncSend(String url, ByteString byteString) {
        return getWebSocket(url)
                .take(1)
                .map(new Function<WebSocket, Boolean>() {
                    @Override
                    public Boolean apply(WebSocket webSocket) throws Exception {
                        return webSocket.send(byteString);
                    }
                }).compose(RxScheduler.sync());
    }

    @Override
    public Observable<Boolean> heartBeat(String url, int period, TimeUnit unit,
                                         HeartBeatGenerateCallback heartBeatGenerateCallback) {
        if (heartBeatGenerateCallback == null) {
            return Observable.error(new NullPointerException("heartBeatGenerateCallback == null"));
        }
        return Observable
                .interval(period, unit)
                .retry()
                .flatMap(new Function<Long, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(@NonNull Long aLong) throws Exception {
                        if (mContext != null && NetworkUtil.hasNetWorkStatus(mContext, false)) {
                            String heartBeatMsg = heartBeatGenerateCallback.onGenerateHeartBeatMsg();
                            Logger.d(TAG, "发送心跳消息: " + heartBeatMsg);
                            if (hasWebSocketConnection(url)) {
                                return send(url, heartBeatMsg);
                            } else {
                                //这里必须用异步发送，如果切断网络，再重连，缓存的WebSocket会被清除，此时再重连网络
                                //是没有WebSocket连接可用的，所以就需要异步连接完成后，再发送
                                return asyncSend(url, heartBeatMsg);
                            }
                        } else {
                            Logger.d(TAG, "无网络连接，不发送心跳，下次网络连通时，再次发送心跳");
                            return Observable.create(new ObservableOnSubscribe<Boolean>() {
                                @Override
                                public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                    emitter.onNext(false);
                                }
                            });
                        }
                    }
                });

    }

    @Override
    public Observable<Boolean> close(String url) {
        return Observable.create(new ObservableOnSubscribe<WebSocket>() {
            @Override
            public void subscribe(ObservableEmitter<WebSocket> emitter) throws Exception {
                WebSocket webSocket = mWebSocketPool.get(url);
                if (webSocket == null) {
                    emitter.onError(new NullPointerException("url:" + url + " WebSocket must be not null"));
                } else {
                    emitter.onNext(webSocket);
                }
            }
        }).map(new Function<WebSocket, Boolean>() {
            @Override
            public Boolean apply(WebSocket webSocket) throws Exception {
                return closeWebSocket(webSocket);
            }
        }).compose(RxScheduler.sync());
    }

    @Override
    public boolean closeNow(String url) {
        return closeWebSocket(mWebSocketPool.get(url));
    }

    @SuppressWarnings("unChecked")
    @Override
    public Observable<List<Boolean>> closeAll() {
        return Observable
                .just(mWebSocketPool)
                .map(new Function<Map<String, WebSocket>, Collection<WebSocket>>() {
                    @Override
                    public Collection<WebSocket> apply(Map<String, WebSocket> webSocketMap) throws Exception {
                        return webSocketMap.values();
                    }
                })
                .concatMap(new Function<Collection<WebSocket>, ObservableSource<WebSocket>>() {
                    @Override
                    public ObservableSource<WebSocket> apply(Collection<WebSocket> webSockets) throws Exception {
                        return Observable.fromIterable(webSockets);
                    }
                }).map(new Function<WebSocket, Boolean>() {
                    @Override
                    public Boolean apply(WebSocket webSocket) throws Exception {
                        return closeWebSocket(webSocket);
                    }
                }).collect(new Callable<List<Boolean>>() {
                    @Override
                    public List<Boolean> call() throws Exception {
                        return new ArrayList<>();
                    }
                }, new BiConsumer<List<Boolean>, Boolean>() {
                    @Override
                    public void accept(List<Boolean> list, Boolean isCloseSuccess) throws Exception {
                        list.add(isCloseSuccess);
                    }
                }).toObservable().compose(RxScheduler.sync());
    }

    @Override
    public void closeAllNow() {
        for (Map.Entry<String, WebSocket> entry : mWebSocketPool.entrySet()) {
            closeWebSocket(entry.getValue());
        }
    }

    /**
     * 是否有连接
     */
    private boolean hasWebSocketConnection(String url) {
        return mWebSocketPool.get(url) != null;
    }

    /**
     * 关闭WebSocket连接
     */
    private boolean closeWebSocket(WebSocket webSocket) {
        if (webSocket == null) {
            return false;
        }
        WebSocketCloseEnum normalCloseEnum = WebSocketCloseEnum.ACTIVE_CLOSE;
        boolean result = webSocket.close(normalCloseEnum.getCode(), normalCloseEnum.getReason());
        if (result) {
            removeUrlWebSocketMapping(webSocket);
        }
        return result;
    }

    /**
     * 移除Url和WebSocket的映射
     */
    private void removeUrlWebSocketMapping(WebSocket webSocket) {
        for (Map.Entry<String, WebSocket> entry : mWebSocketPool.entrySet()) {
            if (entry.getValue() == webSocket) {
                String url = entry.getKey();
                mObservableCacheMap.remove(url);
                mWebSocketPool.remove(url);
            }
        }
    }

    private void removeWebSocketCache(WebSocket webSocket) {
        for (Map.Entry<String, WebSocket> entry : mWebSocketPool.entrySet()) {
            if (entry.getValue() == webSocket) {
                String url = entry.getKey();
                mWebSocketPool.remove(url);
            }
        }
    }

    public Observable<WebSocket> getWebSocket(String url) {
        return getWebSocketInfo(url)
                .filter(new Predicate<WebSocketInfo>() {
                    @Override
                    public boolean test(WebSocketInfo webSocketInfo) throws Exception {
                        return webSocketInfo.getWebSocket() != null;
                    }
                })
                .map(new Function<WebSocketInfo, WebSocket>() {
                    @Override
                    public WebSocket apply(WebSocketInfo webSocketInfo) throws Exception {
                        return webSocketInfo.getWebSocket();
                    }
                });
    }

    public synchronized Observable<WebSocketInfo> getWebSocketInfo(final String url) {
        //先从缓存中取
        Observable<WebSocketInfo> observable = mObservableCacheMap.get(url);
        if (observable == null) {
            //缓存中没有，新建
            Logger.d(TAG, "缓存中没有Observable...");
            observable = Observable
                    .create(new WebSocketOnSubscribe(url))
                    .retry()
                    //因为有share操作符，只有当所有观察者取消注册时，这里才会回调
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            //所有都不注册了，关闭连接
                            closeNow(url);
                            Logger.d(TAG, "所有观察者都取消注册，关闭连接...");
                        }
                    })
                    //Share操作符，实现多个观察者对应一个数据源
                    .share()
                    //将回调都放置到主线程回调，外部调用方直接观察，实现响应回调方法做UI处理
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            //将数据源缓存
            mObservableCacheMap.put(url, observable);
        } else {
            //缓存中有，从连接池中取出
            Logger.d(TAG, "缓存中有Observable...");
            WebSocket webSocket = mWebSocketPool.get(url);
            if (webSocket != null) {
                observable = observable.startWith(createConnect(url, webSocket));
            }
        }
        return observable;
    }

    /**
     * 组装数据源
     */
    private final class WebSocketOnSubscribe implements ObservableOnSubscribe<WebSocketInfo> {
        private String mWebSocketUrl;
        private WebSocket mWebSocket;
        private boolean isReconnecting = false;

        public WebSocketOnSubscribe(String webSocketUrl) {
            this.mWebSocketUrl = webSocketUrl;
        }

        @Override
        public void subscribe(ObservableEmitter<WebSocketInfo> emitter) throws Exception {
            //因为retry重连不能设置延时，所以只能这里延时，降低发送频率
            if (mWebSocket == null && isReconnecting) {
                if (Looper.getMainLooper() != Looper.myLooper()) {
                    long millis = mReconnectIntervalTimeUnit.toMillis(mReconnectInterval);
                    SystemClock.sleep(millis);
                }
            }
            initWebSocket(emitter);
        }

        private Request createRequest(String url) {
            return new Request.Builder().get().url(url).build();
        }

        /**
         * 初始化WebSocket
         */
        private synchronized void initWebSocket(ObservableEmitter<WebSocketInfo> emitter) {
            if (mWebSocket == null) {
                mWebSocket = mClient.newWebSocket(createRequest(mWebSocketUrl), new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        super.onOpen(webSocket, response);
                        //连接成功
                        if (!emitter.isDisposed()) {
                            mWebSocketPool.put(mWebSocketUrl, mWebSocket);
                            //重连成功
                            if (isReconnecting) {
                                emitter.onNext(createReconnect(mWebSocketUrl, webSocket));
                            } else {
                                emitter.onNext(createConnect(mWebSocketUrl, webSocket));
                            }
                        }
                        isReconnecting = false;
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        super.onMessage(webSocket, text);
                        //收到消息
                        if (!emitter.isDisposed()) {
                            emitter.onNext(createReceiveStringMsg(mWebSocketUrl, webSocket, text));
                        }
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        super.onMessage(webSocket, bytes);
                        //收到消息
                        if (!emitter.isDisposed()) {
                            emitter.onNext(createReceiveByteStringMsg(mWebSocketUrl, webSocket, bytes));
                        }
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        super.onClosed(webSocket, code, reason);
                        if (!emitter.isDisposed()) {
                            emitter.onNext(createClose(mWebSocketUrl));
                        }
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                        super.onFailure(webSocket, throwable, response);
                        mWebSocket = null;
                        Logger.d(TAG,"连接失败");
                        //移除WebSocket缓存，retry重试重新连接
                        removeWebSocketCache(webSocket);
                        if (!emitter.isDisposed()) {
                            if(mIsAutoReconnect) {
                                isReconnecting = true;
                                emitter.onNext(createPrepareReconnect(mWebSocketUrl));
                                //失败发送onError，让retry操作符重试
                                emitter.onError(throwable);
                            }else {
                                emitter.onNext(createFailure(mWebSocketUrl));
                            }
                        }
                    }
                });
            }
        }
    }

    private WebSocketInfo createConnect(String url, WebSocket webSocket) {
        return mWebSocketInfoPool.obtain(url)
                .setWebSocket(webSocket)
                .setStatus(WebSocketStatus.STATUS_CONNECTED)
                .setConnect(true);
    }

    private WebSocketInfo createReconnect(String url, WebSocket webSocket) {
        return mWebSocketInfoPool.obtain(url)
                .setWebSocket(webSocket)
                .setStatus(WebSocketStatus.STATUS_RE_CONNECTED)
                .setReconnect(true);
    }

    private WebSocketInfo createPrepareReconnect(String url) {
        return mWebSocketInfoPool.obtain(url)
                .setPrepareReconnect(true)
                .setStatus(WebSocketStatus.STATUS_ON_FAILURE);
    }

    private WebSocketInfo createFailure(String url) {
        return mWebSocketInfoPool.obtain(url)
                .setPrepareReconnect(false)
                .setStatus(WebSocketStatus.STATUS_ON_FAILURE);
    }

    private WebSocketInfo createReceiveStringMsg(String url, WebSocket webSocket, String stringMsg) {
        return mWebSocketInfoPool.obtain(url)
                .setConnect(true)
                .setWebSocket(webSocket)
                .setStringMsg(stringMsg)
                .setStatus(WebSocketStatus.STATUS_ON_REPLY);
    }

    private WebSocketInfo createReceiveByteStringMsg(String url, WebSocket webSocket, ByteString byteMsg) {
        return mWebSocketInfoPool.obtain(url)
                .setConnect(true)
                .setWebSocket(webSocket)
                .setByteStringMsg(byteMsg)
                .setStatus(WebSocketStatus.STATUS_ON_REPLY_BYTE);
    }

    private WebSocketInfo createClose(String url) {
        return mWebSocketInfoPool.obtain(url).setStatus(WebSocketStatus.STATUS_ON_CLOSED);
    }


    private void initOkHttpClient(Config config) {
        SSLSocketFactory sslSocketFactory=config.getSslSocketFactory();
        HostnameVerifier trustManager=config.getTrustManager();
        if (sslSocketFactory == null) {
            sslSocketFactory= SSLManager.getSslSocketFactory(null, null, null);
        }
        if (trustManager == null) {
            trustManager=new SSLManager.SafeHostnameVerifier();
        }
        if( trustManager instanceof SSLManager.SafeHostnameVerifier){
            ((SSLManager.SafeHostnameVerifier) trustManager).trustAll(config.isTrustAll());
        }
        final X509TrustManager x509TrustManager = new OkX509TrustManager();
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{x509TrustManager}, new SecureRandom());
            mClient=new OkHttpClient.Builder()
                    .readTimeout(config.getTimeoutInterval(), config.getTimeoutTimeUnit())//设置读取超时时间
                    .writeTimeout(config.getTimeoutInterval(), config.getTimeoutTimeUnit())//设置写的超时时间
                    .connectTimeout(config.getTimeoutInterval(), config.getTimeoutTimeUnit())//设置连接超时时间
                    .pingInterval(config.getPingInterval(),config.getPingIntervalTimeUnit())
                    .socketFactory(config.getSocketFactory())
                    .sslSocketFactory(sslSocketFactory,x509TrustManager).hostnameVerifier(trustManager)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
