package com.cgmn.msxl.comp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.ac.AppMainActivity;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.data.User;
import com.cgmn.msxl.db.AppSqlHelper;


public class LoginBaseActivity extends AppCompatActivity
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

    public void afterLoginSuccess(User user, Context context){
        //存数据
        AppSqlHelper sqlHeper = new AppSqlHelper(context);
        String token = user.getToken();
        ContentValues values = new ContentValues();
        values.put("phone", user.getPhone());
        values.put("user_name", user.getUserName());
        values.put("password", user.getPassword());
        values.put("token", token);
        values.put("last_active", 1);
        sqlHeper.upsert("users", values, "phone");
        AppApplication.getInstance().setToken(token);

        //跳转
        Intent intent = new Intent(this, AppMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userName", user.getUserName());
        intent.putExtra("userDate", bundle);
        startActivity(intent);
        finish();
    }
}
