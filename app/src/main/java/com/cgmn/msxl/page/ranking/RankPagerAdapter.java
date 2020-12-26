package com.cgmn.msxl.page.ranking;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.cgmn.msxl.data.StockHolder;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class RankPagerAdapter extends FragmentPagerAdapter {
    private static final String[] TAB_TITLES = new String[]{"龙头战法", "趋势波段"};
    private static final Integer[] types = new Integer[]{StockHolder.LEADING_STRATEGY, StockHolder.NORMAL_STRATEGY};
    private final Context mContext;
    private String rankType;

    public RankPagerAdapter(Context context, FragmentManager fm, String rankType) {
        super(fm);
        this.mContext = context;
        this.rankType = rankType;
    }

    @Override
    public Fragment getItem(int position) {
        return RankFragment.newInstance(types[position]+"", rankType);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}