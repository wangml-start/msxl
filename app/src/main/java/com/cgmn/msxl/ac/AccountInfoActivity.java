package com.cgmn.msxl.ac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.adpter.AccountAdapter;
import com.cgmn.msxl.data.EditInfoItem;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.service.GlobalDataHelper;

import java.util.ArrayList;
import java.util.Map;

public class AccountInfoActivity extends AppCompatActivity {
    private static final String TAG = AccountInfoActivity.class.getSimpleName();
    private Context mContext;
    private ListView list_content;
    private TextView backup_btn;
    private ArrayList<Object> mData = null;
    private AccountAdapter myAdapter = null;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accunt_info_layout);
        bindView();
        initAdpter();
        initReceiver();
    }


    private void initAdpter(){
        mData = new ArrayList<>();
        mData.add(new EditInfoItem(1, getString(R.string.head_img), "head_img"));
        mData.add(new EditInfoItem(0, getString(R.string.user_name), "user_name"));
        mData.add(new EditInfoItem(0, getString(R.string.gender),  "gender"));
        mData.add(new EditInfoItem(0, getString(R.string.signature),  "signature"));
        mData.add(new EditInfoItem(0, getString(R.string.ue_psw),  "password"));

        myAdapter = new AccountAdapter(mContext, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(){
        mContext = this;
        list_content = findViewById(R.id.list_content);
        backup_btn = findViewById(R.id.backup_btn);
        backup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.backup_btn){
                    finish();
                }
            }
        });
        final Map<String, Object> userInfo = GlobalDataHelper.getUser(mContext);
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EditInfoItem item = (EditInfoItem) mData.get(position);
                Intent intent = null;
                Bundle bundle = null;
                if("head_img".equals(item.getField_data())){
                    intent = new Intent(mContext, EditHeaderActivity.class);
                    bundle = new Bundle();
                    bundle.putString("title", item.getTitle());
                    bundle.putString("content", (String) userInfo.get(item.getField_data()));
                    bundle.putString("field_data", item.getField_data());
                    intent.putExtra("datas", bundle);
                    startActivity(intent);
                }else if("gender".equals(item.getField_data())){
                    intent = new Intent(mContext, EditGenderActivity.class);
                    bundle = new Bundle();
                    bundle.putString("title", item.getTitle());
                    bundle.putString("content", (String) userInfo.get(item.getField_data()));
                    bundle.putString("field_data", item.getField_data());
                    intent.putExtra("datas", bundle);
                    startActivity(intent);
                }else if("signature".equals(item.getField_data())){
                    intent = new Intent(mContext, EditSignatureActivity.class);
                    bundle = new Bundle();
                    bundle.putString("title", item.getTitle());
                    bundle.putString("content", (String) userInfo.get(item.getField_data()));
                    bundle.putString("field_data", item.getField_data());
                    intent.putExtra("datas", bundle);
                    startActivity(intent);
                }else if("password".equals(item.getField_data())){
                    intent = new Intent(mContext, EditPasswordActivity.class);
                    bundle = new Bundle();
                    bundle.putString("title", item.getTitle());
                    bundle.putString("content", (String) userInfo.get(item.getField_data()));
                    bundle.putString("field_data", item.getField_data());
                    intent.putExtra("datas", bundle);
                    startActivity(intent);
                }else if("user_name".equals(item.getField_data())){
                    intent = new Intent(mContext, EditNameActivity.class);
                    bundle = new Bundle();
                    bundle.putString("title", item.getTitle());
                    bundle.putString("content", (String) userInfo.get(item.getField_data()));
                    bundle.putString("field_data", item.getField_data());
                    intent.putExtra("datas", bundle);
                    startActivity(intent);
                }
            }
        });
    }

    private void initReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("收到了广播：", ReceiverMessage.EDIT_COMPLETED);
                Bundle bundle = intent.getExtras();
//                String resource = bundle.getString("resource");
                //执行自己的业务逻辑......
                myAdapter.notifyDataSetChanged();
            }
        };
        broadcastManager = LocalBroadcastManager.getInstance(mContext);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverMessage.EDIT_COMPLETED); //监听的事件key
        broadcastManager.registerReceiver(receiver, intentFilter);
    }
}