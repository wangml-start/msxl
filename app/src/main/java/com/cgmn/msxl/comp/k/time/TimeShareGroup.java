package com.cgmn.msxl.comp.k.time;

import android.util.Log;

import com.cgmn.msxl.comp.k.index.MACDCalculation;
import com.cgmn.msxl.server_interface.TimeShare;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeShareGroup {

    public Float lastClosePrice;
    private List<Float> show_close_price = new ArrayList<>();
    //分时均价计算
    Float totAmt=0f;
    Float totVol=0f;

    public MinuteCounter timer;
    Integer startNum = 93000;

    public TimeShare current, last;

    List<TimeShare> showList = new ArrayList<>();
    List<TimeShare> leftList = new ArrayList<>();

    List<CMinute> timePrices = new ArrayList<>();
    List<CMinute> solidPrices = new ArrayList<>();

    private Map<String, Integer> exist = new HashMap<>();
    Integer leftIndex=0;
    float priceDelta,deltaVol=0;

    float mYMaxMacd = 0.0f;
    float mYMinMacd = 0.0f;

    public boolean isValidData = false;


    public Boolean init(List<TimeShare> list){
        if(CommonUtil.isEmpty(list)){
            isValidData = false;
            return false;
        }
        timer = new MinuteCounter();
        timer.setTime("093000");

        Integer index = 0;
        for(TimeShare item : list){
            index++;
            String timeStr = item.getTimeStr();
            if(CommonUtil.isEmpty(timeStr)){
                continue;
            }
            if(index == 1){
                if(item.getLastClose() != null && item.getLastClose() > 0){
                    lastClosePrice = item.getLastClose();
                }else{
                    return false;
                }
            }
            if(Integer.valueOf(timeStr) <= startNum){
                if(item.getVolume() > 0){
                    showList.add(item);
                    current = item;
                    show_close_price.add(item.getPrice());
                }
            }else{
                leftList.addAll(list.subList(index-1, list.size()));
                break;
            }
        }
        if(current == null){
            Log.i("Current NUll","CODE: " + list.get(0).getStockCode() + " Date: " + list.get(0).getTradeDate());
            return false;
        }

        updateCurrentNode("0925");
        calExtremeNum();
        calcMacdDatas();
        isValidData = true;
        return true;
    }

    public void calcMacdDatas(){
        Float number = 99999f;
        mYMaxMacd = number * -1;
        mYMinMacd = number;

        Map<String, List<Float>> map = MACDCalculation.MACD(12, 26, 9, show_close_price);
        int length = showList.size();
        int i = 0;
        while(i < length){
            TimeShare entry = showList.get(i);
            entry.dif = (map.get("dif").get(i));
            entry.dea = (map.get("dea").get(i));
            entry.macd = (map.get("macd").get(i));

            if(entry.dif > mYMaxMacd){
                mYMaxMacd = entry.dif;
            }
            if(entry.dea > mYMaxMacd){
                mYMaxMacd = entry.dea;
            }
            if(entry.macd > mYMaxMacd){
                mYMaxMacd = entry.macd;
            }

            if(entry.dif < mYMinMacd){
                mYMinMacd = entry.dif;
            }
            if(entry.dea < mYMinMacd){
                mYMinMacd = entry.dea;
            }
            if(entry.macd < mYMinMacd){
                mYMinMacd = entry.macd;
            }

            i += 1;
        }
    }



    //计算最大绝对值
    public void calExtremeNum(){
        Float delta = Math.abs(current.getPrice() - lastClosePrice);
        for(TimeShare item : showList){
            String timeStr = item.getTimeStr();
            if(CommonUtil.isEmpty(timeStr)){
                continue;
            }
//            if(Integer.valueOf(timeStr) < startNum){
//                continue;
//            }
            Float temp = Math.abs(item.getPrice() - lastClosePrice);
            if(temp > delta){
                delta = temp;
            }
        }
        priceDelta = delta;
        for(CMinute min : timePrices){
            if(min.vol > deltaVol){
                deltaVol = min.vol;
            }
        }
    }


    public void onNextStep(){
        if(timer.isOver){
            return;
        }
        last = current;
        timer.nextStep();
        String time = timer.getTimeStr();
        while(leftIndex<leftList.size()){
            TimeShare item = leftList.get(leftIndex);
            String key = item.getTimeStr();
            if(exist.containsKey(key)){
                leftIndex++;
                continue;
            }
            if(Integer.valueOf(key) <= Integer.valueOf(time)){
                if(item.getVolume() > 0){
                    showList.add(item);
                    current = item;
                    exist.put(key, 1);
                }

            }else{
                break;
            }
            leftIndex++;
        }
        updateCurrentNode(null);
        calExtremeNum();
    }

    /**
     * 一分钟数据时候
     */
    public void onNextMinu(){
        if(timer.isOver){
            return;
        }
        last = current;
        timer.nextMinutes();
        String time = timer.getTimeStr();
        if(leftIndex>=leftList.size()){
            return;
        }
        TimeShare item = leftList.get(leftIndex);
        String key = item.getTimeStr();
        if(key.compareTo("113000") > 0 && key.compareTo("130000") < 0){
            key = "113000";
        }
        int idxTime = Integer.valueOf(key), cTime = Integer.valueOf(time);
        if(idxTime == cTime){
            showList.add(item);
            show_close_price.add(item.getPrice());
            current = item;
            leftIndex += 1;
        }else if(cTime < idxTime && leftIndex > 0){ //存在改时间没有交易的情况
            TimeShare pre_item = leftList.get(leftIndex-1);
            TimeShare new_item = new TimeShare();
            new_item.setVolume(1);
            new_item.setPrice(pre_item.getPrice());
            new_item.setAmt(0);
            new_item.setStockCode(pre_item.getStockCode());
            new_item.setTimeStr(time);
            new_item.setLastClose(pre_item.getLastClose());
            new_item.setTradeDate(pre_item.getTradeDate());
            showList.add(new_item);
            show_close_price.add(new_item.getPrice());
            current = new_item;
        }
        updateCurrentNode(null);
        calExtremeNum();
        calcMacdDatas();
    }


    public void updateCurrentNode(String time){
        if(last == null || !last.getTimeStr().equals(current.getTimeStr())){
            totAmt += current.getAmt();
            int uom = 100;
            if(CommonUtil.isKzz(current.getStockCode())){
                uom = 10;
            }
            totVol += uom * current.getVolume();
        }
        if(time == null){
            time = timer.getTimeMinute();
        }
        updateMinuteNode(timePrices, current.getPrice(), time,current.getVolume());
        updateMinuteNode(solidPrices, totAmt/totVol, time,current.getVolume());
    }

    public void updateMinuteNode(List<CMinute> list, float price, String time, Integer vol){
        if(CommonUtil.isEmpty(list)){
            list.add(new CMinute(price, time,vol));
        }else{
            Integer last = list.size();
            CMinute lastItem = list.get(last-1);
            if(time.equals(lastItem.timeMinute)){
                lastItem.price = price;
                lastItem.vol += vol;
            }else if(time.compareTo(lastItem.timeMinute) > 0){
                list.add(new CMinute(price, time,vol));
            }
        }
    }

    //买卖点设置
    public void setOpInfo(String op, float opPrice, String time){
        if(!CommonUtil.isEmpty(timePrices)){
            for(CMinute item : timePrices){
                if(time.equals(item.timeMinute)){
                    item.opChar = op;
                    item.opPrice = opPrice;
                }
            }
        }
    }

    public String currentRate(){
        return CommonUtil.formatPercent(
                (current.getPrice()-lastClosePrice)/lastClosePrice);
    }

    public float getPriceDelta() {
        return priceDelta;
    }
}
