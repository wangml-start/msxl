package com.cgmn.msxl.comp.frag;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.DisgussSubActivity;
import com.cgmn.msxl.comp.view.CommentExpandableListView;
import com.cgmn.msxl.comp.view.RefreshScrollView;
import com.cgmn.msxl.in.RefreshListener;
import com.cgmn.msxl.server_interface.RelatedToMe;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class RelatedFrgment extends Fragment implements RefreshListener {
    protected RefreshScrollView scrollView;
    protected CommentExpandableListView listView;
    protected RelativeLayout headView;
    protected TextView head_view_tv;
    protected Context mContext;
    protected Handler mHandler;
    protected List<RelatedToMe> mData = new ArrayList<>();
    protected BottomSheetDialog dialog;
    protected TabLayout tabLayout;

    protected Integer currentPos = 0;


    protected boolean appendList = false;
    public static String REFRESH = "REFRESH";
    public static String APPEND = "APPEND";
    protected String action = REFRESH;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.related_to_me_fragment, container, false);
        mContext = view.getContext();
        listView = view.findViewById(R.id.list_content);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                RelatedToMe entity = mData.get(groupPosition);
                Intent intent = new Intent(mContext, DisgussSubActivity.class);
                GlobalDataHelper.setDate("viewId", entity.getCommentId());
                startActivity(intent);
                return true;
            }
        });

        listView.setGroupIndicator(null);
        scrollView = view.findViewById(R.id.scrollView);
        headView = view.findViewById(R.id.head_view);
        head_view_tv = view.findViewById(R.id.head_view_tv);
        scrollView.setListsner(this);
        scrollView.setHeadView(headView);
        bindView(view);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;// 表示屏幕的像素宽度，单位是px（像素）
        scrollView.startRefresh(width, 100);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void startRefresh() {
        action = REFRESH;
        loadList(0);
    }

    @Override
    public void loadMore() {
        if (!appendList) {
            appendList = true;
            action = APPEND;
            loadList(mData.size());
        }
    }

    @Override
    public void hintChange(String hint) {
        head_view_tv.setText(hint);
    }

    protected abstract void bindView(View view);

    protected abstract void loadList(Integer start);

}
