package com.cgmn.msxl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cgmn.msxl.comp.showPassworCheckBox;
import com.cgmn.msxl.utils.CommonUtil;

public class loginActivity extends CustomerBaseActivity {
    private EditText tx_pwd;
    private EditText tx_email;

    private Button bt_login;
    private Button bt_go_register;
    private Button bt_forget_pws;
    private showPassworCheckBox ck_show;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        bindView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login) {
//            startActivity(new Intent(this, RegisterActivity.class));
////            this.finish();

            String em = tx_email.getText().toString();
            String pws = tx_pwd.getText().toString();

            Toast.makeText(mContext, em + pws, Toast.LENGTH_SHORT).show();

        }else if(v.getId() == R.id.bt_go_register){
            startActivity(new Intent(this, RegisterActivity.class));
        }else if(v.getId() == R.id.bt_forget){
            Intent intent = new Intent(this, ForgetPasswordActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("email",tx_email.getText().toString());
            intent.putExtra("datas", bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean flag = false;
        String em = tx_email.getText().toString();
        String pws = tx_pwd.getText().toString();
        if(!CommonUtil.isEmpty(em) && pws.length() >= 8){
            flag = true;
        }
        if (flag) {
            bt_login.setEnabled(true);
        } else {
            bt_login.setEnabled(false);
        }
    }



    @SuppressLint("WrongViewCast")
    private void bindView(){
        mContext = this;
        tx_pwd = findViewById(R.id.tx_pwd);
        tx_email = findViewById(R.id.tx_email);

        bt_login = findViewById(R.id.bt_login);
        bt_go_register = findViewById(R.id.bt_go_register);
        bt_forget_pws = findViewById(R.id.bt_forget);
        ck_show = findViewById(R.id.ck_dis_pws);

        bt_login.setOnClickListener(this);
        bt_forget_pws.setOnClickListener(this);
        bt_go_register.setOnClickListener(this);

        tx_pwd.addTextChangedListener(this);

        ck_show.setPws(tx_pwd);
        ck_show.setOnCheckedChangeListener(ck_show);
    }
}
