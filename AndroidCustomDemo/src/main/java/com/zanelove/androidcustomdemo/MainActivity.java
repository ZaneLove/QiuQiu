package com.zanelove.androidcustomdemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.zanelove.androidcustomdemo.drag.DragLayout;
import com.zanelove.androidcustomdemo.drag.DragLinearLayout;
import com.zanelove.androidcustomdemo.utils.Cheeses;
import com.zanelove.androidcustomdemo.utils.Util;

import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView mLeftList = (ListView) findViewById(R.id.lv_left);
        ListView mMainList = (ListView) findViewById(R.id.lv_main);
        final ImageView iv_head = (ImageView) findViewById(R.id.iv_head);
        //查找DragLayout，设置监听
        DragLayout mDragLayout = (DragLayout) findViewById(R.id.dl);
        mDragLayout.setDragStatusListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onClose() {
                Util.showToast(MainActivity.this,"onClose");
                //让图标晃动
                ObjectAnimator mAnim = ObjectAnimator.ofFloat(iv_head, "translationX", 15.0f);
                mAnim.setInterpolator(new CycleInterpolator(4));//差值器  来回晃动4圈
                mAnim.setDuration(800);
                mAnim.start();
            }

            @Override
            public void onOpen() {
                Util.showToast(MainActivity.this,"onOpen");

                //验证回调方法：左面板ListView随机设置一个条目
                Random random = new Random();
                int nextInt = random.nextInt(50);
                mLeftList.smoothScrollToPosition(nextInt);
            }

            @Override
            public void onDraging(float percent) {
                //更新图标的透明度
                //1.0 -> 0.0
                ViewHelper.setAlpha(iv_head,1 - percent);
            }
        });

        mLeftList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mTextView = (TextView)view;
                mTextView.setTextColor(Color.WHITE);
                return view;
            }
        });
        mMainList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, Cheeses.NAMES){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mTextView = (TextView)view;
                mTextView.setTextColor(Color.BLACK);
                return view;
            }
        });

        DragLinearLayout mDragLinearLayout = (DragLinearLayout) findViewById(R.id.dll);
        mDragLinearLayout.setDragLayout(mDragLayout);
    }
}
