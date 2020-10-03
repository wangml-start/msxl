package com.cgmn.msxl.ac;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.TrainFragment;
import com.cgmn.msxl.utils.MessageUtil;

public class AppMainActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = AppMainActivity.class.getSimpleName();
    private Context mContxt;
    //UI Object
    private TextView txt_xunlian;
    private TextView txt_statistic;
    private TextView txt_may;
    private FrameLayout ly_content;

    //消息处理
    private Handler mHandler;

    //Fragment Object
    private TrainFragment fg1,fg2,fg3,fg4;
    private FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);
        mContxt = this;
        initMessageHandle();
        bindAppMainView();
        fManager = getFragmentManager();
        txt_xunlian.performClick();
    }


    private void bindAppMainView() {
        mContxt = this;
        txt_xunlian = (TextView) findViewById(R.id.txt_xunlian);
        txt_statistic = (TextView) findViewById(R.id.txt_statistic);
        txt_may = (TextView) findViewById(R.id.txt_may);
        ly_content = (FrameLayout) findViewById(R.id.ly_content);

        txt_xunlian.setOnClickListener(this);
        txt_statistic.setOnClickListener(this);
        txt_may.setOnClickListener(this);
    }

    //重置所有文本的选中状态
    private void setSelected(){
        txt_xunlian.setSelected(false);
        txt_statistic.setSelected(false);
        txt_may.setSelected(false);
    }

    private void initMessageHandle() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {

                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {

                }
                return false;
            }
        });
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(fg1 != null)fragmentTransaction.hide(fg1);
        if(fg2 != null)fragmentTransaction.hide(fg2);
        if(fg3 != null)fragmentTransaction.hide(fg3);
        if(fg4 != null)fragmentTransaction.hide(fg4);
    }


    @Override
    public void onClick(View v) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (v.getId()){
            case R.id.txt_xunlian:
                setSelected();
                txt_xunlian.setSelected(true);
                if(fg1 == null){
                    fg1 = new TrainFragment();
                    fTransaction.add(R.id.ly_content,fg1);
                }else{
                    fTransaction.show(fg1);
                }
                break;
            case R.id.txt_statistic:
                setSelected();
                txt_statistic.setSelected(true);
                if(fg2 == null){
//                    fg2 = new TrainFragment("第二个Fragment");
                    fTransaction.add(R.id.ly_content,fg2);
                }else{
                    fTransaction.show(fg2);
                }
                break;
            case R.id.txt_may:
                setSelected();
                txt_may.setSelected(true);
                if(fg3 == null){
//                    fg3 = new TrainFragment();
                    fTransaction.add(R.id.ly_content,fg3);
                }else{
                    fTransaction.show(fg3);
                }
                break;
        }
        fTransaction.commit();
    }
}
