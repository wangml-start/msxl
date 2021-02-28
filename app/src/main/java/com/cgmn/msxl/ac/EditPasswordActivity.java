package com.cgmn.msxl.ac;

import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.EditBaseActivity;
import com.cgmn.msxl.comp.view.ClearEditTextView;
import com.cgmn.msxl.comp.view.showPassworCheckBox;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.AESUtil;
import com.cgmn.msxl.utils.MessageUtil;


import java.util.HashMap;
import java.util.Map;

public class EditPasswordActivity extends EditBaseActivity {
    private static final String TAG = EditPasswordActivity.class.getSimpleName();
    private ClearEditTextView tx_user_wd, tx_new_user_wd;
    private showPassworCheckBox ck_show;


    @Override
    protected int getContentView() {
        return R.layout.edit_upws_layout;
    }

    @Override
    protected void init() {
        baseBind();
        tx_user_wd = findViewById(R.id.tx_user_wd);
        tx_new_user_wd = findViewById(R.id.tx_new_user_wd);
        ck_show = findViewById(R.id.ck_dis_pws);
        ck_show.setOnCheckedChangeListener(ck_show);
        ck_show.setPws(tx_user_wd);
        ck_show.setPws(tx_new_user_wd);
        txt_complete.setEnabled(true);
    }


    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        Map<String, Object> user = GlobalDataHelper.getUser(mContext);
        params.put("phone", (String) user.get("phone"));
        params.put("field_data", fieldData);
        String password = tx_user_wd.getText().toString();
        String passwordNew = tx_new_user_wd.getText().toString();
        if(password.length() < 8 || passwordNew.length() < 8){
            throw new RuntimeException("密码位数验证失败！");
        }
        params.put("content", AESUtil.encrypt(passwordNew, MessageUtil.SERCURETY));
        params.put("password", AESUtil.encrypt(password, MessageUtil.SERCURETY));
        params.put("token", GlobalDataHelper.getToken(mContext));
        return params;
    }


    @Override
    public void finish() {
        dialog.cancel();
        dialog.dismiss();
        super.finish();
    }
}