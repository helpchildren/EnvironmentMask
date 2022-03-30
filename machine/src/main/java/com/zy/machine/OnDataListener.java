package com.zy.machine;

public interface OnDataListener {

    /*
     * 异常回调
     * errcode 1000 打开设备失败
     * errcode 1001 无袋
     * errcode 1002 出袋口有袋子未取走
     * errcode 1003 切刀故障
     * errcode 1004 卡袋
     * errcode 1005 串通讯错误
     * errcode 1006 出货失败
     * errcode 1007 未初始化
     * */
    void onError(int errcode, String err);

    /*
     * 开始
     * */
    void onStart(int type);

    /*
    * 出货成功
    * */
    void onSuccess();
}
