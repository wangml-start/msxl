package com.cgmn.msxl.comp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoScrollViewPager extends ViewPager {

    float beforeX = 0;
    float lastX = 0;

    public NoScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public NoScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            beforeX = ev.getX();
        } else if (action == MotionEvent.ACTION_UP){
            lastX = ev.getX();
            if (beforeX == lastX){ // 点击事件
                return super.onInterceptTouchEvent(ev);
            } else {
                if (beforeX > lastX){ // 向左滑动
                    if (listener != null){
                        listener.onLeftFling();
                    }
                } else if (beforeX < lastX){ // 向右滑动
                    if (listener != null){
                        listener.onRightFling();
                    }
                }
                beforeX = 0;
                lastX = 0;
                return true;
            }
        }
        return false;
    }

    public interface FlingListener{
        void onLeftFling();
        void onRightFling();
    }

    private FlingListener listener;

    public void setFlingListener(FlingListener listener){
        this.listener = listener;
    }
}