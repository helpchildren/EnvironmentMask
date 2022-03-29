package com.zy.environment.machine;

import com.zy.environment.config.GlobalSetting;
import com.zy.machine.MachineManage;
import com.zy.machine.OnDataListener;
import com.zy.machine.device.SQ800Machine;

public class MachineController {

    private MachineManage machineBag;//袋子控制器
    private MachineManage machineMask;//口罩控制器


    public void openDevice(OnDataListener listener){
        //获取硬件控制
        machineBag = new SQ800Machine();//鼎戟
        machineBag.setOutLength(GlobalSetting.outLenBag);
        machineBag.setDevicesPort(GlobalSetting.serialPortBag);
        machineBag.openDevice(listener);

        machineMask = new SQ800Machine();//鼎戟
        machineMask.setOutLength(GlobalSetting.outLenMask);
        machineMask.setDevicesPort(GlobalSetting.serialPortMask);
        machineMask.openDevice(listener);

    }

    public void setOutBagLength(int outLength){
        machineBag.setOutLength(outLength);
    }

    public void setOutMaskLength(int outLength){
        machineMask.setOutLength(outLength);
    }

    public void outGoods(String type){
        if ("0".equals(type)){
            machineBag.outGoods(0);
        }else {
            machineMask.outGoods(1);
        }
    }
    /*
    * 关闭设备
    * */
    public void closeDevice(){
        if (machineBag != null){
            machineBag.closeDevice();
        }
        if (machineMask != null){
            machineMask.closeDevice();
        }
    }

}
