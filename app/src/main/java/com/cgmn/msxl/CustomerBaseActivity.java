package com.cgmn.msxl;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerBaseActivity extends AppCompatActivity
        implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {

    protected String getSourceString(int sourceId){
        return getResources().getString(sourceId);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }
}
