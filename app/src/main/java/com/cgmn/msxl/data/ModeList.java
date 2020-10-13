package com.cgmn.msxl.data;

import com.cgmn.msxl.comp.swb.State;

import java.util.ArrayList;
import java.util.List;

public class ModeList {
    private List<SettingItem> list = null;
    private static ModeList model = null;

    private ModeList(){
        list = new ArrayList<>();
        list.add(new SettingItem(1001, "只参与多头趋势股票", State.OPEN.ordinal()));
        list.add(new SettingItem(1002, "突破10日最高点入场", State.CLOSE.ordinal()));
        list.add(new SettingItem(1003, "跌破10日最低点离场", State.CLOSE.ordinal()));
        list.add(new SettingItem(1004, "突破20日最高点入场", State.OPEN.ordinal()));
        list.add(new SettingItem(1005, "跌破20日最低点离场", State.OPEN.ordinal()));
        list.add(new SettingItem(1006, "短线持股不超过3天", State.CLOSE.ordinal()));
        list.add(new SettingItem(1007, "建仓不超过总仓位1/10", State.OPEN.ordinal()));
        list.add(new SettingItem(1008, "单只股票不超过总仓位4/10", State.OPEN.ordinal()));
        list.add(new SettingItem(1009, "亏损达到10%止损", State.OPEN.ordinal()));
    }

    public static ModeList getInstance(){
        if(model == null){
            model = new ModeList();
        }

        return model;
    }

    public List<SettingItem> getList(){
        return list;
    }
}
