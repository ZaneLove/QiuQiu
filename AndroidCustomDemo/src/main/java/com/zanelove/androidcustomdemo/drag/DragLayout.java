package com.zanelove.androidcustomdemo.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Zane on 2015/4/25.
 */
public class DragLayout extends FrameLayout{
    private static final String TAG = "DragLayout";
    private ViewGroup mLeftContent,mMainContent;
    private ViewDragHelper mDragHelper;
    private int measureHeight,measureWidth,mRange;
    private OnDragStatusChangeListener mListener;
    //初始状态
    private Status mStatus = Status.Close;

    public void setStatus(Status status){
        this.mStatus = status;
    }

    public Status getStatus(){
        return mStatus;
    }

    /**
     * 状态枚举
     */
    public static enum Status{
        Close,Open,Draging;
    }

    public interface OnDragStatusChangeListener {
        void onClose();
        void onOpen();
        void onDraging(float percent);
    }

    public void setDragStatusListener(OnDragStatusChangeListener onDragStatusChangeListener){
        this.mListener = onDragStatusChangeListener;
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //a.初始化操作(通过静态方法)
        /**
         * ViewGroup forParent:所要拖拽孩子的父View
         * float sensitivity:敏感度
         * Callback cb:回调接口，当你触摸到子View的时候就会响应
         * mTouchSlop:最小敏感范围，值越小越敏感
         *
         public static ViewDragHelper create(ViewGroup forParent, float sensitivity, ViewDragHelper.Callback cb) {
             ViewDragHelper helper = create(forParent, cb);
             helper.mTouchSlop = (int)((float)helper.mTouchSlop * (1.0F / sensitivity));
             return helper;
         }
         */
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }

    ViewDragHelper.Callback mCallback =  new ViewDragHelper.Callback() {
        /**
         * 1.根据返回结果决定当前child是否可以拖拽
            child：当前被拖拽的View
            pointerId：区分多点触摸的id
         * @param child
         * @param pointerId
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.e(TAG,"tryCaptureView"+child);
            return true;
        }

        /**
         * 当capturedChild被捕获时,回调此方法
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            Log.e(TAG,"onViewCaptured"+capturedChild);
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 返回拖拽的范围，但不对拖拽进行真正的限制，仅仅决定了动画执行的速度
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 2.根据建议值修正将要移动到的(横向)位置
         * @param child 当前拖拽的View
         * @param left 新的位置的建议值  left = oldLeft = dx;
         * @param dx 位置变化量
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.e(TAG,"clampViewPositionHorizontal:"+" oldLeft"+child.getLeft() + " dx:"+dx+" left"+left);

            if(child == mMainContent) {
                left = fixLeft(left);
            }
            return left;
        }

//        该方法暂时不用
//        @Override
//        public int clampViewPositionVertical(View child, int top, int dy) {
//            return super.clampViewPositionVertical(child, top, dy);
//        }

        /**
         * 3.当View位置改变的时候，处理要做的事情（更新状态，伴随动画，重绘界面），注意此时的View已经发生了位置的改变
         * @param changedView 改变位置的View
         * @param left 新的左边值
         * @param top
         * @param dx 水平方向变化量  (右拖拽为正，左拖拽为负)
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            int newLeft = left;
            if(changedView == mLeftContent) {
                //把当前变化量传递给mMainContent
                newLeft = mMainContent.getLeft() + dx;
                //进行修正
                newLeft = fixLeft(newLeft);
                //当左面板移动之后，再强制放回去
                mLeftContent.layout(0,0,0+measureWidth,0+measureHeight);
                mMainContent.layout(newLeft, 0, newLeft + measureWidth, 0 + measureHeight);
            }

            //更新状态，执行动画
            dispatchDragEvent(newLeft);

            //为了兼容低版本，每次修改之后重绘界面
            invalidate();
        }

        /**
         * 当View被释放的时候，处理的事情(执行动画)
         * @param releasedChild 被释放的子View
         * @param xvel 水平方向的速度（右拖拽为正，左拖拽为负）
         * @param yvel 垂直方向的速度（上拖拽为负，下拖拽为正）
         *
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.d(TAG,"onViewReleased:"+" xvel"+xvel+" yvel:" + yvel);
            super.onViewReleased(releasedChild, xvel, yvel);
            //判断执行 关闭/开启
            //先考虑所有开启的情况，剩下的就都是关闭的情况
            if(xvel == 0 && mMainContent.getLeft() > mRange / 2.0f){
                open();
            }else if(xvel > 0) {
                open();
            }else{
                close();
            }
        }
    };

    private void dispatchDragEvent(int newLeft) {
        float percent = newLeft * 1.0f / mRange;

        //更新状态，执行回调
        Status perStatus = mStatus; //上一次状态
        mStatus = updateStatus(percent);
        if(mStatus != perStatus) {
            //状态发生变化
            if(mStatus == Status.Close) {
                //当前变为关闭状态
                if(mListener != null) {
                    mListener.onClose();
                }
            }else if(mStatus == Status.Open) {
                if(mListener != null) {
                    mListener.onOpen();
                }
            }
        }

        //每时每刻都在调用onDraging()
        if(mListener != null) {
            mListener.onDraging(percent);
        }

        /**
         * 伴随动画：
         */
        animViews(percent);
    }

