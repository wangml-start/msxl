package com.cgmn.msxl.comp.frag;

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
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.ac.RealControlActivity;
import com.cgmn.msxl.ac.StatisticActivity;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;
import com.cgmn.msxl.data.StockHolder;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class StatisticFragment extends Fragment {
    private Context mContxt;
    private ListView list_content;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.static_fragment, container, false);
        bindView(view);
        initAdpter();

        return view;
    }

    private void initAdpter(){
        mData = new ArrayList<Object>();
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.train_sum_line), PageMainItem.SUM_PL_LINE));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.leading_line), PageMainItem.LEADING_LINE));
        mData.add(new PageMainItem(R.mipmap.head, getString(R.string.normal_line), PageMainItem.NORMARL_LINE));

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
                    Intent intent = null;
                    Bundle bundle = null;
                    switch (type){
                        case 6:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            startActivity(intent);
                            break;
                        case 7:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 8:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.NORMAL_STRATEGY);
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                    }
                }
            }
        });
    }
}
