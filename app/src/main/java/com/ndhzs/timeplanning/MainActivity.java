package com.ndhzs.timeplanning;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ndhzs.timeplanning.weight.TimeSelectView;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public TimeSelectView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
        view.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y, int dy) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("123", "onDestroy: ");
        List<Rect> rects = view.getRects();
        HashMap<Rect, String> rectAndName = view.getRectAndName();
        HashMap<Rect, String> rectAndDTime = view.getRectAndDTime();
    }
}