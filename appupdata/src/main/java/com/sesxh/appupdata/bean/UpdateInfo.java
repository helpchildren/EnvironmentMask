package com.sesxh.appupdata.bean;

public class UpdateInfo {
    private String versionCode;//版本号 1
    private String versionName;//版本名 1.0.0
    private String description;
    private String apkurl;
    private String appName;
    private String isForce;//是否强制升级 0否 1是
    private double size;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApkurl() {
        return apkurl;
    }

    public void setApkurl(String apkurl) {
        this.apkurl = apkurl;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public boolean isForce() {
        return "1".endsWith(isForce);
    }


    public void setIsForce(String isForce) {
        this.isForce = isForce;
    }

    @Override
    public String toString() {
        return "{" +
                "versionCode:'" + versionCode + '\'' +
                ", versionName:'" + versionName + '\'' +
                ", description:'" + description + '\'' +
                ", apkurl:'" + apkurl + '\'' +
                ", appName:'" + appName + '\'' +
                ", isForce:" + isForce +
                ", size:" + size +
                '}';
    }
}
