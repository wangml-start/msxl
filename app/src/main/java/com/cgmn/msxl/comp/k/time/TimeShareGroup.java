package com.cgmn.msxl.comp.k.time;

import android.util.Log;
import com.cgmn.msxl.server_interface.TimeShare;
import com.cgmn.msxl.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeShareGroup {

    Float lastClosePrice;

    Float totAmt=0f;
    Float totVol=0f;

    MinuteCounter timer;
    Integer startNum = 93000;
    Integer endNum = 153000;

    TimeShare current, last;

    List<TimeShare> showList = new ArrayList<>();
    List<TimeShare> leftList = new ArrayList<>();

    List<CMinute> timePrices = new ArrayList<>();
    List<CMinute> solidPrices = new ArrayList<>();

    private Map<String, Integer> exist = new HashMap<>();


    public Boolean init(List<TimeShare> list){
        if(CommonUtil.isEmpty(list)){
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
            if(Integer.valueOf(timeStr) < startNum){
                if(item.getVolume() > 0){
                    showList.add(item);
                    current = item;
                }
            }else{
                leftList.addAll(list.subList(index-1, list.size()));
                break;
            }
        }
        updateCurrentNode("0925");

        if(current == null){
            Log.i("D","NODE: NULL");
        }
        return true;
    }



    //计算最大绝对值
    public float calExtremeNum(){
        Float delta = Math.abs(current.getPrice() - lastClosePrice);
        for(TimeShare item : showList){
            String timeStr = item.getTimeStr();
            if(CommonUtil.isEmpty(timeStr)){
                continue;
            }
            if(Integer.valueOf(timeStr) < startNum){
                continue;
            }
            Float temp = Math.abs(item.getPrice() - lastClosePrice);
            if(temp > delta){
                delta = temp;
            }
        }
        return delta;
    }


    public void onNextStep(){
        last = current;
        timer.nextStep();
        String time = timer.getTimeStr();
        Integer index=0;
        for(TimeShare item : leftList){
            index++;
            String key = item.getTimeStr();
            if(exist.containsKey(key)){
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
        }
        if(leftList.size() > 0){
            List<TimeShare> temp = leftList.subList(index-1, leftList.size());
            leftList = temp;
        }

        updateCurrentNode(null);
    }

    public void updateCurrentNode(String time){
        if(last == null || !last.getTimeStr().equals(current.getTimeStr())){
            totAmt += current.getAmt();
            totVol += 100 * current.getVolume();
        }
        if(time == null){
            time = timer.getTimeMinute();
        }
        updateMinuteNode(timePrices, current.getPrice(), time);
        updateMinuteNode(solidPrices, totAmt/totVol, time);
    }

    public void updateMinuteNode(List<CMinute> list, float price, String time){
        if(CommonUtil.isEmpty(list)){
            list.add(new CMinute(price, time));
        }else{
            Integer last = list.size();
            CMinute lastItem = list.get(last-1);
            if(time.equals(lastItem.timeMinute)){
                lastItem.price = price;
            }else if(time.compareTo(lastItem.timeMinute) > 0){
                list.add(new CMinute(price, time));
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
}
