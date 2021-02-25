package com.ndhzs.timeplanning.weight.dialog;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.RoundCornerView;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

public class NameDialog extends Dialog implements View.OnClickListener{

    private Context mContext;
    private TaskBean mTaskBean;
    private ImageView mImgHead;
    private TextView mTvTime;
    private EditText mEtName;
    private EditText mEtDescribe;
    private TextView mTvBorderColor, mTvInsideColor, mTvSetRepeat, mTvSetGroup;
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
        mTvTime = findViewById(R.id.tv_time);
        mEtName = findViewById(R.id.et_name);
        mEtDescribe = findViewById(R.id.et_describe);
        mTvBorderColor = findViewById(R.id.tv_color_border);
        mTvInsideColor = findViewById(R.id.tv_color_inside);
        mTvSetRepeat = findViewById(R.id.tv_set_repeat);
        mTvSetGroup = findViewById(R.id.tv_set_group);
        mBtnBack = findViewById(R.id.btn_back);
        mBtnFinish = findViewById(R.id.btn_finish);
        mColorBorder = findViewById(R.id.view_color_border);
        mColorInside = findViewById(R.id.view_color_inside);

        String time = mTaskBean.getMonth() + "月" + mTaskBean.getDay() + "日，" + mTaskBean.getStartTime() + " - " + getEndTime();
        mTvTime.setText(time);

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
        mTvBorderColor.setOnClickListener(this);
        mTvSetRepeat.setOnClickListener(this);
        mTvInsideColor.setOnClickListener(this);
        mTvSetGroup.setOnClickListener(this);
        mBtnFinish.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
    }

    private String getEndTime() {
        String startTime = mTaskBean.getStartTime();
        String diffTime = mTaskBean.getDiffTime();
        int startHour = Integer.parseInt(startTime.substring(0, 2));
        int startMinute = Integer.parseInt(startTime.substring(3));
        String[] time = mTaskBean.getDiffTime().split(":");
        int dH = Integer.parseInt(time[0]);
        int dM = Integer.parseInt(time[1]);
        int endHour = startHour + dH;
        int endMinute = startMinute + dM;
        if (endMinute >= 60) {
            endHour++;
            endMinute -= 60;
        }
        return endHour + ":" + endMinute;
    }

    private void close() {
        if (mCloseListener != null) {
            String name = mEtName.getText().toString();
            String describe = mEtDescribe.getText().toString();
            if (name.length() > 0) {
                mTaskBean.setName(name);
                mTaskBean.setDescribe(describe);
                mCloseListener.onClose();
                mTaskBean.save();
            }
        }
        dismiss();
    }

    public void setOnDlgCloseListener(onDlgCloseListener l) {
        this.mCloseListener = l;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
            case R.id.btn_finish:
                close();
                break;
            case R.id.tv_color_border:
            case R.id.tv_color_inside:
            case R.id.tv_set_repeat:
            case R.id.tv_set_group:
                Toast.makeText(mContext, "QAQ 抱歉，因时间原因，该功能暂未实现", Toast.LENGTH_LONG).show();
        }
    }

    public interface onDlgCloseListener {
        void onClose();
    }
}
