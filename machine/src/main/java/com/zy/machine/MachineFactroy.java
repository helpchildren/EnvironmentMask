package com.zy.machine;

import com.zy.machine.device.SQ800Machine;

public class MachineFactroy {

    public static MachineManage init() {
        MachineManage manage = new SQ800Machine();//鼎戟
        return manage;
    }

}
