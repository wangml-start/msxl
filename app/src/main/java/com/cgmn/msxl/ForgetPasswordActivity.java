package com.cgmn.msxl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MyPatternUtil;

public class ForgetPasswordActivity extends CustomerBaseActivity {
    private EditText tx_new_pwd;
    private EditText tx_email;
    private EditText tx_valid_code;

    private Button bt_login;
    private Button bt_go_register;
    private Button bt_sent_email;
    private Button backup_btn;
    private showPassworCheckBox ck_show;

    private Context mContext;

    private MyTimeCount time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pws_layout);
        bindView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login) {
//            startActivity(new Intent(this, RegisterActivity.class));
////            this.finish();

            String em = tx_email.getText().toString();
            String pws = tx_new_pwd.getText().toString();
            String code = tx_valid_code.getText().toString();
            Toast.makeText(mContext, em + pws + code, Toast.LENGTH_SHORT).show();

        } else if (v.getId() == R.id.bt_go_register) {
            Intent intent = new Intent(this, RegisterActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("email", tx_email.getText().toString());
            bundle.putString("pws", tx_new_pwd.getText().toString());
            intent.putExtra("datas", bundle);
            startActivity(intent);
        } else if (v.getId() == R.id.bt_send_mail) {
            String em = tx_email.getText().toString();
            Toast.makeText(mContext, em, Toast.LENGTH_SHORT).show();
            if(MyPatternUtil.validEmail(em)){
                time.start();
            }else{
                StringBuffer tipes = new StringBuffer();
                tipes.append(getSourceString(R.string.sign_email));
                tipes.append(getSourceString(R.string.valid_fails));
                tipes.append("\n");
                Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.backup_btn) {
            finish();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean flag = true;
        if (s.length() < 6) {
            flag = false;
        }

        if (flag && validForm()) {
            bt_login.setEnabled(true);
        } else {
            bt_login.setEnabled(false);
        }
    }

    private boolean validForm() {
        boolean flag = true;
        String em = tx_email.getText().toString();
        String pws = tx_new_pwd.getText().toString();
        StringBuffer tipes = new StringBuffer();
        if (CommonUtil.isEmpty(em) || !MyPatternUtil.validEmail(em)) {
            tipes.append(getSourceString(R.string.sign_email));
            tipes.append(getSourceString(R.string.valid_fails));
            tipes.append("\n");
            flag = false;
        }
        if (CommonUtil.isEmpty(pws) || pws.length() < 8) {
            tipes.append(getSourceString(R.string.new_user_wd));
            tipes.append(getSourceString(R.string.valid_fails));
            tipes.append(":" + getSourceString(R.string.wd_reqiure_8));
            flag = false;
        }

        if (tipes.length() > 0) {
            Toast.makeText(mContext, tipes.toString(), Toast.LENGTH_SHORT).show();
        }
        return flag;
    }


    private void bindView() {
        mContext = this;
        tx_new_pwd = findViewById(R.id.tx_new_user_wd);
        tx_email = findViewById(R.id.tx_email);
        tx_valid_code = findViewById(R.id.tx_valid_code);
        bt_login = findViewById(R.id.bt_login);
        bt_go_register = findViewById(R.id.bt_go_register);
        bt_sent_email = findViewById(R.id.bt_send_mail);
        backup_btn = findViewById(R.id.backup_btn);
        time = new MyTimeCount(60000, 1000);

        ck_show = findViewById(R.id.ck_dis_pws);

        bt_login.setOnClickListener(this);
        bt_go_register.setOnClickListener(this);
        bt_sent_email.setOnClickListener(this);
        backup_btn.setOnClickListener(this);

        tx_valid_code.addTextChangedListener(this);

        ck_show.setPws(tx_new_pwd);
        ck_show.setOnCheckedChangeListener(ck_show);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        String email = bundle.getString("email");
        tx_email.setText(email);
    }


    class MyTimeCount extends CountDownTimer {
        public MyTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            bt_sent_email.setEnabled(false);
            String text = getSourceString(R.string.send_email);
            bt_sent_email.setText(String.format("%s(%s)", text, millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            bt_sent_email.setEnabled(true);
            String text = getSourceString(R.string.send_email);
            bt_sent_email.setText(text);
        }
    }
}
