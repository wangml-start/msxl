package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import androidx.viewpager.widget.ViewPager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.page.ranking.DanPagerAdapter;
import com.google.android.material.tabs.TabLayout;


public class DanRankingActivity extends BaseOtherActivity {

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

    @Override
    protected String setTitle(){
        return getString(R.string.dan_ranking);
    };

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
        DanPagerAdapter adapter = new DanPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

}
