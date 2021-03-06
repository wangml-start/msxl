package com.cgmn.msxl.page.ranking;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.adpter.UserDanAdpter;
import com.cgmn.msxl.data.RankEntity;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.FixStringBuffer;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class DanFragment extends Fragment {
    private ListView listView;
    private Context mContext;
    private static final String danLevel = "danLevel";
    private Integer danRank;
    private Handler mHandler;
    private List<RankEntity> mData = null;
    private UserDanAdpter adpter;
    private TextView txt_rank, txt_des;

    public DanFragment() { }
    public static DanFragment newInstance(Integer trainType) {
        DanFragment fragment = new DanFragment();
        Bundle args = new Bundle();
        args.putInt(danLevel, trainType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            danRank = bundle.getInt(danLevel);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.user_dan_fragment, container, false);
        bindView(view);
        initAdpter();
        return view;
    }

    private void bindView(View view){
        mContext = view.getContext();
        listView = view.findViewById(R.id.list_content);
        txt_rank = view.findViewById(R.id.txt_rank);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    mData = (List<RankEntity>) msg.obj;
                    if(!CommonUtil.isEmpty(mData)){
                        adpter = new UserDanAdpter(mContext, mData);
                        listView.setAdapter(adpter);
                        FixStringBuffer text = new FixStringBuffer();
                        text.append("我的排名： ");
                        Integer rankNo = mData.get(0).getMyRank();
                        if(rankNo != null && rankNo>0){
                            text.append("%s/%s", rankNo, mData.get(0).getTotalAcc());
                        }else{
                            text.append("%s/%s", "--", mData.get(0).getTotalAcc());
                        }
                        txt_rank.setText(text.toString());
                    }
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
        txt_des =  view.findViewById(R.id.txt_des);
        if(danRank == 1){
            txt_des.setText(" 资金达到200万");
        }else if(danRank == 2){
            txt_des.setText(" 资金达到1000万");
        }else if(danRank == 3){
            txt_des.setText(" 资金达到10000万");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private String getUrl(){
        String action = "/statistic/user_dan";
        Map<String, String> params = new HashMap<>();
        params.put("danRank", danRank+"");
        params.put("token", GlobalDataHelper.getToken(mContext));
        return CommonUtil.buildGetUrl(
                PropertyService.getInstance().getKey("serverUrl"),
                action, params);
    }

    private void initAdpter() {
        //加载用户信息
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClientManager.getAsyn(getUrl(),
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
                                    message.obj = data.getRankList();
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