package com.zy.environment.widget;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.zy.environment.R;
import com.zy.environment.base.BaseDialog;
import com.zy.environment.config.GlobalSetting;
import com.zy.environment.utils.EventBusUtils;
import com.zy.environment.utils.FylToast;
import com.zy.environment.utils.Validate;

import androidx.annotation.NonNull;

public class SettingDialog extends BaseDialog {

    private EditText ed_input, eSerialPortBag, eSerialPortMask, eWsurl, eOutlenBag, eOutlenMask;
    private Button btnTestBag, btnTestMask;
    private LinearLayout llPassword, llSetting;
    private Switch s_isDebugLog;

    public static boolean isShowSet = false;

    public SettingDialog(@NonNull Context context) {
        super(context, R.style.BaseDialog, R.layout.setting_items);
        isShowSet = false;
    }

    @Override
    public void bindView(View v) {
        ed_input = (EditText) findViewById(R.id.ed_input);
        Button btnCancle = (Button) findViewById(R.id.btn_cancle);
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        llPassword = (LinearLayout) findViewById(R.id.ll_password);
        llSetting = (LinearLayout) findViewById(R.id.ll_setting);
        eSerialPortBag = (EditText) findViewById(R.id.e_serialPortBag);
        eSerialPortMask = (EditText) findViewById(R.id.e_serialPortMask);
        btnTestBag = (Button) findViewById(R.id.btn_testBag);
        btnTestMask = (Button) findViewById(R.id.btn_testMask);
        eWsurl  = (EditText) findViewById(R.id.e_wsurl);
        eOutlenBag = (EditText) findViewById(R.id.e_outlen);
        eOutlenMask  = (EditText) findViewById(R.id.e_outlen_mask);
        s_isDebugLog  = (Switch) findViewById(R.id.s_Log);

        s_isDebugLog.setChecked(GlobalSetting.isDugLog);
        s_isDebugLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalSetting.isDugLog = isChecked;
            }
        });

        eWsurl.setText(GlobalSetting.wsurl);
        eSerialPortBag.setText(GlobalSetting.serialPortBag);
        eSerialPortMask.setText(GlobalSetting.serialPortMask);
        eOutlenBag.setText(GlobalSetting.outLenBag +"");
        eOutlenMask.setText(GlobalSetting.outLenMask+"");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowSet){
                    if (Validate.isNull(eWsurl.getText().toString())
                            || Validate.isNull(eSerialPortBag.getText().toString())
                            || Validate.isNull(eSerialPortMask.getText().toString())
                            || Validate.isNull(eOutlenBag.getText().toString())
                            || Validate.isNull(eOutlenMask.getText().toString())){
                        FylToast.makeText(getContext(), "配置项不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    GlobalSetting.wsurl = eWsurl.getText().toString();
                    GlobalSetting.serialPortBag = eSerialPortBag.getText().toString();
                    GlobalSetting.serialPortMask = eSerialPortMask.getText().toString();
                    GlobalSetting.outLenBag = Integer.parseInt(eOutlenBag.getText().toString());
                    GlobalSetting.outLenMask = Integer.parseInt(eOutlenMask.getText().toString());
                    //保存
                    GlobalSetting.putSetting(getContext());
                    FylToast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
                    EventBusUtils.post("Refresh Main");
                    dismiss();
                }else {
                    String password = ed_input.getText().toString();
                    if ("123456".equals(password)) {
                        isShowSet = true;
                        llPassword.setVisibility(View.GONE);
                        llSetting.setVisibility(View.VISIBLE);
                    } else {
                        FylToast.makeText(getContext(), "密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                        ed_input.setText("");
                    }
                }
            }
        });
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnTestBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalSetting.outLenBag = Integer.parseInt(eOutlenBag.getText().toString());
                EventBusUtils.post("testBag");
            }
        });
        btnTestMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalSetting.outLenMask = Integer.parseInt(eOutlenMask.getText().toString());
                EventBusUtils.post("testMask");
            }
        });


    }



}
