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
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;
import com.cgmn.msxl.utils.PagePermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class TrainFragment extends Fragment{
    private Context mContxt;
    private ListView list_content;
    private NetImageView user_header;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;
    private Handler mHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.train_fragment, container, false);
        bindView(view);
        initAdpter();

        loadRaleatedTome();

        return view;
    }

    private void initAdpter(){
        List<Integer> list = new ArrayList<>();
        list.add(PageMainItem.LEADING_STRATEGY);
        list.add(PageMainItem.NORMAL_STRATEGY);

        list.add(PageMainItem.FREE_KLINE);
        list.add(PageMainItem.FREE_TIME_SHARE);

        list.add(PageMainItem.SUM_PL_LINE);
        list.add(PageMainItem.LEADING_LINE);
        list.add(PageMainItem.NORMARL_LINE);
        list.add(PageMainItem.FREE_RATE_CURV);


        list.add(PageMainItem.TOTAL_RANK);
        list.add(PageMainItem.DAN_RANK);
        list.add(PageMainItem.FREE_RANK_LIST);

        list.add(PageMainItem.TREND_BREAK_UP);

        list.add(PageMainItem.OPTIONAL_STOCKS);
        list.add(PageMainItem.MODEL_SETTING);
        list.add(PageMainItem.VIOLATE_MODE_DETAI);

        list.add(PageMainItem.COMMENT_TO_ME);
        list.add(PageMainItem.APPROVE_TO_ME);

        mData = PagePermissionUtils.getPageDatas(list, this);
        if(mData == null){
            new ArrayList<>();
        }
        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(View view){
        mContxt = view.getContext();
        TextView txt_frg_tain = view.findViewById(R.id.txt_frg_tain);
        txt_frg_tain.setText(view.getResources().getString(R.string.up_train_txt));
        list_content = view.findViewById(R.id.list_content);
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mData.get(position);
                if( object instanceof PageMainItem){
                    int type = ((PageMainItem)object).getItemType();
                    Intent intent = null;
                    Bundle bundle = null;
                    switch (type){
                        case 0:
                            intent = new Intent(mContxt, ModeSettingActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(mContxt, RealControlActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(mContxt, RealControlActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.NORMAL_STRATEGY);
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
//                        case 3:
//                            intent = new Intent(mContxt, UserRankingActivity.class);
//                            bundle = new Bundle();
//                            bundle.putString("rank_type", "DAY");
//                            intent.putExtra("datas", bundle);
//                            startActivity(intent);
//                            break;
                        case 4:
                            intent = new Intent(mContxt, ViolateModeActivity.class);
                            startActivity(intent);
                            break;
                        case 5:
                            intent = new Intent(mContxt, UserRankingActivity.class);
                            bundle = new Bundle();
                            bundle.putString("rank_type", "SUM");
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 12:
                            intent = new Intent(mContxt, DanRankingActivity.class);
                            startActivity(intent);
                            break;
                        case 6:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.EARNING_CURVE_SUMMARY);
                            bundle.putString("title", getString(R.string.train_sum_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 7:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            bundle.putString("title", getString(R.string.leading_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 8:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.NORMAL_STRATEGY);
                            bundle.putString("title", getString(R.string.normal_line));
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 15:
                            intent = new Intent(mContxt, MarketTrendActivity.class);
                            startActivity(intent);
                            break;
                        case 16:
                            intent = new Intent(mContxt, OptionalStockActivity.class);
                            startActivity(intent);
                            break;
                        case 18:
                            resetNewVersionTip(18);
                            intent = new Intent(mContxt, RelatedToMeActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("type", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 19:
                            resetNewVersionTip(19);
                            intent = new Intent(mContxt, RelatedToMeActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("type", 0);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 21:
                            intent = new Intent(mContxt, KLineSimulateActivity.class);
                            startActivity(intent);
                            break;
                        case 22:
                            intent = new Intent(mContxt, NormalTimeShareActivity.class);
                            startActivity(intent);
                            break;
                        case 23:
                            intent = new Intent(mContxt, NormalRankingActivity.class);
                            startActivity(intent);
                            break;
                        case 24:
                            intent = new Intent(mContxt, NormalStatisticActivity.class);
                            startActivity(intent);

                    }
                }
            }
        });

        TextView txt_to_vip = view.findViewById(R.id.txt_to_vip);
        txt_to_vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContxt, VIPActivity.class);
                startActivity(intent);
            }
        });


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(MessageUtil.LOAD_RELATED_TO_ME == msg.what){
                    BaseData response = (BaseData) msg.obj;
                    if(response != null){
                        setNewVersionTip(response.getCommentToMe(), PageMainItem.COMMENT_TO_ME);
                        setNewVersionTip(response.getApproveToMe(), PageMainItem.APPROVE_TO_ME);
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContxt).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });


//        user_header = view.findViewById(R.id.user_header);
//        user_header.setImageContent(GlobalDataHelper.getUserCut(mContxt));
//        user_header.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GlobalDataHelper.setDate("email", GlobalDataHelper.getUserAcc(mContxt));
//                Intent intent = new Intent(mContxt, ImageViewActivity.class);
//                startActivity(intent);
//            }
//        });
    }


    private void setNewVersionTip(Integer num, Integer type){
        if(num != null && num > 0){
            PageMainItem version = null;
            for(Object ob : mData){
                if(ob instanceof  PageMainItem){
                    PageMainItem item = (PageMainItem) ob;
                    if(item.getItemType() == type){
                        version = item;
                        break;
                    }
                }
            }
            if(version != null){
                version.setRightDec(String.format("%s", num));
                version.setRightColor(R.color.colorPrimary);
                myAdapter.notifyDataSetChanged();
            }
        }
    }

    private void resetNewVersionTip(Integer type){
        PageMainItem version = null;
        for(Object ob : mData){
            if(ob instanceof  PageMainItem){
                PageMainItem item = (PageMainItem) ob;
                if(item.getItemType() == type){
                    version = item;
                    break;
                }
            }
        }
        if(version != null){
            version.setRightDec(null);
            myAdapter.notifyDataSetChanged();
        }
    }

    private void loadRaleatedTome(){
        GlobalTreadPools.getInstance(mContxt).execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("token", GlobalDataHelper.getToken(mContxt));
                String url = CommonUtil.buildGetUrl(
                        ConstantHelper.serverUrl,
                        "/chat/related_to_me", params);
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
                                message.what = MessageUtil.LOAD_RELATED_TO_ME;
                                try {
                                    message.obj = data;
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
