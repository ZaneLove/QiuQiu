package com.zanelove.androidcustomdemo.drag;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Zane on 2015/5/16.
 */
public class DragLinearLayout extends LinearLayout {
    private DragLayout mDragLayout;
    public DragLinearLayout(Context context) {
        super(context);
    }

    public DragLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDragLayout(DragLayout mDragLayout){
        this.mDragLayout = mDragLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果当前是关闭状态,子View：ListView能滚动
        if(mDragLayout.getStatus() == DragLayout.Status.Close) {
            return super.onInterceptTouchEvent(ev); //false,不拦截事件，由子View来处理
        }else {
            return true; //拦截事件，交给DragLinearLayout来处理
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果当前是关闭状态,子View：ListView能滚动
        if(mDragLayout.getStatus() == DragLayout.Status.Close) {
            return super.onTouchEvent(event); //false,不拦截事件，由子View来处理
        }else {
            //手指抬起，执行关闭操作
            if(event.getAction() == MotionEvent.ACTION_UP) {
                mDragLayout.close();
            }
            return true; //拦截事件，交给DragLinearLayout来处理
        }
    }
}
