package com.zy.environment.bean;

import com.zy.environment.config.GlobalSetting;

import java.util.ArrayList;
import java.util.List;

public class MsgBean {

    private String type; //类型 客户端：login登录、back出袋反馈、qrcode获取二维码广告、
                             // 服务端：  1出袋、2心跳、3设备登录、4出袋回调、5获取二维码广告、6其他
    private String msg;
    private String device_id;//设备号
    private String order_sn;//订单sn
    private String result;// 出袋结果 1机头反馈出袋成功 2机头反馈出袋失败
    private String order_type;  //订单类型 0袋子 1口罩
    private String qrcode_url;  //设备二维码地址

    private List<AdvBean> adv;//广告列表

    public MsgBean() {
    }

    public MsgBean(String type) {
        this.type = type;
        this.device_id = GlobalSetting.deviceid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getOrder_sn() {
        return order_sn;
    }

    public void setOrder_sn(String order_sn) {
        this.order_sn = order_sn;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getQrcode_url() {
        return qrcode_url;
    }

    public void setQrcode_url(String qrcode_url) {
        this.qrcode_url = qrcode_url;
    }

    public List<AdvBean> getAdv() {
        return adv==null?new ArrayList<>():adv;
    }

    public void setAdv(List<AdvBean> adv) {
        this.adv = adv;
    }
}
