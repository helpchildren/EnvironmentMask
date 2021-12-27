package com.sesxh.okwebsocket.config;

import android.content.Context;

import com.sesxh.okwebsocket.ssl.SSLManager;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;

/**
 * @author LYH
 * @date 2021/1/27
 * @time 9:34
 * @desc 全局配置类
 **/
public class Config {

    private Context context;
    private OkHttpClient client;
    private SocketFactory socketFactory;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier trustManager;
    private boolean isAutoReconnect;
    private TimeUnit reconnectIntervalTimeUnit;
    private long reconnectInterval;
    private TimeUnit pingIntervalTimeUnit;
    private long pingInterval;
    private long timeoutInterval;
    private TimeUnit timeoutTimeUnit;
    private boolean debug;
    private boolean isTrustAll;


    public Config(Builder builder) {
        this.context=builder.context;
        this.client=builder.client;
        this.socketFactory=builder.socketFactory;
        this.sslSocketFactory=builder.sslSocketFactory;
        this.trustManager=builder.trustManager;
        this.isAutoReconnect=builder.isAutoReconnect;
        this.reconnectIntervalTimeUnit=builder.reconnectIntervalTimeUnit;
        this.reconnectInterval=builder.reconnectInterval;
        this.pingInterval=builder.pingInterval;
        this.pingIntervalTimeUnit=builder.pingIntervalTimeUnit;
        this.timeoutInterval=builder.timeoutInterval;
        this.timeoutTimeUnit=builder.timeoutTimeUnit;
        this.debug=builder.debug;
        this.isTrustAll=builder.isTrustAll;
    }

    public static class Builder{
        private Context context;
        private OkHttpClient client;
        private SocketFactory socketFactory;
        private SSLSocketFactory sslSocketFactory;
        private HostnameVerifier trustManager;
        private boolean isAutoReconnect=true;//是否需要自动重连
        private long reconnectInterval=10;//重连间隔时间
        private TimeUnit reconnectIntervalTimeUnit=TimeUnit.SECONDS;// 重连间隔时间单位
        private long pingInterval;//心跳间隔时间
        private TimeUnit pingIntervalTimeUnit=TimeUnit.SECONDS;// 心跳间隔时间单位
        private long timeoutInterval=10;//超时时间
        private TimeUnit timeoutTimeUnit =TimeUnit.SECONDS;// 重连间隔时间单位
        private boolean debug=false;// 是否是开发模式
        private boolean isTrustAll=true;//是否信任所有证书


        public Builder(Context context) {
            this.context = context;
        }

        public Builder client(OkHttpClient client) {
            if(client!=null) {
                this.client = client;
            }
            return this;
        }

        public Builder socketFactory(SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public Builder trustManager(HostnameVerifier trustManager) {
            this.trustManager = trustManager;
            return this;
        }

        public Builder isAutoReconnect(boolean isAutoReconnect) {
            this.isAutoReconnect = isAutoReconnect;
            return this;
        }

        public Builder reconnectInterval(long reconnectInterval) {
            return reconnectInterval(reconnectInterval,reconnectIntervalTimeUnit);
        }

        public Builder reconnectInterval(long reconnectInterval, TimeUnit reconnectIntervalTimeUnit) {
            if(reconnectInterval>0) {
                this.reconnectInterval = reconnectInterval;
            }
            if(reconnectIntervalTimeUnit!=null) {
                this.reconnectIntervalTimeUnit = reconnectIntervalTimeUnit;
            }
            return this;
        }

        public Builder isDefaultHeartBeat(boolean isDefaultHeartBeat) {
            if(isDefaultHeartBeat){
                pingInterval=30;
            }
            return this;
        }

        public Builder pingInterval(long pingInterval) {
           return pingInterval(pingInterval,pingIntervalTimeUnit);
        }

        public Builder pingInterval(long pingInterval,TimeUnit pingIntervalTimeUnit) {
            if(pingInterval>0) {
                this.pingInterval = pingInterval;
            }
            if(pingIntervalTimeUnit!=null) {
                this.pingIntervalTimeUnit = pingIntervalTimeUnit;
            }
            return this;
        }

        public Builder timeoutInterval(long timeoutInterval) {
            return timeoutInterval(timeoutInterval, timeoutTimeUnit);
        }

        public Builder timeoutInterval(long timeoutInterval,TimeUnit timeoutUnit) {
            if(timeoutInterval>0) {
                this.timeoutInterval= timeoutInterval;
            }
            if(timeoutUnit!=null) {
                this.timeoutTimeUnit = timeoutUnit;
            }
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder trustAll(boolean trustAll) {
            isTrustAll = trustAll;
            return this;
        }

        public Builder hosts(String... hosts) {
            // 默认添加基础域名
            if (this.trustManager == null) {
                this.trustManager = new SSLManager.SafeHostnameVerifier(hosts);
            } else {
                if (this.trustManager instanceof SSLManager.SafeHostnameVerifier) {
                    ((SSLManager.SafeHostnameVerifier) this.trustManager).addHosts(Arrays.asList(hosts));
                } else {
                    throw new IllegalArgumentException("please verifier host in your custom hostnameVerifier");
                }
            }
            return this;
        }

        public Config build(){
            return new Config(this);
        }
    }

    public Context getContext() {
        return context;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public SocketFactory getSocketFactory() {
        return socketFactory==null?SocketFactory.getDefault():socketFactory;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public HostnameVerifier getTrustManager() {
        return trustManager;
    }

    public boolean isAutoReconnect() {
        return isAutoReconnect;
    }

    public TimeUnit getReconnectIntervalTimeUnit() {
        return reconnectIntervalTimeUnit;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public TimeUnit getPingIntervalTimeUnit() {
        return pingIntervalTimeUnit;
    }

    public long getPingInterval() {
        return pingInterval;
    }

    public long getTimeoutInterval() {
        return timeoutInterval;
    }

    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isTrustAll() {
        return isTrustAll;
    }


}
