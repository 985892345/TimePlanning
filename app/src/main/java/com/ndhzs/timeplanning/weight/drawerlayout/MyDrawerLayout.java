package com.ndhzs.timeplanning.weight.drawerlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MyDrawerLayout extends DrawerLayout{
    public MyDrawerLayout(@NonNull Context context) {
        super(context);
    }
    public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDrawerOpen(GravityCompat.START)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }


}
