package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.view.NetImageView;
import com.cgmn.msxl.data.EditInfoItem;
import com.cgmn.msxl.service.GlobalDataHelper;

import java.util.ArrayList;
import java.util.Map;

public class AccountAdapter extends BaseAdapter {

    //定义两个类别标志
    private static final int TYPE_HEAD_IMG = 0;
    private static final int TYPE_EDIT_CONTENT = 1;
    private Context mContext;
    private ArrayList<Object> mData = null;


    public AccountAdapter(Context mContext, ArrayList<Object> mData) {
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
        EditInfoItem item = (EditInfoItem) mData.get(position);
        if (item.getaIcon() > 0) {
            return TYPE_HEAD_IMG;
        } else if (item.getaIcon() == 0) {
            return TYPE_EDIT_CONTENT;
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
        HeadViewHolder headHolder = null;
        Map<String, Object> userInfo = GlobalDataHelper.getUser(mContext);
        if (convertView == null) {
            switch (type) {
                case TYPE_EDIT_CONTENT:
                    itemHolder = new ItemViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.edit_item, parent, false);
                    itemHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
                    itemHolder.txt_content = (TextView) convertView.findViewById(R.id.txt_content);
                    convertView.setTag(R.id.tag_edit_item, itemHolder);
                    break;
                case TYPE_HEAD_IMG:
                    headHolder = new HeadViewHolder();
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.head_im_item, parent, false);
                    headHolder.img_icon = (NetImageView) convertView.findViewById(R.id.img_head_img);
                    headHolder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
                    convertView.setTag(R.id.tag_head_item, headHolder);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_EDIT_CONTENT:
                    itemHolder = (ItemViewHolder) convertView.getTag(R.id.tag_edit_item);
                    break;
                case TYPE_HEAD_IMG:
                    headHolder = (HeadViewHolder) convertView.getTag(R.id.tag_head_item);
                    break;
            }
        }

        Object obj = mData.get(position);
        //设置下控件的值
        EditInfoItem item = (EditInfoItem) obj;
        switch (type) {
            case TYPE_HEAD_IMG:
                if (item != null) {
                    byte[] cut = GlobalDataHelper.getUserCut(mContext);
                    if(cut != null && cut.length > 0){
                        headHolder.img_icon.setImageContent(cut);
                    }else{
                        headHolder.img_icon.setImageName(GlobalDataHelper.getUserAcc(mContext));
                        headHolder.img_icon.setImageURL(GlobalDataHelper.getUserPortraitUrl(mContext));
                    }
                    headHolder.txt_title.setText(item.getTitle());
                }
                break;
            case TYPE_EDIT_CONTENT:
                itemHolder.txt_title.setText(item.getTitle());
                if (!"password".equals(item.getField_data())) {
                    if ("gender".equals(item.getField_data())) {
                        String re = renderGender((String) userInfo.get(item.getField_data()), convertView);
                        itemHolder.txt_content.setText(re);
                    } else {
                        itemHolder.txt_content.setText((String) userInfo.get(item.getField_data()));
                    }
                }

                break;
        }
        return convertView;
    }

    public static String renderGender(String type, View view) {
        String res = "";
        if ("0".equals(type)) {
            res = view.getResources().getString(R.string.man);
        } else if ("1".equals(type)) {
            res = view.getResources().getString(R.string.weman);
        }
        return res;
    }


    //两个不同的ViewHolder
    private static class ItemViewHolder {
        TextView txt_title;
        TextView txt_content;
    }

    private static class HeadViewHolder {
        TextView txt_title;
        NetImageView img_icon;
    }

}
