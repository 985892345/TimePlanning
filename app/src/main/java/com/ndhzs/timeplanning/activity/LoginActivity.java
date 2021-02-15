package com.ndhzs.timeplanning.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.ndhzs.timeplanning.TextWatcher.BaseTextWatcher;
import com.ndhzs.timeplanning.httpservice.SendNetRequest;
import com.ndhzs.timeplanning.weight.RegisterDialog;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnForgetPassword;
    private Button btnRegister;
    private Button btnLogin;
    private Button btnQQ;
    private Button btnWechat;
    private CheckBox cbRemember;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    private SharedPreferences shared;
    private SharedPreferences.Editor editor;

    private RegisterDialog registerDialog;

    private MHandler mHandler;

    private static final String TAG = "123";
    private static final int SUCCEED = 0;
    private static final int FAIL = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initEvent();
    }

    private void initView() {
        btnForgetPassword = findViewById(R.id.login_btn_forgetPassword);
        btnRegister = findViewById(R.id.login_btn_register);
        btnLogin = findViewById(R.id.login_btn_login);
        btnQQ = findViewById(R.id.login_btn_qq);
        btnWechat = findViewById(R.id.login_btn_wechat);

        cbRemember = findViewById(R.id.login_cb_remember);

        tilUsername = findViewById(R.id.login_til_username);
        tilPassword = findViewById(R.id.login_til_password);

        etUsername = findViewById(R.id.login_et_username);
        etPassword = findViewById(R.id.login_et_password);
    }

    private void initEvent() {
        shared = getSharedPreferences("data", MODE_PRIVATE);
        editor = shared.edit();
        btnForgetPassword.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnQQ.setOnClickListener(this);
        btnWechat.setOnClickListener(this);

        cbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("remember_password", isChecked);
                editor.apply();
            }
        });

        etUsername.addTextChangedListener(new BaseTextWatcher(tilUsername));
        etPassword.addTextChangedListener(new BaseTextWatcher(tilPassword));

        //记录是否记住密码
        cbRemember.setChecked(shared.getBoolean("remember_password", false));
        if (cbRemember.isChecked()){

            String username = shared.getString("username", null);
            if (username != null) {
                etUsername.setText(username);
                etPassword.setText(shared.getString("password", null));
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
                registerDialog = new RegisterDialog(LoginActivity.this, R.style.dialog);
                registerDialog.setRegisterDialogListener(new RegisterDialog.RegisterDialogListener() {
                    @Override
                    public void ClosedClickListener(String username, String password) {
                        sharedPreferencesEditor(username, password);
                        etUsername.setText(username);
                        etPassword.setText(password);
                        registerDialog.dismiss();
                    }
                });
                registerDialog.show();
                break;
            case R.id.login_btn_login :
                String username = Objects.requireNonNull(etUsername.getText()).toString();
                String password = Objects.requireNonNull(etPassword.getText()).toString();
                if (username.equals("")|| password.equals("")){
                    Toast.makeText(this, "请输入完整！", Toast.LENGTH_SHORT).show();
                }else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", username);
                    map.put("password", password);
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
    public void sharedPreferencesEditor(String username, String password) {
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    private static class MHandler extends Handler {

        private final WeakReference<LoginActivity> weakReference;

        public MHandler(LoginActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LoginActivity activity= weakReference.get();

            String[] data = (String[]) msg.obj;

            if (activity != null){
                switch (msg.what) {
                    case SUCCEED : {
                        activity.sharedPreferencesEditor(data[0], data[1]);
//                        Intent intent = new Intent(activity, ContentActivity.class);
//                        activity.startActivity(intent);
                        break;
                    }
                    case FAIL : {
                        Toast.makeText(activity, "登陆失败！", Toast.LENGTH_LONG).show();
                        activity.tilUsername.setError(data[2]);
                        activity.tilPassword.setError(data[2]);
                        break;
                    }
                }
            }
        }
    }
}