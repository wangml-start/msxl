package com.cgmn.msxl.ac;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.frag.ApproveFragment;
import com.cgmn.msxl.comp.frag.CommentFragment;
import com.google.android.material.tabs.TabLayout;


public class RelatedToMeActivity extends BaseOtherActivity {
    private TabLayout tabLayout;
    private ApproveFragment approveFragment;
    private CommentFragment commentFragment;
    private FragmentManager fManager;


    @Override
    protected void init(){
        bindView();
        onSelected(0);
    };

    @Override
    protected int getContentView() {
        return R.layout.related_to_me_layout;
    }

    @Override
    protected String setTitle(){
        return "与我相关";
    }

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
        tabLayout = findViewById(R.id.about_me_tab);
        fManager = getFragmentManager();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                onSelected(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab){
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab){
            }
        });

    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (approveFragment != null) fragmentTransaction.hide(approveFragment);
        if (commentFragment != null) fragmentTransaction.hide(commentFragment);
    }

    private void onSelected(Integer position){
        FragmentTransaction trian = fManager.beginTransaction();
        hideAllFragment(trian);
        if(position == 0){
            if (approveFragment == null) {
                approveFragment = ApproveFragment.newInstance();
                trian.add(R.id.ly_content, approveFragment);
            } else {
                trian.show(approveFragment);
            }
        }else if(position == 1){
            if (commentFragment == null) {
                commentFragment = CommentFragment.newInstance();
                trian.add(R.id.ly_content, commentFragment);
            } else {
                trian.show(commentFragment);
            }
        }
        trian.commit();
    }


}
