package com.ndhzs.timeplanning.weight;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ndhzs.timeplanning.R;

public class NameDialog extends Dialog {

    private Context mContext;
    private ImageView mImgHead;
    private EditText mEtTop;
    private EditText mEtBottom;
    private TextView mTv1;
    private TextView mTv2;
    private TextView mTv3;
    private TextView mTv4;
    private Button mBtnBack;
    private Button mBtnMenu;
    private Button mBtnFinish;
    private onDlgCloseListener mCloseListener;

    public NameDialog(@NonNull Context context) {
        this(context, 0);
    }

    public NameDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dlg_set_name);
        //设置窗口
        Window dialogWindow = getWindow();
        dialogWindow.setWindowAnimations(R.style.NameDialogAnim);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        DisplayMetrics d = mContext.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(true);

        initView();
        initEvent();
    }

    private void initView() {
        mImgHead = findViewById(R.id.img_head);
        mEtTop = findViewById(R.id.et_name);
        mEtBottom = findViewById(R.id.et_describe);
        mTv1 = findViewById(R.id.tv_1);
        mTv2 = findViewById(R.id.tv_2);
        mTv3 = findViewById(R.id.tv_3);
        mTv4 = findViewById(R.id.tv_4);
        mBtnBack = findViewById(R.id.btn_back);
        mBtnMenu = findViewById(R.id.btn_menu);
        mBtnFinish = findViewById(R.id.btn_finish);
    }
    private void initEvent() {
        mBtnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    private void close() {
        if (mCloseListener != null) {
            String name = mEtTop.getText().toString();
            String describe = mEtBottom.getText().toString();
            if (name.length() > 0) {
                mCloseListener.onClose(name, describe);
            }
        }
        dismiss();
    }

    public void setOnDlgCloseListener(onDlgCloseListener l) {
        this.mCloseListener = l;
    }

    public interface onDlgCloseListener {
        void onClose(String name, String describe);
    }
}
