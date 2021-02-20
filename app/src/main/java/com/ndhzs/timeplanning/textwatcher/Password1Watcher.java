package com.ndhzs.timeplanning.textwatcher;

import android.text.Editable;

import com.google.android.material.textfield.TextInputLayout;

public class Password1Watcher extends BaseTextWatcher{

    private final TextInputLayout mLayout;

    public Password1Watcher(TextInputLayout mLayout) {
        super(mLayout);
        this.mLayout = mLayout;
    }

    @Override
    public void afterTextChanged(Editable s) {
        super.afterTextChanged(s);

        String text = s.toString();
        if (s.length() > 5 && !text.matches(".*[a-zA-Z]+.*")) {
            mLayout.setError(null);//每次都会设置一个新的报错，必须把前一个报错给删掉
            mLayout.setError("密码必须包含字母！");
        }else if (s.length() <= mLayout.getCounterMaxLength() && text.matches(".*[a-zA-Z]+.*")){
            mLayout.setError(null);
        }
    }
}
