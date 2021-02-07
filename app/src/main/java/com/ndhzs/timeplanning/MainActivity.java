package com.ndhzs.timeplanning;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ndhzs.timeplanning.weight.TimeSelectView;

public class MainActivity extends AppCompatActivity {

    public TimeSelectView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
    }
}