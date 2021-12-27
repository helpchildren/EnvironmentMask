package com.sesxh.appupdata.callback;

import com.sesxh.appupdata.bean.UpdateInfo;

public interface UpdataCallback {

    void onUpdate(boolean isNeed, UpdateInfo info);

    void onError(String msg);
}
