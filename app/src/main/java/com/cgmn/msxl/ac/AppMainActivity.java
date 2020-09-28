package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.AppApplication;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.utils.AESUtil;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.Map;

public class AppMainActivity extends AppCompatActivity {
    private Context mContxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);

        bindAppMainView();
    }


    private void bindAppMainView(){
        mContxt = this;
        TextView un = findViewById(R.id.lb_name);
        TextView token = findViewById(R.id.lb_token);
        TextView ps = findViewById(R.id.lb_pws);
        TextView orpws = findViewById(R.id.lb_pws_origin);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("userDate");
        un.setText(bundle.getString("userName"));
        token.setText(AppApplication.getInstance().getToken());

        AppSqlHelper sqlHeper = new AppSqlHelper(mContxt);
        Map<String, Object> map = sqlHeper.getActiveUser();
        String password = AESUtil.decrypt((String) map.get("password"), MessageUtil.SERCURETY);
        ps.setText(password);
        orpws.setText((String) map.get("password"));
    }
}
