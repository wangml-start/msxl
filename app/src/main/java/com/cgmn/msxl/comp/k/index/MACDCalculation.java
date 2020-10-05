package com.cgmn.msxl.comp.k.index;

import com.cgmn.msxl.comp.k.KLine;

import java.util.*;

public class MACDCalculation {

    /**
     * @param n    周期 12 26 9
     * @param list
     * @return
     */
    public static List<Float> EMA(int n, LinkedList<KLine> list) {
        List<Float> ema = new ArrayList<>();
        int length = list.size();
        float factor = 2.0f / (n + 1);
        KLine kk = list.getFirst();
        ema.add(kk.close);
        for (int i = 1; i < length; i++) {
            KLine entity = list.get(i);
            ema.add(factor * entity.close + (1 - factor) * ema.get(i - 1));
        }

        return ema;
    }

    /**
     * 重载方法
     *
     * @param n
     * @param list
     * @return
     */
    public static List<Float> EMA(int n, List<Float> list) {
        List<Float> ema = new ArrayList<>();
        int length = list.size();
        float factor = 2 / (n + 1);
        ema.add(list.get(0));
        for (int i = 1; i < length; i++) {
            ema.add(factor * list.get(i) + (1 - factor) * list.get(i - 1));
        }
        //普通一维数组

        return ema;
    }


    /**
     * @param mid
     * @param dif
     * @return
     */
    public static List<Float> DEA(int mid, List<Float> dif) {
        return EMA(mid, dif);
    }

    /**
     * @param s    12
     * @param l    26
     * @param list
     * @return
     */
    public static List<Float> DIF(int s, int l, LinkedList<KLine> list) {
        List<Float> dif = new ArrayList<>();
        List<Float> emaShort = EMA(s, list);
        List<Float> emaLong = EMA(l, list);
        int length = list.size();
        for (int i = 0; i < length; i++) {
            dif.add(emaShort.get(i) - emaLong.get(i));
        }
        return dif;
    }

    public static Map<String, List<Float>> MACD(int s, int l, int mid, LinkedList<KLine> list) {
        Map<String, List<Float>> result = new HashMap<>();
        List<Float> macd = new ArrayList<>();
        List<Float> dif = DIF(s, l, list);
        List<Float> dea = DEA(mid, dif);
        for (int i = 0; i < list.size(); i++) {
            macd.add((dif.get(i) - dea.get(i)) * 2);
        }
        result.put("dif", dif);
        result.put("dea", dea);
        result.put("macd", macd);
        return result;
    }
}
