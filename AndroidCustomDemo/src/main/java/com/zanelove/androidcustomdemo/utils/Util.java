package com.zanelove.androidcustomdemo.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

/**
 * Created by Zane on 2015/5/16.
 */
public class Util {

    /**
     * 无需等待上一次吐司显示完，就可以直接显示下一条吐司
     */
    public static Toast mToast;
    public static void showToast(Context mContext,String msg){
        if(mToast == null) {
            mToast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * dip 转换成px
     */
    public static float dip2Dimension(float dip,Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip,displayMetrics);
    }
}
