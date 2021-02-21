package com.ndhzs.timeplanning.weight;

import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ndhzs.timeplanning.R;

public class MyDrawerLayout {

    private final AppCompatActivity activity;

    public MyDrawerLayout(AppCompatActivity activity){
        this.activity = activity;
        InMyDrawerLayout();
    }

    public void InMyDrawerLayout(){

        //隐藏状态栏时，获取状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        //初始化状态栏的高度
        View statusBar = activity.findViewById(R.id.view_statusBar);
        ConstraintLayout.LayoutParams params =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
        statusBar.setLayoutParams(params);
        //隐藏状态栏
        Window window = activity.getWindow();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        final DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);

        //将ToolBar与ActionBar关联
        activity.setSupportActionBar(toolbar);
        //另外openDrawerContentDescRes 打开图片   closeDrawerContentDescRes 关闭图片
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar, 0, 0);
        //初始化状态
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        CardView content = (CardView) drawerLayout.getChildAt(0);
        //蒙层颜色
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // 滑动的过程中执行 slideOffset：从0到1
                //侧边栏
                float scale = 1 - slideOffset;//1 ~ 0
                float leftScale = (1 - 0.4f * scale);//0.6~1.0
                float leftAlpha = 0.4f + 0.6f * slideOffset; //0.4~1.0

                float rightScale = (0.6f + 0.4f * scale);//1.0~0.6
                float rightAlpha = 0.4f + 0.6f * scale;//1.0~0.4
                float rotation = -5 * slideOffset;//设置旋转0 ~ -5
                float radius = (float) Math.sqrt(1000 * slideOffset);//设置圆角

                drawerView.setScaleX(leftScale);
                drawerView.setScaleY(leftScale);
                drawerView.setAlpha(leftAlpha);

                content.setScaleX(rightScale);
                content.setScaleY(rightScale);
                content.setAlpha(rightAlpha);
                content.setRotationY(rotation);
                content.setTranslationX(500 * slideOffset);//0~width
                content.setRadius(radius);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }
        });
    }
}
