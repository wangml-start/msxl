package com.cgmn.msxl.comp.adpter;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.CustmerToast;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactUsAdapter extends BaseAdapter {
    //定义两个类别标志
    private Context mContext;
    private List<String> mData = null;


    public ContactUsAdapter(Context mContext, List<String> mData) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder itemHolder = null;
        if (convertView == null) {
            itemHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_us_item, parent, false);
            itemHolder.txt_content = (TextView) convertView.findViewById(R.id.txt_content);
            itemHolder.txt_copy = (Button) convertView.findViewById(R.id.txt_copy);
            convertView.setTag(R.id.tag_edit_item, itemHolder);
        } else {
            itemHolder = (ItemViewHolder) convertView.getTag(R.id.tag_edit_item);
        }

        final String text = mData.get(position);
        itemHolder.txt_content.setText(text);
        //设置下控件的值
        itemHolder.txt_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pattern = "\\d+";
                // 创建 Pattern 对象
                Pattern reg = Pattern.compile(pattern);
                Matcher m = reg.matcher(text);
                if (m.find()) {
                    ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(m.group());
                    CustmerToast.makeText(mContext, "复制成功").show();
                }
            }
        });

        return convertView;
    }

    //两个不同的ViewHolder
    private static class ItemViewHolder {
        TextView txt_content;
        Button txt_copy;
    }
}
