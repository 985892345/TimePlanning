package com.ndhzs.timeplanning.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.textwatcher.BaseTextWatcher;
import com.ndhzs.timeplanning.httpservice.SendNetRequest;
import com.ndhzs.timeplanning.weight.RegisterDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnForgetPassword;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private Button mBtnQQ;
    private Button mBtnWechat;
    private CheckBox mCbRemember;
    private TextInputLayout mTilUsername;
    private TextInputLayout mTilPassword;
    private TextInputEditText mEtUsername;
    private TextInputEditText mEtPassword;
    private String mUsername;
    private String mPassword;

    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;

    private RegisterDialog mRegisterDialog;

    private MHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initEvent();
    }

    private void initView() {
        mBtnForgetPassword = findViewById(R.id.login_btn_forgetPassword);
        mBtnRegister = findViewById(R.id.login_btn_register);
        mBtnLogin = findViewById(R.id.login_btn_login);
        mBtnQQ = findViewById(R.id.login_btn_qq);
        mBtnWechat = findViewById(R.id.login_btn_wechat);

        mCbRemember = findViewById(R.id.login_cb_remember);

        mTilUsername = findViewById(R.id.login_til_username);
        mTilPassword = findViewById(R.id.login_til_password);

        mEtUsername = findViewById(R.id.login_et_username);
        mEtPassword = findViewById(R.id.login_et_password);
    }

    private void initEvent() {
        mShared = getSharedPreferences("login", MODE_PRIVATE);
        mBtnForgetPassword.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
        mBtnQQ.setOnClickListener(this);
        mBtnWechat.setOnClickListener(this);

        mCbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor = mShared.edit();
                mEditor.putBoolean("remember_password", isChecked);
                mEditor.apply();
            }
        });

        mEtUsername.addTextChangedListener(new BaseTextWatcher(mTilUsername));
        mEtPassword.addTextChangedListener(new BaseTextWatcher(mTilPassword));

        //记录是否记住密码
        mCbRemember.setChecked(mShared.getBoolean("remember_password", false));
        if (mCbRemember.isChecked()){

            String username = mShared.getString("username", null);
            if (username != null) {
                mEtUsername.setText(username);
                mEtPassword.setText(mShared.getString("password", null));
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn_forgetPassword :
                Toast.makeText(this, "暂时不能改密码！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_btn_register :
                mRegisterDialog = new RegisterDialog(LoginActivity.this, R.style.dialog);
                mRegisterDialog.setRegisterDialogListener(new RegisterDialog.RegisterDialogListener() {
                    @Override
                    public void ClosedClickListener(String username, String password) {
                        sharedEditor(username, password);
                        mEtUsername.setText(username);
                        mEtPassword.setText(password);
                        mRegisterDialog.dismiss();
                    }
                });
                mRegisterDialog.show();
                break;
            case R.id.login_btn_login :
                mUsername = mEtUsername.getText().toString();
                mPassword = mEtPassword.getText().toString();
                if (mUsername.equals("")|| mPassword.equals("")){
                    Toast.makeText(this, "请输入完整！", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "请求登陆中，请耐心等待", Toast.LENGTH_SHORT).show();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", mUsername);
                    map.put("password", mPassword);
                    mHandler = new MHandler(LoginActivity.this);
                    new SendNetRequest(mHandler).sendPostNetRequest("https://www.wanandroid.com/user/login", map);
                }
                break;
            case R.id.login_btn_qq :
            case R.id.login_btn_wechat :
                Toast.makeText(this, "好看用的，暂未实现", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //写进SharedPreferences
    public void sharedEditor(String username, String password) {
        mEditor = mShared.edit();
        mEditor.putString("username", username);
        mEditor.putString("password", password);
        mEditor.apply();
    }

    private static class MHandler extends Handler {

        private final WeakReference<LoginActivity> weakReference;
        private final int SUCCEED = 0;
        private final int FAIL = -1;

        public MHandler(LoginActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LoginActivity activity= weakReference.get();
            if (activity != null){
                JSONObject js = (JSONObject) msg.obj;
                try {
                    int errorCode = js.getInt("errorCode");
                    switch (errorCode) {
                        case SUCCEED:
                            Toast.makeText(activity, "登陆成功，欢迎回来！", Toast.LENGTH_SHORT).show();
                            String username = activity.mUsername;
                            String password = activity.mPassword;
                            activity.sharedEditor(username, password);
                            activity.finish();
                            break;
                        case FAIL:
                            String errorMsg = js.getString("errorMsg");
                            Toast.makeText(activity, "登陆失败！", Toast.LENGTH_LONG).show();
                            activity.mTilUsername.setError(errorMsg);
                            activity.mTilPassword.setError(errorMsg);
                            break;
                    }
                }catch (JSONException e) {
                    Toast.makeText(activity, "Json读取出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}