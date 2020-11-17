package com.cgmn.msxl.page.related;

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
public class RelatedPagerAdapter extends FragmentPagerAdapter {
    private static final String[] TAB_TITLES = new String[]{"点赞我的", "评论我的"};
    private final Context mContext;

    public RelatedPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return ApproveFragment.newInstance();
        }else if(position == 1){
            return CommentFragment.newInstance();
        }
        return null;
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