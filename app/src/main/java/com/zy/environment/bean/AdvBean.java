package com.zy.environment.bean;

import android.net.Uri;

import com.zy.environment.config.GlobalSetting;

import java.io.File;

/*
* 广告类
* */
public class AdvBean {

    private String id;//广告id
    private String screen_name;//广告名
    private String type;//广告类型 1：图片 2：视频
    private String url;//下载路径

    private String dirName;//本地文件名

    public AdvBean() {
    }

    public AdvBean(String id, String screen_name, String type, String url) {
        this.id = id;
        this.screen_name = screen_name;
        this.type = type;
        this.url = url;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVideo() {
        return "2".equals(type);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getDirPath() {
        return GlobalSetting.AdvPath +File.separator+ dirName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AdvBean)) {
            return false;
        }
        AdvBean bean = (AdvBean) obj;
        return this.id.equals(bean.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", screen_name='" + screen_name + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", dirName='" + dirName + '\'' +
                '}';
    }
}
