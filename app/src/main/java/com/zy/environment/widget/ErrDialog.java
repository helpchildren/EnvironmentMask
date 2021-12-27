package com.zy.environment.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zy.environment.R;
import com.zy.environment.base.BaseDialog;

import androidx.annotation.NonNull;

public class ErrDialog extends BaseDialog {

    private TextView mMessageTv;
    private String message;

    public ErrDialog(@NonNull Context context) {
        super(context, R.style.BaseDialog, R.layout.dialog_err);
    }

    @Override
    public void bindView(View v) {
        ImageView close_iv = (ImageView) findViewById(R.id.close_iv);
        mMessageTv = (TextView) findViewById(R.id.message_tv);// 提示文字
        updateText();
        close_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public ErrDialog message(String message) {
        if(TextUtils.isEmpty(message)&&TextUtils.isEmpty(this.message)){
            this.message= "设备异常，请联系管理员！";
        }else if(!TextUtils.isEmpty(message)&&!message.equals(this.message)){
            this.message=message;
            updateText();
        }
        return this;
    }

    private void updateText() {
        if(mMessageTv==null){
            return;
        }
        mMessageTv.setText(this.message);
    }




}
