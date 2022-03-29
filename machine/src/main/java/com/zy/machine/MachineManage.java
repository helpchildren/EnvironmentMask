package com.zy.machine;

import android.content.Context;

/*
* 硬件机器管理
* */
public abstract class MachineManage {

    //设置串口号
    public abstract void setDevicesPort(String devicesPort);

    //设置波特率
    public abstract void setBaudRate(int baudRate);

    //设置出货长度
    public abstract void setOutLength(int outLength);

    //打开连接
    public abstract void openDevice(OnDataListener listener);

    //关闭连接
    public abstract void closeDevice();

    //出货 0袋子 1口罩
    public abstract void outGoods(int type);

}
