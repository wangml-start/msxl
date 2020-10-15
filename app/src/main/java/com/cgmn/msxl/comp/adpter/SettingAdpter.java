package com.cgmn.msxl.comp.adpter;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.comp.swb.SwitchButton;
import com.cgmn.msxl.data.SettingItem;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.service.GlobalDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingAdpter extends BaseAdapter {
    private Context mContext;
    private List<SettingItem> mData = null;
    private Map<Integer, View> views = null;

    public SettingAdpter(Context mContext, List<SettingItem> mData) {
        this.mContext = mContext;
        this.mData = mData;
        views = new HashMap<>();
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
        Object obj = mData.get(position);
        //设置下控件的值
        SettingItem item = (SettingItem) obj;
        SettingViewHolder holder = null;
        if(!views.containsKey(position)){
            holder = new SettingViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.mode_settin_item, parent, false);
            holder.txt = convertView.findViewById(R.id.txt_des);
            holder.switchButton = convertView.findViewById(R.id.bt_sw);
//            convertView.setTag(holder);
            views.put(position, convertView);
//            holder.switchButton.setName(item.getModeText());

            if(item != null){
                final String modelType = item.getModedType()+"";
                holder.txt.setText(item.getModeText());
                holder.switchButton.changeStatus(item.getState());
                if(holder.switchButton.listenrtIsBlank()){
                    holder.switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(SwitchButton buttonView, final boolean isChecked) {
                            final int status = isChecked ? State.OPEN :  State.CLOSE;
                            GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
                                @Override
                                public void run() {
                                    AppSqlHelper sqlHeper = new AppSqlHelper(mContext);
                                    Map<String, Object> map = GlobalDataHelper.getUser(mContext);
                                    ContentValues values = new ContentValues();
                                    values.put("user_id", (String) map.get("id"));
                                    values.put("mode_type", modelType);
                                    values.put("model_status", status+"");
                                    sqlHeper.upsert("user_modes",
                                            values, "mode_type", String.format(" AND user_id=%s", map.get("id")));
                                }
                            });
                        }
                    });
                }
            }
        }else{
            convertView = views.get(position);
        }
        return convertView;
    }

    private static class SettingViewHolder{
        TextView txt;
        SwitchButton switchButton;
    }


}
