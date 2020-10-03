package com.cgmn.msxl.comp;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class TrainFragment extends Fragment {
    private Context mContxt;
    private ListView list_content;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.train_fragment, container, false);
        list_content = view.findViewById(R.id.list_content);
        mContxt = view.getContext();
        mData = new ArrayList<Object>();

        mData.add(new SplitItem(getString(R.string.trading)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.mode_setting)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.dragTrain)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.tradTrain)));

        mData.add(new SplitItem(getString(R.string.ranking)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.day_ranking)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.month_ranking)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.total_ranking)));

        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);

        return view;
    }
}
