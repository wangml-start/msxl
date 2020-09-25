package com.cgmn.msxl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MyPatternUtil;

public class RegisterActivity extends CustomerBaseActivity {
    private EditText tx_pwd;
    private EditText tx_user_name;
    private EditText tx_email;
    private Button btRegister;
    private Button go_signin;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rgister_layout);
        bindView();
        setHintSize();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btRegister) {
//            startActivity(new Intent(this, RegisterActivity.class));
////            this.finish();

            String em = tx_email.getText().toString();
            String userName = tx_user_name.getText().toString();
            String pws = tx_pwd.getText().toString();

            Toast.makeText(mContext, em + userName + pws, Toast.LENGTH_SHORT).show();

        }else if(v.getId() == R.id.go_signin){
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean flag = true;
        if (s.length() < 8) {
            flag = false;
        }

        if (flag && validForm()) {
            btRegister.setEnabled(true);
        } else {
            btRegister.setEnabled(false);
        }
    }

    private boolean validForm() {
        boolean flag = true;
        String em = tx_email.getText().toString();
        String userName = tx_user_name.getText().toString();
        StringBuffer tipes = new StringBuffer();
        if (CommonUtil.isEmpty(em) || !MyPatternUtil.validEmail(em)) {
            tipes.append(getSourceString(R.string.sign_email));
            tipes.append(getSourceString(R.string.valid_fails));
            tipes.append("\n");
            flag = false;
        }
        if (CommonUtil.isEmpty(userName)) {
            tipes.append(getSourceString(R.string.sign_user_name));
            tipes.append(getSourceString(R.string.valid_fails));
            flag = false;
        }

        if (tipes.length() > 0) {
            Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    private void bindView() {
        mContext = this;
        tx_pwd = findViewById(R.id.tx_pwd);
        tx_user_name = findViewById(R.id.tx_user_name);
        tx_email = findViewById(R.id.tx_email);
        btRegister = findViewById(R.id.btRegister);
        go_signin = findViewById(R.id.go_signin);

        btRegister.setOnClickListener(this);
        go_signin.setOnClickListener(this);

        tx_pwd.addTextChangedListener(this);

        Intent intent=getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        String email=bundle.getString("email");
        String pws=bundle.getString("pws");
        tx_email.setText(email);
        tx_pwd.setText(pws);
    }

    private void setHintSize() {
        //设置"用户名"提示文字的大小
        StringBuffer tipes = new StringBuffer(getSourceString(R.string.email_user_for));
        SpannableString s = new SpannableString(tipes.toString());
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(10, true);
        s.setSpan(textSize, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置字体样式: NORMAL正常，BOLD粗体，ITALIC斜体，BOLD_ITALIC粗斜体
        s.setSpan(new StyleSpan(Typeface.ITALIC), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tx_email.setHint(s);
    }

}
