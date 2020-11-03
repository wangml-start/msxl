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
import com.cgmn.msxl.ac.AccountInfoActivity;
import com.cgmn.msxl.ac.StatisticActivity;
import com.cgmn.msxl.comp.adpter.MutiLayoutAdapter;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.StockHolder;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class MayFragment extends Fragment {
    private Context mContxt;
    private ListView list_content;
    private ArrayList<Object> mData = null;
    private MutiLayoutAdapter myAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_fragment, container, false);
        bindView(view);
        initAdpter();
        return view;
    }

    private void initAdpter(){

        mData = new ArrayList<Object>();
        mData.add(new PageMainItem(R.drawable.main_2, getString(R.string.acct_info), PageMainItem.MY_GENERAL_INFO));
        mData.add(new PageMainItem(R.drawable.main_3, getString(R.string.vip_way), PageMainItem.VIP_INFO));

        myAdapter = new MutiLayoutAdapter(mContxt, mData);
        list_content.setAdapter(myAdapter);
    }

    private void bindView(View view){
        mContxt = view.getContext();
        TextView txt_frg_tain = view.findViewById(R.id.txt_frg_tain);
        txt_frg_tain.setText(view.getResources().getString(R.string.tab_menu_may));
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
                        case 9:
                            intent = new Intent(mContxt, AccountInfoActivity.class);
                            bundle = new Bundle();
                            bundle.putString("title", getString(R.string.train_sum_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                        case 10:
                            intent = new Intent(mContxt, StatisticActivity.class);
                            bundle = new Bundle();
                            bundle.putInt("train_type", StockHolder.LEADING_STRATEGY);
                            bundle.putString("title", getString(R.string.leading_line));
                            intent.putExtra("datas", bundle);
                            startActivity(intent);
                            break;
                    }
                }
            }
        });
    }
}
