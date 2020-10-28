package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.cgmn.msxl.R;
import com.google.android.material.tabs.TabLayout;

public class UseRankingActivity extends BaseActivity {

    private Context mContext;
    private TabLayout tabLayout;
    private String rankType;

    @Override
    protected void init(){
        bindView();
    };

    @Override
    protected int getContentView() {
        return R.layout.user_ranking_layout;
    }

//    @Override
//    protected String setTitle(){
//        if("DAY".equals(rankType)){
//            return getString(R.string.day_ranking);
//        }
//        return getString(R.string.total_ranking);
//    };

    @Override
    protected boolean showRight(){
        return false;
    };
    @Override
    protected boolean showComplate(){
        return false;
    };

    @SuppressLint("WrongViewCast")
    private void bindView(){
        mContext = this;
        tabLayout = findViewById(R.id.ranking_tab);
        // 添加 tab item
        tabLayout.addTab(tabLayout.newTab().setText("排行榜"));
        tabLayout.addTab(tabLayout.newTab().setText("龙头战法排行"));
        tabLayout.addTab(tabLayout.newTab().setText("趋势波段排行"));

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        rankType = bundle.getString("rank_type");
    }



}
