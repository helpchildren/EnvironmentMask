package com.zy.environment.widget;

import android.content.Context;
import android.widget.TextView;

import com.zy.environment.R;
import com.zy.environment.base.BaseDialog;
import com.zy.environment.utils.log.Logger;


/**
 * 功能描述：自定义dialog的正在加载对话框
 */

public class DialogUtils {

    private static DialogUtils mInstance;
    public static DialogUtils getInstance() {
        if (mInstance == null) {
            mInstance = new DialogUtils();
        }
        return mInstance;
    }

    private BaseDialog loadingDialog;

    public void showLoadingDialog(Context context) {
        showLoadingDialog(context,"加载中...");
    }

    public void showLoadingDialog(Context context, String msg) {
        if(loadingDialog == null){
            loadingDialog = new BaseDialog(context, R.layout.dialog_loading);// 创建自定义样式dialog
        }
        TextView tipTextView = (TextView) loadingDialog.findViewById(R.id.tipTextView);// 提示文字
        tipTextView.setText(msg);// 设置加载信息
        loadingDialog.showDialog(30);
    }

    /**
     * 关闭dialog
     */
    public void closeLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private ErrDialog errDialog;

    public void showErrDialog(Context context, String msg) {
        if (context == null){
            return;
        }
        if(errDialog == null){
            errDialog = new ErrDialog(context);// 创建自定义样式dialog
        }
        if (!errDialog.isShowing()) {
            errDialog.message(msg);
            errDialog.showDialog(60);
        }
    }

    public void closeErrDialog() {
        if(errDialog != null && errDialog.isShowing()){
            errDialog.dismiss();// 创建自定义样式dialog
        }
    }

    public void releaseDialog() {
        if (loadingDialog !=null){
            loadingDialog.dismiss();
            loadingDialog = null;
        }
        if (errDialog !=null){
            errDialog.dismiss();
            errDialog = null;
        }
    }

}