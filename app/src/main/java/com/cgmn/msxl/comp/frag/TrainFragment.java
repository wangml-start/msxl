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
import com.cgmn.msxl.ac.*;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;
import com.cgmn.msxl.data.StockHolder;

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
        mData.add(new PageMainItem(R.drawable.main_1, getString(R.string.mode_setting), PageMainItem.MODEL_SETTING));
        mData.add(new PageMainItem(R.drawable.main_2, getString(R.string.dragTrain), PageMainItem.LEADING_STRATEGY));
        mData.add(new PageMainItem(R.drawable.main_3, getString(R.string.tradTrain), PageMainItem.NORMAL_STRATEGY));
        mData.add(new SplitItem(getString(R.string.pl_rate_line)));
//        mData.add(new PageMainItem(R.drawable.head, getString(R.string.train_sum_line), PageMainItem.SUM_PL_LINE));
        mData.add(new PageMainItem(R.drawable.main_4, getString(R.string.leading_line), PageMainItem.LEADING_LINE));
        mData.add(new PageMainItem(R.drawable.main_5, getString(R.string.normal_line), PageMainItem.NORMARL_LINE));
        mData.add(new SplitItem(getString(R.string.ranking)));
        mData.add(new PageMainItem(R.drawable.main_6, getString(R.string.total_ranking), PageMainItem.TOTAL_RANK));
//        mData.add(new PageMainItem(R.drawable.head, getString(R.string.day_ranking), PageMainItem.DAY_RANK));
        mData.add(new SplitItem(getString(R.string.mode_detail)));
        mData.add(new PageMainItem(R.drawable.main_7, getString(R.string.violate_mode_detai), PageMainItem.VIOLATE_MODE_DETAI));

        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(View view){
        mContxt = view.getContext();
        TextView txt_frg_tain = view.findViewById(R.id.txt_frg_tain);
        txt_frg_tain.setText(view.getResources().getString(R.string.up_train_txt));
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
                        case 0:
                            intent = new Intent(mContxt, ModeSettingActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(mContxt, RealControlActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(mContxt, RealControlActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.NORMAL_STRATEGY);
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
//                        case 3:
//                            intent = new Intent(mContxt, UserRankingActivity.class);
//                            bundle = new Bundle();
//                            bundle.putString("rank_type", "DAY");
//                            intent.putExtra("datas", bundle);
//                            startActivity(intent);
//                            break;
                        case 4:
                            intent = new Intent(mContxt, ViolateModeActivity.class);
                            startActivity(intent);
                            break;
                        case 5:
                            intent = new Intent(mContxt, UserRankingActivity.class);
                            bundle = new Bundle();
                            bundle.putString("rank_type", "SUM");
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 6:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putString("title", getString(R.string.train_sum_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 7:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            bundle.putString("title", getString(R.string.leading_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 8:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.NORMAL_STRATEGY);
                            bundle.putString("title", getString(R.string.normal_line));
                            bundle.putInt("user_model_id", 1);
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                    }
                }
            }
        });

        TextView txt_to_vip = view.findViewById(R.id.txt_to_vip);
        txt_to_vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContxt, VIPActivity.class);
                startActivity(intent);
            }
        });
    }
}
