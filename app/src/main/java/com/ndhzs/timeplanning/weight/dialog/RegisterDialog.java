package com.ndhzs.timeplanning.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.textwatcher.BaseTextWatcher;
import com.ndhzs.timeplanning.textwatcher.Password1Watcher;
import com.ndhzs.timeplanning.textwatcher.Password2Watcher;
import com.ndhzs.timeplanning.httpservice.SendNetRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class RegisterDialog extends Dialog {

    private final Context mContext;
    private TextInputLayout mTilUsername;
    private TextInputLayout mTilPassword1;
    private TextInputLayout mTilPassword2;
    private TextInputLayout mTilPhone;
    private TextInputEditText mEtUsername;
    private TextInputEditText mEtPassword1;
    private TextInputEditText mEtPassword2;
    private TextInputEditText mEtPhone;
    private CheckBox mCbAgreement;
    private Button mBtnRegister;
    private MHandler mHandler;
    private RegisterDialogListener mListener;


    public RegisterDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_register);
        //设置窗口
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        initView();
        initEvent();
    }

    public void initView() {
        mTilUsername =findViewById(R.id.register_til_username);
        mTilPassword1 = findViewById(R.id.register_til_password1);
        mTilPassword2 = findViewById(R.id.register_til_password2);
        mTilPhone = findViewById(R.id.register_til_phone);

        mEtUsername = findViewById(R.id.register_et_username);
        mEtPassword1 = findViewById(R.id.register_et_password1);
        mEtPassword2 = findViewById(R.id.register_et_password2);
        mEtPhone = findViewById(R.id.register_et_phone);

        mCbAgreement = findViewById(R.id.register_cb_agreement);
        mBtnRegister = findViewById(R.id.register_btn_register);
    }

    private void initEvent() {
        mEtUsername.addTextChangedListener(new BaseTextWatcher(mTilUsername));
        mEtPassword1.addTextChangedListener(new Password1Watcher(mTilPassword1));
        mEtPassword2.addTextChangedListener(new Password2Watcher(mTilPassword1, mEtPassword1, mTilPassword2));
        mEtPhone.addTextChangedListener(new BaseTextWatcher(mTilPhone));
        mBtnRegister.setOnClickListener(new MyOnClickListener());
    }

    public void setRegisterDialogListener(RegisterDialogListener l) {
        this.mListener = l;
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            boolean isOk = true;
            String username = mEtUsername.getText().toString();
            String password = mEtPassword1.getText().toString();
            String phoneNum = mEtPhone.getText().toString();
            if (username.equals("")){
                isOk = false;
                mTilUsername.setError("用户名不能为空！");
            }
            if (password.equals("")){
                isOk = false;
                mTilPassword1.setError("密码不能为空！");
            }
            if (phoneNum.length() != 11){
                isOk = false;
                mTilPhone.setError("号码尾数不为11位！");
            }
            if (isOk) {
                HashMap<String, String> map = new HashMap<>();
                map.put("username", username);
                map.put("password", password);
                map.put("repassword", password);
                mHandler = new MHandler(RegisterDialog.this);
                new SendNetRequest(mHandler).sendPostNetRequest("https://www.wanandroid.com/user/register", map);
            }
        }
    }

    private static class MHandler extends Handler {

        private final WeakReference<RegisterDialog> weakReference;
        private final int SUCCEED = 0;
        private final int FAIL = -1;

        public MHandler(RegisterDialog dialog) {
            this.weakReference = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            RegisterDialog dialog= weakReference.get();
            if (dialog != null) {
                JSONObject js = (JSONObject) msg.obj;
                try {
                    int errorCode = js.getInt("errorCode");
                    switch (errorCode) {
                        case SUCCEED:
                            Toast.makeText(dialog.mContext, "注册成功！", Toast.LENGTH_SHORT).show();
                            String username = dialog.mEtUsername.getText().toString();
                            String password = dialog.mEtPassword1.getText().toString();
                            dialog.mListener.ClosedClickListener(username, password);
                            break;
                        case FAIL:
                            String errorMsg = js.getString("errorMsg");
                            Toast.makeText(dialog.mContext, "注册失败！", Toast.LENGTH_LONG).show();
                            dialog.mTilUsername.setError(errorMsg);
                            break;
                    }
                }catch (JSONException e) {
                    Toast.makeText(dialog.mContext, "Json读取出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public interface RegisterDialogListener {
        //监听是否关闭
        void ClosedClickListener(String username, String password);
    }

    private static final String TAG = "123";
}