    private Status updateStatus(float percent) {
        if(percent == 0f) {
            return Status.Close;
        }else if(percent == 1.0f) {
            return Status.Open;
        }
        return Status.Draging;
    }

    private void animViews(float percent) {
//      1. 左面板：缩放动画，平移动画，透明度动画
        //0.0 -> 1.0f >>> 0.5f -> 1.0f >>> 0.5f * percent + 0.5f
        /*
            3.0以上版本才兼容
            mLeftContent.setScaleX(0.5f + 0.5f * percent);
            mLeftContent.setScaleY(0.5f + 0.5f * percent);
        */
        //需要导入nineoldandroids.jar包
        /**
         * 缩放动画
         */
        ViewHelper.setScaleX(mLeftContent, evaluate(percent, 0.5f, 1.0f));
        ViewHelper.setScaleY(mLeftContent,0.5f + 0.5f * percent);
        /**
         * 平移动画 -mWidth / 2.0f -> 0.0f
         */
        ViewHelper.setTranslationX(mLeftContent,evaluate(percent,-measureWidth / 2.0f,0));
        /**
         * 透明度
         */
        ViewHelper.setAlpha(mLeftContent,evaluate(percent,0.5f,1.0f));
//      2. 主界面：缩放动画
        //1.0f -> 0.8f
        ViewHelper.setScaleX(mMainContent,evaluate(percent,1.0f,0.8f));
        ViewHelper.setScaleY(mMainContent,evaluate(percent,1.0f,0.8f));
//      3. 背景动画：亮度变化（颜色变化）
        getBackground().setColorFilter((Integer)evaluateColor(percent, Color.BLACK,Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    public Float evaluate(float fraction,Number startValue,Number endValue){
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 颜色变化过度
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //2.持续平滑动画（高频率调用）
        if(mDragHelper.continueSettling(true)) {
            //如果返回true，还需要继续执行
            ViewCompat.postInvalidateOnAnimation(this); //参数传this(child所在的ViewGroup)
        }
    }

    /**
     * 关闭
     */
    public void close() {
        close(true);
    }

    /**
     * 关闭时是否平滑
     * @param isSmooth true 平滑
     *                 false 不平滑
     */
    private void close(boolean isSmooth) {
        int finalLeft = 0;
        if(isSmooth) {
            //1.触发一个平滑动画
            if(mDragHelper.smoothSlideViewTo(mMainContent,finalLeft,0)){
                //返回true代表还没有移动到指定位置，需要刷新界面
                ViewCompat.postInvalidateOnAnimation(this); //参数传this(child所在的ViewGroup)
            }
        }else {
            mMainContent.layout(finalLeft, 0, finalLeft + measureWidth, 0 + measureHeight);
        }
    }

    /**
     * 开启
     */
    public void open() {
        open(true);
    }

    /**
     * 开启时是否平滑
     * @param isSmooth true 平滑
     *                 false 不平滑
     */
    private void open(boolean isSmooth) {
        int finalLeft = mRange;
        if(isSmooth) {
            //1.触发一个平滑动画
            if(mDragHelper.smoothSlideViewTo(mMainContent,finalLeft,0)){
                //返回true代表还没有移动到指定位置，需要刷新界面
                ViewCompat.postInvalidateOnAnimation(this); //参数传this(child所在的ViewGroup)
            }
        }else {
            mMainContent.layout(finalLeft,0,finalLeft + measureWidth,0 + measureHeight);
        }
    }

    //b.传递触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //传递给mDragHelper
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        }catch (Exception e) {
        }
        //返回true，持续接收事件
        return true;
    }

    /**
     * 当xml填充结束之后，此方法被调用，同时它的所有的孩子都添加进来了
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Github
        //写注释
        //容错性检查（至少有俩子View，子View必须是ViewGroup的子类）
        if(getChildCount() < 2) {
            throw new IllegalStateException("Your ViewGroup must have two children at least!");
        }

        if(!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("Your children must be an instance of ViewGroup!");
        }

        mLeftContent = (ViewGroup) getChildAt(0);//根据索引找孩子
        mMainContent = (ViewGroup) getChildAt(1);//根据索引找孩子
    }

    /**
     * 当onMeasure方法前后测量尺寸有变化的时候回调此方法
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        measureHeight = getMeasuredHeight();
        measureWidth = getMeasuredWidth();
        mRange = (int) (measureWidth * 0.6f);
    }

    /**
     * 根据范围修正左边的值
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if(left < 0) {
            return 0;
        }else if(left > mRange) {
            return mRange;
        }
        return left;
    }
}