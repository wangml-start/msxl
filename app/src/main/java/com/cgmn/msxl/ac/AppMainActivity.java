package com.cgmn.msxl.ac;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.cgmn.msxl.R;
import com.cgmn.msxl.comp.GuideIconView;
import com.cgmn.msxl.comp.frag.MayFragment;
import com.cgmn.msxl.comp.frag.DisCussFragment;
import com.cgmn.msxl.comp.frag.TrainFragment;

public class AppMainActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = AppMainActivity.class.getSimpleName();
    private Context mContxt;
    //UI Object
    private GuideIconView txt_xunlian, txt_guba, txt_may;
    private FrameLayout ly_content;

    //Fragment Object
    private TrainFragment trainFrag;
    private DisCussFragment disCussFragment;
    private MayFragment myFrag;
    private FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);
        mContxt = this;
        bindAppMainView();
        fManager = getFragmentManager();
        txt_xunlian.performClick();
    }


    private void bindAppMainView() {
        mContxt = this;
        txt_guba =  findViewById(R.id.txt_guba);
        txt_xunlian = findViewById(R.id.txt_xunlian);
        txt_may =  findViewById(R.id.txt_may);
        ly_content = (FrameLayout) findViewById(R.id.ly_content);

        txt_xunlian.setOnClickListener(this);
        txt_guba.setOnClickListener(this);
        txt_may.setOnClickListener(this);
    }

    //重置所有文本的选中状态
    private void reset() {
        txt_xunlian.setIconAlpha(0);
        txt_guba.setIconAlpha(0);
        txt_may.setIconAlpha(0);
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (trainFrag != null) fragmentTransaction.hide(trainFrag);
        if (disCussFragment != null) fragmentTransaction.hide(disCussFragment);
        if (myFrag != null) fragmentTransaction.hide(myFrag);
    }


    @Override
    public void onClick(View v) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (v.getId()) {
            case R.id.txt_xunlian:
                reset();
                txt_xunlian.setIconAlpha(1);
                if (trainFrag == null) {
                    trainFrag = new TrainFragment();
                    fTransaction.add(R.id.ly_content, trainFrag);
                } else {
                    fTransaction.show(trainFrag);
                }
                break;
            case R.id.txt_guba:
                reset();
                txt_guba.setIconAlpha(1);
                if (disCussFragment == null) {
                    disCussFragment = new DisCussFragment();
                    fTransaction.add(R.id.ly_content, disCussFragment);
                } else {
                    fTransaction.show(disCussFragment);
                }
                break;
            case R.id.txt_may:
                reset();
                txt_may.setIconAlpha(1);
                if (myFrag == null) {
                    myFrag = new MayFragment();
                    fTransaction.add(R.id.ly_content, myFrag);
                } else {
                    fTransaction.show(myFrag);
                }
                break;
        }
        fTransaction.commit();
    }
}
