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
public class DanPagerAdapter extends FragmentPagerAdapter {
    private static final Integer NIU_SAN = 1;
    private static final Integer DA_HU = 2;
    private static final Integer YOU_ZI = 3;
    private static final String[] TAB_TITLES = new String[]{"牛散","大户", "游资"};
    private static final Integer[] types = new Integer[]{NIU_SAN, DA_HU, YOU_ZI};
    private final Context mContext;

    public DanPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return DanFragment.newInstance(types[position]);
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