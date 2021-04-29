package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import androidx.viewpager.widget.ViewPager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.page.normal.NormalPagerAdapter;
import com.google.android.material.tabs.TabLayout;


public class NormalRankingActivity extends BaseOtherActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void init(){
        bindView();
    };

    @Override
    protected int getContentView() {
        return R.layout.normal_ranking_layout;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.free_rank_list);
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
        NormalPagerAdapter adapter = new NormalPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

}
