package com.cgmn.msxl.ac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.adpter.AccountAdapter;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.EditInfoItem;
import com.cgmn.msxl.service.GlobalDataHelper;

import java.util.ArrayList;
import java.util.Map;

public class AccountInfoActivity extends AppCompatActivity {
    private static final String TAG = AccountInfoActivity.class.getSimpleName();
    private Context mContext;
    private ListView list_content;
    private Handler mHandler;
    private ArrayList<Object> mData = null;
    private AccountAdapter myAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accunt_info_layout);
        bindView();
        initAdpter();
    }

    private void initAdpter(){
        Map<String, Object> userInfo = GlobalDataHelper.getUser(mContext);
        mData = new ArrayList<>();
        mData.add(new EditInfoItem(R.mipmap.head, getString(R.string.head_img), null, "head_img"));
        mData.add(new EditInfoItem(0, getString(R.string.user_name), (String) userInfo.get("user_name"), "user_name"));
        mData.add(new EditInfoItem(0, getString(R.string.gender), (String) userInfo.get("gender"), "gender"));
        mData.add(new EditInfoItem(0, getString(R.string.signature), (String) userInfo.get("signature"), "signature"));
        mData.add(new EditInfoItem(0, getString(R.string.ue_email), (String) userInfo.get("phone"), "phone"));
        mData.add(new EditInfoItem(0, getString(R.string.ue_psw), null, "password"));

        myAdapter = new AccountAdapter(mContext, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(){
        mContext = this;
        list_content = findViewById(R.id.list_content);
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EditInfoItem item = (EditInfoItem) mData.get(position);
                Intent intent = null;
                Bundle bundle = null;
                intent = new Intent(mContext, EditDetailActivity.class);
                bundle = new Bundle();
                bundle.putString("title", item.getTitle());
                bundle.putString("content", item.getContent());
                bundle.putString("field_data", item.getField_data());
                intent.putExtra("datas", bundle);
                startActivity(intent);
            }
        });
    }
}