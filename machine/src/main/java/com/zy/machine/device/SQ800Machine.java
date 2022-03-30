package com.zy.machine.device;

import android.os.SystemClock;
import android.util.Log;

import com.dingee.tcmsdk.TicketModule;
import com.zy.machine.MachineManage;
import com.zy.machine.OnDataListener;

/*
* SQ800机器管理 --鼎旗
* */
public class SQ800Machine extends MachineManage {

    private static final String TAG="SQ800Machine";

    private Thread receiveThread = null;
    private boolean flag = false;

    private int fd = -1;
    private final TicketModule obj_tcm;

    private int baudRate = 9600;//波特率
    private int outLength = 229;//出货长度 单位毫米   袋子229    口罩210
    private int iCurDevAddr	= 0;	// 当前选中的设备地址
    private String devicesPort = "/dev/ttyS0";//串口号  口罩是1 袋子是0
    private OnDataListener listener;

    public SQ800Machine() {
        obj_tcm = new TicketModule();
    }

    //设置波特率
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
    //设置打开串口号
    public void setDevicesPort(String devicesPort) {
        this.devicesPort = devicesPort;
    }
    //设置出货长度
    public void setOutLength(int outLength) {
        this.outLength = outLength;
    }


    public void init(OnDataListener listener) {
        this.listener = listener;
        flag = true;
        isOutGoodsFlag = false;//出货标志
        receiveThread();
    }

    public void destroy() {
        flag = false;
        isOutGoodsFlag = false;
        if (receiveThread != null)
            receiveThread = null;
    }

    private boolean isOutGoodsFlag = false;//出货标志

    //0袋子 1口罩
    public void outGoods(int type){
        if (flag){
            if (!isOutGoodsFlag){
                fd = -1;
                listener.onStart(type);
                isOutGoodsFlag = true;
            }
        }else {
            sendErrMsg(1007, "控制机头尚未初始化");
        }
    }

    /**
     * 接收数据线程
     */
    private void receiveThread(){
        if(receiveThread != null){
            return;
        }
        receiveThread = new Thread(){
            @Override
            public void run() {
                while (flag){
                    if (isOutGoodsFlag){

                        //判断是否先打开设备 未打开则先打开串口
                        if (fd <= 0){
                            fd = obj_tcm.dgOpenPort(devicesPort, baudRate);
                            if(fd <= 0){
                                sendErrMsg(1000,"串口"+devicesPort+" 打开失败");
                                return;
                            }
                        }
                        //出货
                        int ret = obj_tcm.dgCutTicket(fd, iCurDevAddr,TicketModule.TICKET_OUT_CAL_MILLIMETER , outLength,9);
                        switch(ret) {
                            case TicketModule.RSLT_OUT_TICKET_SUCC:
                                listener.onSuccess();
                                break;
                            case TicketModule.RSLT_OUT_TICKET_NOPAPER:
                                sendErrMsg(1001,"出货失败:无货");
                                break;
                            case TicketModule.RSLT_OUT_TICKET_NOT_TAKEN:
                                sendErrMsg(1002,"出货失败:出货口有货未取走");
                                break;
                            case TicketModule.RSLT_OUT_TICKET_KNIFE_ERR:
                                if(reset == 0){
                                    resetCMD(1);
                                    reset++;
                                    continue;
                                }
                                sendErrMsg(1003,"出货失败:切刀故障");
                                break;
                            case TicketModule.RSLT_OUT_TICKET_PAPERJAM:
                                if(reset == 0){
                                    resetCMD(0);
                                    reset++;
                                    continue;
                                }
                                sendErrMsg(1004,"出货失败:发生卡袋");
                                break;
                            default:
                                sendErrMsg(1005,"出货失败:通讯错误 错误码："+ret);
                                break;
                        }
                        reset = 0;//成功返回 重置恢复标志
                        isOutGoodsFlag = false;
                        obj_tcm.dgReleasePort(fd);//关闭串口
                    }
                    SystemClock.sleep(1000);
                }
            }
        };
        //启动接收线程
        receiveThread.start();
    }

    /*
    * 恢复指令
    * 0：恢复卡袋
    * 1：恢复切刀
    * */
    private int reset = 0;//恢复标志
    private void resetCMD(int type){
        if (type == 0){
            obj_tcm.dgResetPaperJam(fd, iCurDevAddr);
        }else {
            obj_tcm.dgResetKnife(fd, iCurDevAddr);
        }
    }


    private void sendErrMsg(int errcode, String err){
        if (listener != null) {
            listener.onError(errcode,err);
        }
    }

}
