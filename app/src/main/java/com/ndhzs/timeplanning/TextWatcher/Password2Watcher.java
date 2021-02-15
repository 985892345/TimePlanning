package com.ndhzs.timeplanning.TextWatcher;

import android.text.Editable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Password2Watcher extends BaseTextWatcher{

    private final TextInputLayout preLayout;
    private final TextInputEditText password1;
    private final TextInputLayout mLayout;

    public Password2Watcher(TextInputLayout preLayout, TextInputEditText password1, TextInputLayout mLayout) {
        super(mLayout);

        this.preLayout = preLayout;
        this.password1 = password1;
        this.mLayout = mLayout;
    }

    @Override
    public void afterTextChanged(Editable s) {

        String password2 = s.toString();
        if (password1.getText().length() < 6){
            preLayout.setError("密码必须大于5位！");
        }
        if (s.length() >= password1.getText().length() && !password2.contentEquals(password1.getText())) {
            mLayout.setError(null);
            mLayout.setError("密码不相同！");
        }else if (s.length() <= mLayout.getCounterMaxLength()){
            mLayout.setError(null);
        }
    }
}
