package com.cgmn.msxl.ac;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.bean.PopuBean;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.adpter.AccountAdapter;
import com.cgmn.msxl.comp.k.KlineStyle;
import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.comp.swb.SwitchButton;
import com.cgmn.msxl.comp.view.PopuWindowView;
import com.cgmn.msxl.data.EditInfoItem;
import com.cgmn.msxl.data.SelectionItem;
import com.cgmn.msxl.db.AppSqlHelper;
import com.cgmn.msxl.in.TdataListener;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.utils.AESUtil;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.ConstantHelper;
import com.cgmn.msxl.utils.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KLineSettingActivity extends BaseOtherActivity
        implements TextWatcher {
    private static final String TAG = KLineSettingActivity.class.getSimpleName();
    private Context mContext;

    private TextView txt_first_dex_list,txt_second_dex_list;
    private SwitchButton bt_sw,bt_voice_sw;

    private TextView count_minus,et_count,count_plus;
    private TextView count_minus_lb,et_count_lb,count_plus_lb;

    private EditText pos_1_up,pos_2_up,pos_3_up,pos_4_up,pos_5_up;
    private EditText pos_1_down,pos_2_down,pos_3_down,pos_4_down,pos_5_down;

    private OptionsPickerView firstOptions, secondOptions;
    private ArrayList<SelectionItem> firstList = new ArrayList<>(), secondList = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.system_setting_layout;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void init(){
        bindView();
        initOptions();
        loadSetting();
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initOptions(){
        firstList.add(new SelectionItem("MACD", "MACD", "FIRST_INDEX"));
        firstList.add(new SelectionItem("KDJ", "KDJ", "FIRST_INDEX"));
        firstList.add(new SelectionItem("VOL", "VOL", "FIRST_INDEX"));

        secondList.add(new SelectionItem("MACD", "MACD", "SECOND_INDEX"));
        secondList.add(new SelectionItem("KDJ", "KDJ", "SECOND_INDEX"));
        secondList.add(new SelectionItem("VOL", "VOL", "SECOND_INDEX"));

        firstOptions = creatOptionPicker(firstList);
        secondOptions = creatOptionPicker(secondList);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private OptionsPickerView creatOptionPicker(final ArrayList<SelectionItem> list) {
        OptionsPickerView ops = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                SelectionItem item = list.get(options1);
                if (item.getType().equals("FIRST_INDEX")) {
                    txt_first_dex_list.setText(item.getPickerViewText());
                } else if(item.getType().equals("SECOND_INDEX")){
                    txt_second_dex_list.setText(item.getPickerViewText());
                }
                //save
                upsertSetting(item.getType(), item.getPickerViewText());
            }
        })
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(getColor(R.color.colorPrimary))//设置分割线的颜色
                .setSelectOptions(0)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(getColor(R.color.div_white))
                .setCancelColor(getColor(R.color.colorPrimary))
                .setSubmitColor(getColor(R.color.colorPrimary))
                .setTextColorCenter(getColor(R.color.colorPrimary))
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideColor(getColor(R.color.div_white_bg)) //设置外部遮罩颜色
                .build();

        ops.setPicker(list);
        return ops;
    }

    @Override
    protected String setTitle(){
        return getString(R.string.kline_setting);
    };

    @Override
    protected boolean showRight(){
        return false;
    };
    @Override
    protected boolean showComplate(){
        return false;
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bindView(){
        mContext = this;
        txt_first_dex_list = findViewById(R.id.txt_first_dex_list);
        txt_second_dex_list = findViewById(R.id.txt_second_dex_list);

        bt_sw = findViewById(R.id.bt_sw);
        bt_voice_sw = findViewById(R.id.bt_voice_sw);
        count_minus = findViewById(R.id.count_minus);
        et_count = findViewById(R.id.et_count);
        count_plus = findViewById(R.id.count_plus);

        count_minus_lb = findViewById(R.id.count_minus_lb);
        et_count_lb = findViewById(R.id.et_count_lb);
        count_plus_lb = findViewById(R.id.count_plus_lb);

        View.OnClickListener ls = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCompclick(v);
            }
        };
        txt_first_dex_list.setOnClickListener(ls);
        txt_second_dex_list.setOnClickListener(ls);
        bt_sw.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton buttonView, final boolean isChecked) {
                final int status = isChecked ? State.OPEN :  State.CLOSE;
                upsertSetting("AUTO_NEXT_STEP", status+"");
            }
        });
        bt_voice_sw.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton buttonView, final boolean isChecked) {
                final int status = isChecked ? State.OPEN :  State.CLOSE;
                upsertSetting("PLAY_VOICE", status+"");
            }
        });
        count_minus.setOnClickListener(ls);
        count_plus.setOnClickListener(ls);

        count_minus_lb.setOnClickListener(ls);
        count_plus_lb.setOnClickListener(ls);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        setKlineBaseDatas(screenHeight);

        Drawable right = getResources().getDrawable(R.drawable.down);
        right.setBounds(0, 0, (int) (10* KlineStyle.pxScaleRate),
                (int) (10*KlineStyle.pxScaleRate));//必须设置图片的大小否则没有作用
        Drawable wrappedDrawable = DrawableCompat.wrap(right);
        DrawableCompat.setTint(wrappedDrawable, getColor(R.color.colorPrimary));

        //设置图片left这里如果是右边就放到第二个参数里面依次对应
        txt_first_dex_list.setCompoundDrawables(null, null, wrappedDrawable, null);
        txt_second_dex_list.setCompoundDrawables(null, null, wrappedDrawable, null);

        pos_1_up = findViewById(R.id.pos_1_up);
        pos_2_up = findViewById(R.id.pos_2_up);
        pos_3_up = findViewById(R.id.pos_3_up);
        pos_4_up = findViewById(R.id.pos_4_up);
        pos_5_up = findViewById(R.id.pos_5_up);
        pos_1_down = findViewById(R.id.pos_1_down);
        pos_2_down = findViewById(R.id.pos_2_down);
        pos_3_down = findViewById(R.id.pos_3_down);
        pos_4_down = findViewById(R.id.pos_4_down);
        pos_5_down = findViewById(R.id.pos_5_down);
        pos_1_up.addTextChangedListener(this);
        pos_2_up.addTextChangedListener(this);
        pos_3_up.addTextChangedListener(this);
        pos_4_up.addTextChangedListener(this);
        pos_5_up.addTextChangedListener(this);
        pos_1_down.addTextChangedListener(this);
        pos_2_down.addTextChangedListener(this);
        pos_3_down.addTextChangedListener(this);
        pos_4_down.addTextChangedListener(this);
        pos_5_down.addTextChangedListener(this);
    }

    private void setKlineBaseDatas(float height){
        KlineStyle.initSize();
        float baseHeight = 480f;
        KlineStyle.pxScaleRate = height/baseHeight;
        KlineStyle.resetSize();
    }

    protected void onCompclick(View v){
        if(v.getId() == R.id.txt_first_dex_list){
            firstOptions.show();
        }else if(v.getId() == R.id.txt_second_dex_list){
            secondOptions.show();
        }else if(v.getId() == R.id.count_minus){
            calcTime(et_count, -1, "TREND_TIME");
        }else if(v.getId() == R.id.count_minus_lb){
            calcTime(et_count_lb, -1, "SHORT_TIME");
        }else if(v.getId() == R.id.count_plus){
            calcTime(et_count, 1, "TREND_TIME");
        }else if(v.getId() == R.id.count_plus_lb){
            calcTime(et_count_lb, 1, "SHORT_TIME");
        }
    }

    private void loadSetting(){
        String firstIndex = "MACD", secondIndex="VOL";
        Integer autoNext = 0,playVoice=0;
        Integer trendTime = 3, shortTime = 5;

        final AppSqlHelper dbHelper = new AppSqlHelper(mContext);
        Map<String, String> map =  dbHelper.getSystenSettings();
        if(!CommonUtil.isEmpty(map.get("FIRST_INDEX"))){
            firstIndex = map.get("FIRST_INDEX");
        }
        if(!CommonUtil.isEmpty(map.get("SECOND_INDEX"))){
            secondIndex = map.get("SECOND_INDEX");
        }
        if(!CommonUtil.isEmpty(map.get("AUTO_NEXT_STEP"))){
            autoNext = Integer.valueOf(map.get("AUTO_NEXT_STEP"));
        }
        if(!CommonUtil.isEmpty(map.get("PLAY_VOICE"))){
            playVoice = Integer.valueOf(map.get("PLAY_VOICE"));
        }
        if(!CommonUtil.isEmpty(map.get("TREND_TIME"))){
            trendTime = Integer.valueOf(map.get("TREND_TIME"));
        }
        if(!CommonUtil.isEmpty(map.get("SHORT_TIME"))){
            shortTime = Integer.valueOf(map.get("SHORT_TIME"));
        }

        txt_first_dex_list.setText(firstIndex);
        txt_second_dex_list.setText(secondIndex);
        bt_sw.changeStatus(autoNext);
        bt_voice_sw.changeStatus(playVoice);
        et_count.setText(trendTime+"");
        et_count_lb.setText(shortTime+"");

        if(!CommonUtil.isEmpty(map.get("FIRST_POS"))){
            String[] arr1 = map.get("FIRST_POS").toString().split(ConstantHelper.positionSplit);
            if(arr1.length == 2){
                pos_1_up.setText(arr1[0]);
                pos_1_down.setText(arr1[1]);
            }
        }
        if(!CommonUtil.isEmpty(map.get("SECOND_POS"))){
            String[] arr2 = map.get("SECOND_POS").toString().split(ConstantHelper.positionSplit);
            if(arr2.length == 2){
                pos_2_up.setText(arr2[0]);
                pos_2_down.setText(arr2[1]);
            }
        }
        if(!CommonUtil.isEmpty(map.get("THIRD_POS"))){
            String[] arr3 = map.get("THIRD_POS").toString().split(ConstantHelper.positionSplit);
            if(arr3.length == 2){
                pos_3_up.setText(arr3[0]);
                pos_3_down.setText(arr3[1]);
            }
        }
        if(!CommonUtil.isEmpty(map.get("FOUR_POS"))){
            String[] arr4 = map.get("FOUR_POS").toString().split(ConstantHelper.positionSplit);
            if(arr4.length == 2){
                pos_4_up.setText(arr4[0]);
                pos_4_down.setText(arr4[1]);
            }
        }
        if(!CommonUtil.isEmpty(map.get("FIVE_POS"))){
            String[] arr5 = map.get("FIVE_POS").toString().split(ConstantHelper.positionSplit);
            if(arr5.length == 2){
                pos_5_up.setText(arr5[0]);
                pos_5_down.setText(arr5[1]);
            }
        }
    }

    private void calcTime(TextView v , Integer direction, String field){
        String value = v.getText().toString();
        if(!CommonUtil.isEmpty(value)){
            Integer base = Integer.valueOf(value);
            Integer changed = base + 1*direction;
            v.setText(changed+"");

            upsertSetting(field, changed+"");
        }
    }

    private void upsertSetting(final String field, final String value){
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                AppSqlHelper dbHelper = new AppSqlHelper(mContext);
                ContentValues values = new ContentValues();
                values.put("setting_name", field);
                values.put("setting_value", value);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);
            }
        });
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try{ Integer num = Integer.valueOf(s.toString());
            if(num <= 0){
                CustmerToast.makeText(mContext, "无效参数").show();
                return;
            }
            if(validNum()){
                savePositions();
            }
        }catch (Exception e){

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean validNum(){
        if(CommonUtil.isEmpty(pos_1_up.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_2_up.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_3_up.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_4_up.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_5_up.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_1_down.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_2_down.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_3_down.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_4_down.getText().toString())){
            return false;
        }
        if(CommonUtil.isEmpty(pos_5_down.getText().toString())){
            return false;
        }
        return true;
    }

    private void savePositions(){
        final String first_pos = String.format("%s%s%s", pos_1_up.getText().toString(), ConstantHelper.positionSplit,pos_1_down.getText().toString());
        final String second_pos = String.format("%s%s%s", pos_2_up.getText().toString(),ConstantHelper.positionSplit,pos_2_down.getText().toString());
        final String third_pos = String.format("%s%s%s", pos_3_up.getText().toString(),ConstantHelper.positionSplit,pos_3_down.getText().toString());
        final String four_pos = String.format("%s%s%s", pos_4_up.getText().toString(),ConstantHelper.positionSplit,pos_4_down.getText().toString());
        final String five_pos = String.format("%s%s%s", pos_5_up.getText().toString(),ConstantHelper.positionSplit,pos_5_down.getText().toString());

        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                AppSqlHelper dbHelper = new AppSqlHelper(mContext);
                ContentValues values = new ContentValues();
                values.put("setting_name", "FIRST_POS");
                values.put("setting_value", first_pos);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);

                values.put("setting_name", "SECOND_POS");
                values.put("setting_value", second_pos);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);

                values.put("setting_name", "THIRD_POS");
                values.put("setting_value", third_pos);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);

                values.put("setting_name", "FOUR_POS");
                values.put("setting_value", four_pos);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);

                values.put("setting_name", "FIVE_POS");
                values.put("setting_value", five_pos);
                dbHelper.upsert("user_settings", values, "setting_name");
                GlobalDataHelper.updateUser(mContext);
            }
        });
    }
}