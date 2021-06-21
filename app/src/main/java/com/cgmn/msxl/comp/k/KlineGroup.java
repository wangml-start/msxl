package com.cgmn.msxl.comp.k;

import com.cgmn.msxl.comp.k.index.KDJCalculation;
import com.cgmn.msxl.comp.k.index.MACDCalculation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KlineGroup {
    private LinkedList<KLine> nodes = new LinkedList<>();

    public float mYMax = 0.0f;
    public float mYMin = 0.0f;
    public float mMaxYVolume = 0.0f;
    public float mYMaxMacd = 0.0f;
    public float mYMinMacd = 0.0f;

    public float mYMaxKdj = 0.0f;
    public float mYMinKdj = 0.0f;

    public LinkedList<Float> ks5 = new LinkedList<>();
    public LinkedList<Float> ks10 = new LinkedList<>();
    public LinkedList<Float> ks20 = new LinkedList<>();


    public LinkedList<KLine> getNodes() {
        return nodes;
    }

    public void addKline(KLine kline) {
        nodes.addLast(kline);
    }

    public void calcMinMax(int start, int end) {
        int lastIndex;
        if (end == 0 || end >= nodes.size()) {
            lastIndex = nodes.size() - 1;
        } else {
            lastIndex = end;
        }
        mYMin = Float.MAX_VALUE;
        mYMax = -Float.MAX_VALUE;
        mMaxYVolume = -Float.MAX_VALUE;
        mYMaxMacd = -Float.MAX_VALUE;
        mYMinMacd = Float.MAX_VALUE;

        mYMaxKdj = -Float.MAX_VALUE;
        mYMinKdj = Float.MAX_VALUE;

        for (int i = start; i <= lastIndex; i++) {
            KLine entry = nodes.get(i);
            if (entry.low < mYMin) {
                mYMin = entry.low;
            }
            if (entry.high > mYMax) {
                mYMax = entry.high;
            }
            if (entry.volume > mMaxYVolume) {
                mMaxYVolume = entry.volume;
            }
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

            mYMaxKdj = Math.max(mYMaxKdj, entry.k);
            mYMaxKdj = Math.max(mYMaxKdj, entry.d);
            mYMaxKdj = Math.max(mYMaxKdj, entry.j);

            mYMinKdj = Math.min(mYMinKdj, entry.k);
            mYMinKdj = Math.min(mYMinKdj, entry.d);
            mYMinKdj = Math.min(mYMinKdj, entry.j);
        }
    }

    public Float calcAverage(int days) {
        Float blanckVal = -1f;
        LinkedList<Float> temp = null;
        if (5 == days) {
            if (ks5.size() < 5) {
                return blanckVal;
            }
            temp = ks5;
        } else if (10 == days) {
            if (ks10.size() < 10) {
                return blanckVal;
            }
            temp = ks10;
        } else {
            if (ks20.size() < 20) {
                return blanckVal;
            }
            temp = ks20;
        }
        Float sum = 0f;
        for (Float num : temp) {
            sum += num;
        }

        return sum / days;
    }

    /**
     * 计算指标各项数据
     */
    public void calcAverageMACD() {
        Map<String, List<Float>> map = MACDCalculation.MACD(12, 26, 9, nodes);
        Map<String, List<Float>> kdjMap = KDJCalculation.KDJ(nodes,9,3,3,1);
        int length = nodes.size();
        for (int i = 0; i < length; i++) {
            KLine kline = nodes.get(i);
            ks5.addLast(kline.close);
            ks10.addLast(kline.close);
            ks20.addLast(kline.close);

            if (ks5.size() > 5) {
                ks5.removeFirst();
            }
            if (ks10.size() > 10) {
                ks10.removeFirst();
            }
            if (ks20.size() > 20) {
                ks20.removeFirst();
            }
            kline.avg5 = calcAverage(5);
            kline.avg10 = calcAverage(10);
            kline.avg20 = calcAverage(20);

//            int index = length - i - 1;
            kline.dif = map.get("dif").get(i);
            kline.dea = map.get("dea").get(i);
            kline.macd = map.get("macd").get(i);

            kline.k = kdjMap.get("k").get(i);
            kline.d = kdjMap.get("d").get(i);
            kline.j = kdjMap.get("j").get(i);
        }
    }

}
