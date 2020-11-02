package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.page.ranking.RankPagerAdapter;
import com.google.android.material.tabs.TabLayout;


public class UserRankingActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


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
        viewPager = findViewById(R.id.view_pager);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("datas");
        String rankType = bundle.getString("rank_type");
        RankPagerAdapter adapter = new RankPagerAdapter(this, getSupportFragmentManager(), rankType);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

}
