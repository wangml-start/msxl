package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.bean.PopuBean;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopuWindowAdapter extends BaseAdapter {

    private Context mContext;
    List<PopuBean> list;
    private Map<Integer, View> views;
    private LayoutInflater inflater;

    public PopuWindowAdapter(Context mContext, List<PopuBean> lists) {
        this.mContext = mContext;
        this.list = lists;
        if (list == null) {
            list = new ArrayList<>();
        }
        inflater = LayoutInflater.from(mContext);
        views = new HashMap<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (!views.containsKey(position)) {
            convertView = inflater.inflate(R.layout.dialogui_popu_option_item, null);
            holder = new ViewHolder(convertView);
            PopuBean bean = list.get(position);
            holder.textView.setText(bean.getTitle());
            if(bean.getRes() != null && bean.getRes() > 0){
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageResource(bean.getRes());
            }

        } else {
            convertView = views.get(position);
        }
        return convertView;
    }

    class ViewHolder {
        TextView textView;
        ImageView image;

        public ViewHolder(View view){
            textView = view.findViewById(R.id.customui_item_text);
            image = view.findViewById(R.id.image_head);
        }
    }

}
