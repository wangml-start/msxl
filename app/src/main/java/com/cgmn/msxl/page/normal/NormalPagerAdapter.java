package com.cgmn.msxl.page.normal;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.cgmn.msxl.data.StockHolder;
import com.cgmn.msxl.page.ranking.RankFragment;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class NormalPagerAdapter extends FragmentPagerAdapter {
    private static final String[] TAB_TITLES = new String[]{"K线榜", "分时榜"};
    private static final Integer[] types = new Integer[]{1,2};
    private final Context mContext;

    public NormalPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return normalRankFragment.newInstance(types[position]+"");
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