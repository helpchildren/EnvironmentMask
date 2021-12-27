package com.zy.environment.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zy.environment.R;


/**
 * Created by lfn on 2021/4/21.
 */

public class FylToast {
    private Toast mToast;
    private static long lastTime=0;
    private int showDuration;
    private static CharSequence lastText;
    private static CharSequence nowText;

    private FylToast(Context context, CharSequence text, int duration) {
        View v = LayoutInflater.from(context).inflate(R.layout.fyl_toast, null);
        TextView textView = (TextView) v.findViewById(R.id.textToast);
        textView.setText(text);
        showDuration=duration;
        if (duration==0){
            showDuration=2000;//这里转换的原因是因为Toast.LENGTH_SHORT实际对应的数字是0
        }
        if (duration==1){
            showDuration=4000;//这里转换的原因是因为Toast.LENGTH_SHORT实际对应的数字是1，如果不转换，后面的时间判断就会出错
        }
        nowText=text;
        mToast = new Toast(context);
        mToast.setDuration(duration);
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.setView(v);
    }

    public static FylToast makeText(Context context, CharSequence text, int duration) {
        return new FylToast(context, text, duration);
    }

    //显示 Toast 之前，判断 Toast 的内容是否与上次一样，不一样就排队在后面展示即可，一样判断上一次的是否已经显示完，未显示完，则本次不显示
    public void show() {
        if (!nowText.equals(lastText) && mToast!=null){
            mToast.show();
            lastTime=System.currentTimeMillis();
            lastText=nowText;
        }else {
            if((System.currentTimeMillis()-lastTime)>showDuration && mToast != null){
                mToast.show();
                lastTime=System.currentTimeMillis();
                lastText=nowText;
            }
        }
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        if (mToast != null) {
            mToast.setGravity(gravity, xOffset, yOffset);
        }
    }
}
