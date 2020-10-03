package com.cgmn.msxl.comp;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.NormalStrategyActivity;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class TrainFragment extends Fragment{
    private Context mContxt;
    private ListView list_content;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.train_fragment, container, false);
        bindView(view);
        initAdpter();

        return view;
    }

    private void initAdpter(){
        mData = new ArrayList<Object>();
        mData.add(new SplitItem(getString(R.string.trading)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.mode_setting), PageMainItem.MODEL_SETTING));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.dragTrain), PageMainItem.LEADING_STRATEGY));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.tradTrain), PageMainItem.NORMAL_STRATEGY));
        mData.add(new SplitItem(getString(R.string.ranking)));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.day_ranking), PageMainItem.DAY_RANK));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.month_ranking), PageMainItem.MONTH_RANK));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.total_ranking), PageMainItem.TOTAL_RANK));

        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(View view){
        mContxt = view.getContext();
        list_content = view.findViewById(R.id.list_content);
        list_content.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = mData.get(position);
                if( object instanceof PageMainItem){
                    int type = ((PageMainItem)object).getItemType();
                    switch (type){
                        case 0:
                        case 1:
                        case 2:
                            startActivity(new Intent(mContxt, NormalStrategyActivity.class));
                            break;
                        case 3:

                    }
                }
            }
        });
    }
}
