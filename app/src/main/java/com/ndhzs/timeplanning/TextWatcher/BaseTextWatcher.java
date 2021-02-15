package com.ndhzs.timeplanning.TextWatcher;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;


public class BaseTextWatcher implements TextWatcher {

    private final TextInputLayout mLayout;

    public BaseTextWatcher(TextInputLayout mLayout) {
        this.mLayout = mLayout;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length()  > mLayout.getCounterMaxLength()) {
            mLayout.setError("超出限定字数！");
        }else {
            mLayout.setError(null);
        }
    }
}
