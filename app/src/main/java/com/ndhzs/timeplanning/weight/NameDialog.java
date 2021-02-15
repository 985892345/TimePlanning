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
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

public class NameDialog extends Dialog {

    private Context mContext;
    private TaskBean mTaskBean;
    private ImageView mImgHead;
    private EditText mEtName;
    private EditText mEtDescribe;
    private TextView mTv1, mTv2, mTv3, mTv4;
    private Button mBtnBack, mBtnFinish;
    private RoundCornerView mColorBorder, mColorInside;
    private onDlgCloseListener mCloseListener;

    public NameDialog(@NonNull Context context, int themeResId, TaskBean taskBean) {
        super(context, themeResId);
        this.mContext = context;
        this.mTaskBean = taskBean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dlg_set_name);
        //设置窗口
        Window dialogWindow = getWindow();
        dialogWindow.setWindowAnimations(R.style.dialogAnim);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
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
        mEtName = findViewById(R.id.et_name);
        mEtDescribe = findViewById(R.id.et_describe);
        mTv1 = findViewById(R.id.tv_week_1);
        mTv2 = findViewById(R.id.tv_week_2);
        mTv3 = findViewById(R.id.tv_3);
        mTv4 = findViewById(R.id.tv_4);
        mBtnBack = findViewById(R.id.btn_back);
        mBtnFinish = findViewById(R.id.btn_finish);
        mColorBorder = findViewById(R.id.view_color_border);
        mColorInside = findViewById(R.id.view_color_inside);

        String name = mTaskBean.getName();
        if (!name.equals("点击设置任务名称")) {
            mEtName.setText(name);
        }
        String describe = mTaskBean.getDescribe();
        if (describe != null && describe.length() > 0) {
            mEtDescribe.setText(describe);
        }
        mColorBorder.setColor(mTaskBean.getBorderColor());
        mColorInside.setColor(mTaskBean.getInsideColor());
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
            String name = mEtName.getText().toString();
            String describe = mEtDescribe.getText().toString();
            if (name.length() > 0) {
                mTaskBean.setName(name);
                mTaskBean.setDescribe(describe);
                mCloseListener.onClose();
            }
        }
        dismiss();
    }

    public void setOnDlgCloseListener(onDlgCloseListener l) {
        this.mCloseListener = l;
    }

    public interface onDlgCloseListener {
        void onClose();
    }
}
