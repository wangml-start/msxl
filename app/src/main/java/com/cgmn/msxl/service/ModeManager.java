package com.cgmn.msxl.service;

import android.util.Log;
import com.cgmn.msxl.comp.k.KLine;
import com.cgmn.msxl.comp.swb.State;
import com.cgmn.msxl.data.SettingItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private ModeManager() {
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

    public static ModeManager getInstance() {
        if (model == null) {
            model = new ModeManager();
        }

        return model;
    }

    public List<SettingItem> getList() {
        return list;
    }

    public List<Integer> getBuyCheck() {
        if (buylist == null) {
            buylist = new ArrayList<>();
        }
        buylist.add(ONLY_UP_TREND);
        buylist.add(BREAK_THROUTGH_MAX_TEN);
        buylist.add(BREAK_THROUTGH_MAX_TWENTY);
        buylist.add(START_BUY_BELOW_TEN_PERCENT);
        buylist.add(TOTL_HOLD_BELOW_FOURTYY_PERCENT);

        return buylist;
    }

    public List<Integer> getHoldCheck() {
        if (holdList == null) {
            holdList = new ArrayList<>();
        }
        holdList.add(FAIL_BELOW_MIN_TEN);
        holdList.add(FAIL_BELOW_MIN_TWENTY);
        holdList.add(STOP_LOSS_BY_TEN_PERCENT);
        holdList.add(SHORT_HOLD_BELOW_THREE_DAY);

        return holdList;
    }

    /**
     * 判断是否属于多头趋势
     *
     * @param params
     * @return
     */
    public boolean isUpTrend(Map<String, Object> params) {
        LinkedList<KLine> nodes = (LinkedList<KLine>) params.get("nodes");
        KLine currentKline = nodes.getLast();
        boolean flag = false;
        if (currentKline.avg10 > currentKline.avg20) {
            flag = true;
        }
        return flag;
    }

    /**
     * 昨日、当日收盘价突破
     *
     * @param days
     * @param params
     * @return
     */
    public boolean isBreakThrough(int days, Map<String, Object> params) {
        LinkedList<KLine> nodes = (LinkedList<KLine>) params.get("nodes");
        KLine yesterdayKline = nodes.get(nodes.size()-2);
        KLine currentKline = nodes.getLast();
        String status = (String) params.get("kStatus");
        int length = nodes.size();
        float maxHigh = 0.0f;
        //默认为开盘状态
        int startIndex = (length - days - 3);
        int endIndex = (length - 3);
        if (status.equals(RealTradeManage.CLOSE)) {
            startIndex = (length - days - 2);
            endIndex = (length - 2);
        }
        for (; startIndex <= endIndex; startIndex++) {
            KLine node = nodes.get(startIndex);
            if (node.high > maxHigh) {
                maxHigh = node.high;
            }
        }
        if (status.equals(RealTradeManage.CLOSE)) {
            return currentKline.close > maxHigh;
        } else {
            return yesterdayKline.close > maxHigh;
        }
    }

    /**
     * 收盘价跌破
     *
     * @param days
     * @param params
     * @return
     */
    public boolean isFailBelow(int days, Map<String, Object> params) {
        LinkedList<KLine> nodes = (LinkedList<KLine>) params.get("nodes");
        KLine currentKline = nodes.getLast();
        int length = nodes.size();
        float minLow = Float.MAX_VALUE;
        //默认为开盘状态
        int startIndex = (length - days - 2);
        int endIndex = (length - 2);
        for (; startIndex <= endIndex; startIndex++) {
            KLine node = nodes.get(startIndex);
            if (node.low < minLow) {
                minLow = node.low;
            }
        }
        Log.d("############Min:", minLow+"");
        Log.d("####currentKline.close:", currentKline.close+"");
        return currentKline.close < minLow;
    }

    /**
     * 操盘手在执行操作时是否违反模式规则
     * @param modeType
     * @param params
     * @return
     */
    public boolean assertionOverMode(int modeType, Map<String, Object> params) {
        boolean isOver = false;
        switch (modeType) {
            case ONLY_UP_TREND:
                isOver = !isUpTrend(params);
                break;
            case BREAK_THROUTGH_MAX_TEN:
                isOver = !isBreakThrough(10, params);
                break;
            case FAIL_BELOW_MIN_TEN:
                boolean holdStock1 = (boolean) params.get("holdStock");
                isOver = isFailBelow(10, params) && holdStock1;
                break;
            case BREAK_THROUTGH_MAX_TWENTY:
                isOver = !isBreakThrough(20, params);
                break;
            case FAIL_BELOW_MIN_TWENTY:
                boolean holdStock2 = (boolean) params.get("holdStock");
                isOver = isFailBelow(20, params) && holdStock2;
                break;
            case SHORT_HOLD_BELOW_THREE_DAY:
                int holdDay = (int) params.get("holdDay");
                boolean holdStock4 = (boolean) params.get("holdStock");
                isOver = holdDay > 3 && holdStock4;
                break;
            case START_BUY_BELOW_TEN_PERCENT:
                boolean isCreateHold = (boolean) params.get("isCreateHold");
                float startRate = (float) params.get("startRate");
                isOver = startRate > 0.1 && isCreateHold;
                break;
            case TOTL_HOLD_BELOW_FOURTYY_PERCENT:
                float totalRate = (float) params.get("totalRate");
                isOver = totalRate > 0.4;
                break;
            case STOP_LOSS_BY_TEN_PERCENT:
                boolean holdStock3 = (boolean) params.get("holdStock");
                float lossRate = (float) params.get("lossRate");
                isOver = lossRate < -0.1 && holdStock3;
                break;
        }

        return isOver;
    }
}
