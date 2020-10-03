package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;

import java.util.ArrayList;

public class MutiLayoutAdapter extends BaseAdapter {

    //定义两个类别标志
    private static final int TYPE_SPLIT_ITEM = 0;
    private static final int TYPE_MAIN_ITEM = 1;
    private Context mContext;
    private ArrayList<Object> mData = null;


    public MutiLayoutAdapter(Context mContext, ArrayList<Object> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //多布局的核心，通过这个判断类别
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof PageMainItem) {
            return TYPE_MAIN_ITEM;
        } else if (mData.get(position) instanceof SplitItem) {
            return TYPE_SPLIT_ITEM;
        } else {
            return super.getItemViewType(position);
        }
    }

    //类别数目
    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        ItemViewHolder itemHolder = null;
        SplitViewHolder splitHolder = null;
        if(convertView == null){
            switch (type){
                case TYPE_MAIN_ITEM:
                    itemHolder = new ItemViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.main_list_item, parent, false);
                    itemHolder.img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
                    itemHolder.txt_aname = (TextView) convertView.findViewById(R.id.txt_aname);
                    convertView.setTag(R.id.tag_main_item,itemHolder);
                    break;
                case TYPE_SPLIT_ITEM:
                    splitHolder = new SplitViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.split_item, parent, false);
                    splitHolder.txt_split = (TextView) convertView.findViewById(R.id.txt_split_val);
                    convertView.setTag(R.id.Tag_split_item,splitHolder);
                    break;
            }
        }else{
            switch (type){
                case TYPE_MAIN_ITEM:
                    itemHolder = (ItemViewHolder) convertView.getTag(R.id.tag_main_item);
                    break;
                case TYPE_SPLIT_ITEM:
                    splitHolder = (SplitViewHolder) convertView.getTag(R.id.Tag_split_item);
                    break;
            }
        }

        Object obj = mData.get(position);
        //设置下控件的值
        switch (type){
            case TYPE_MAIN_ITEM:
                PageMainItem item = (PageMainItem) obj;
                if(item != null){
                    itemHolder.img_icon.setImageResource(item.getaIcon());
                    itemHolder.txt_aname.setText(item.getaName());
                }
                break;
            case TYPE_SPLIT_ITEM:
                SplitItem split = (SplitItem) obj;
                if(split != null){
                    splitHolder.txt_split.setText(split.getSplitText());
                }
                break;
        }
        return convertView;
    }


    //两个不同的ViewHolder
    private static class ItemViewHolder{
        ImageView img_icon;
        TextView txt_aname;
    }

    private static class SplitViewHolder{
        TextView txt_split;
    }

}
