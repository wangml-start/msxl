package com.cgmn.msxl.service;

import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.data.SettingItem;

import java.util.ArrayList;
import java.util.List;

public class ModeManager {
    private final static int ONLY_UP_TREND = 1001;
    private final static int BREAK_THROUTGH_MAX_TEN = 1002;
    private final static int FAIL_BELOW_MIN_TEN = 1003;
    private final static int BREAK_THROUTGH_MAX_TWENTY = 1004;
    private final static int FAIL_BELOW_MIN_TWENTY = 1005;
    private final static int SHORT_HOLD_BELOW_THREE_DAY = 1006;
    private final static int START_BUY_BELOW_TEN_PERCENT = 1007;
    private final static int TOTL_HOLD_BELOW_FOURTYY_PERCENT = 1008;
    private final static int STOP_LOSS_BY_TEN_PERCENT = 1009;

    private List<SettingItem> list = null;
    private static ModeManager model = null;
    private List<Integer> buylist = null;
    private List<Integer> holdList = null;

    private ModeManager(){
        list = new ArrayList<>();
        list.add(new SettingItem(ONLY_UP_TREND, "只参与多头趋势股票", State.OPEN.ordinal()));
        list.add(new SettingItem(BREAK_THROUTGH_MAX_TEN, "突破10日最高点入场", State.CLOSE.ordinal()));
        list.add(new SettingItem(FAIL_BELOW_MIN_TEN, "跌破10日最低点离场", State.CLOSE.ordinal()));
        list.add(new SettingItem(BREAK_THROUTGH_MAX_TWENTY, "突破20日最高点入场", State.OPEN.ordinal()));
        list.add(new SettingItem(FAIL_BELOW_MIN_TWENTY, "跌破20日最低点离场", State.OPEN.ordinal()));
        list.add(new SettingItem(SHORT_HOLD_BELOW_THREE_DAY, "短线持股不超过3天", State.CLOSE.ordinal()));
        list.add(new SettingItem(START_BUY_BELOW_TEN_PERCENT, "建仓不超过总仓位1/10", State.OPEN.ordinal()));
        list.add(new SettingItem(TOTL_HOLD_BELOW_FOURTYY_PERCENT, "单只股票不超过总仓位4/10", State.OPEN.ordinal()));
        list.add(new SettingItem(STOP_LOSS_BY_TEN_PERCENT, "亏损达到10%止损", State.OPEN.ordinal()));
    }

    public static ModeManager getInstance(){
        if(model == null){
            model = new ModeManager();
        }

        return model;
    }

    public List<SettingItem> getList(){
        return list;
    }

    public List<Integer> getBuyCheck(){
        if(buylist == null){
            buylist = new ArrayList<>();
        }
        buylist.add(ONLY_UP_TREND);
        buylist.add(BREAK_THROUTGH_MAX_TEN);
        buylist.add(BREAK_THROUTGH_MAX_TWENTY);
        buylist.add(START_BUY_BELOW_TEN_PERCENT);
        buylist.add(TOTL_HOLD_BELOW_FOURTYY_PERCENT);

        return buylist;
    }

    public List<Integer> getHoldCheck(){
        if(holdList == null){
            holdList = new ArrayList<>();
        }
        holdList.add(FAIL_BELOW_MIN_TEN);
        holdList.add(FAIL_BELOW_MIN_TWENTY);
        holdList.add(STOP_LOSS_BY_TEN_PERCENT);
        holdList.add(SHORT_HOLD_BELOW_THREE_DAY);

        return holdList;
    }
}
