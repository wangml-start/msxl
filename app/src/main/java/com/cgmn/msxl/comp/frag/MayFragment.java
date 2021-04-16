package com.cgmn.msxl.comp.frag;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.manager.DownloadManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.*;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class MayFragment extends Fragment {
    private Context mContxt;
    private ListView list_content;
    private NetImageView user_header;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;

    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_fragment, container, false);
        bindView(view);
        initAdpter();
        checkVersion();
        return view;
    }

    private void initAdpter() {

        mData = new ArrayList<Object>();
        mData.add(new SplitItem(getString(R.string.personal)));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.acct_info), PageMainItem.MY_GENERAL_INFO));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.vip_way), PageMainItem.VIP_INFO));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.chargev_way), PageMainItem.CHARGE_INFO));
        mData.add(new SplitItem(getString(R.string.system)));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.contact_us), PageMainItem.CONTACT_US));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.check_new_version), PageMainItem.CHECK_NEW_VERSION));
        mData.add(new PageMainItem(R.drawable.item_header, getString(R.string.user_agrement_title), PageMainItem.USER_AGREMENT));

        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(View view) {
        mContxt = view.getContext();
        TextView txt_frg_tain = view.findViewById(R.id.txt_frg_tain);
        txt_frg_tain.setText(view.getResources().getString(R.string.tab_menu_may));
        list_content = view.findViewById(R.id.list_content);
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mData.get(position);
                if (object instanceof PageMainItem) {
                    int type = ((PageMainItem) object).getItemType();
                    Intent intent = null;
                    Bundle bundle = null;
                    switch (type) {
                        case 9:
                            intent = new Intent(mContxt, UserInfoActivity.class);
                            startActivity(intent);
                            break;
                        case 10:
                            intent = new Intent(mContxt, VIPActivity.class);
                            startActivity(intent);
                            break;
                        case 11:
                            intent = new Intent(mContxt, ChargeActivity.class);
                            startActivity(intent);
                            break;
                        case 13:
                            intent = new Intent(mContxt, ContactUsActivity.class);
                            startActivity(intent);
                            break;
                        case 14:
                            intent = new Intent(mContxt, NewVersionActivity.class);
                            startActivity(intent);
                            break;
                        case 17:
                            intent = new Intent(mContxt, UserAgrementActivity.class);
                            startActivity(intent);
                            break;

                    }
                }
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (MessageUtil.REQUEST_SUCCESS == msg.what) {
                    List<String> list = (List<String>) msg.obj;
                    if (!CommonUtil.isEmpty(list)) {
                        ((PageMainItem)mData.get(6)).setRightDec("New");
                        ((PageMainItem)mData.get(6)).setRightColor(R.color.main_red_color);
                        myAdapter.notifyDataSetChanged();
                    } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                        GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                    }
                }
                return false;
            }
        });
    }

    private void checkVersion() {
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                String action = "/common/query_new_version";
                Map<String, String> params = new HashMap<>();
                params.put("version", ConstantHelper.version);
                params.put("type", "android");
                params.put("token", GlobalDataHelper.getToken(mContxt));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        action, params);
                OkHttpClientManager.getAsyn(url,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                mHandler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(BaseData data) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.REQUEST_SUCCESS;
                                try {
                                    message.obj = data.getInfoList();
                                    Integer status = data.getStatus();
                                    if (status == null || status == -1) {
                                        throw new Exception(data.getError());
                                    }
                                } catch (Exception e) {
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                }
                                mHandler.sendMessage(message);
                            }
                        });
            }
        });
    }
}
