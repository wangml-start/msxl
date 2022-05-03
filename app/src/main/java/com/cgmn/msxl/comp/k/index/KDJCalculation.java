package com.cgmn.msxl.comp.k.index;

import com.cgmn.msxl.comp.k.KLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KDJCalculation {

    /**
     * kdj 9,3,3
     * N:=9; P1:=3; P2:=3;
     * RSV:=(CLOSE-L(LOW,N))/(H(HIGH,N)-L(LOW,N))*100;
     * K:SMA(RSV,P1,1);
     * D:SMA(K,P2,1);
     * J:3*K-2*D;
     * @param list 数据集合
     * @param n 指标周期 9
     * @param m 权重 1
     * @param P1 参数值为3
     * @param P2 参数值为3
     * @return
     */
    public static Map<String, List<Float>> KDJ(LinkedList<KLine> list, int n, int P1, int P2, int m) {
        Map<String, List<Float>> result = new HashMap<>();
        List<Float> kValue = new ArrayList();
        List<Float> dValue = new ArrayList();
        List<Float> jValue = new ArrayList();

        float rsv = 0;
        float lastK = 50;
        float lastD = 50;

        for (int i = 0; i < list.size(); i++) {
            float min = getPeriodLowest(list, i, n);
            float max = getPeriodHighest(list, i, n);
            float div = max-min;
            if(div == 0){
                kValue.add(0.0f);
                dValue.add(0.0f);
                jValue.add(0.0f);
            }else{
                rsv = ((list.get(i).close - min) / div) * 100;

                float k = countSMA(rsv, P1, m, lastK);
                float d = countSMA(k, P2, m, lastD);
                float j = 3 * k - 2 * d;
                lastK = k;
                lastD = d;
                kValue.add(k);
                dValue.add(d);
                jValue.add(j);
            }
        }
        result.put("k", kValue);
        result.put("d", dValue);
        result.put("j", jValue);
        return result;
    }

    /**
     * SMA(C,N,M) = (M*C+(N-M)*Y')/N
     * C=今天收盘价－昨天收盘价    N＝就是周期比如 6或者12或者24， M＝权重，一般取1
     *
     * @param c   今天收盘价－昨天收盘价
     * @param n   周期
     * @param m   1
     * @param sma 上一个周期的sma
     * @return
     */
    private static float countSMA(float c, float n, float m, float sma) {
        return (m * c + (n - m) * sma) / n;
    }

    /**
     * n周期内最低值集合
     * @param list
     * @param index list 下标
     * @param n
     * @return
     */
    private static float getPeriodLowest(LinkedList<KLine> list, int index, int n) {
        float minValue = Float.MAX_VALUE;
        if(index < n-1){
            for (int i = 0; i <= index; i++) {
                minValue = Math.min(minValue, list.get(i).low);
            }
        }else{
            for (int i = index-(n - 1); i <= index; i++) {
                minValue = Math.min(minValue, list.get(i).low);
            }
        }
        return minValue;
    }

    /**
     * n周期内最低值集合
     * @param list
     * @param index list 下标
     * @param n
     * @return
     */
    private static float getPeriodHighest(LinkedList<KLine> list, int index, int n) {
        float value = -Float.MAX_VALUE;
        if(index < n-1){
            for (int i = 0; i <= index; i++) {
                value = Math.max(value, list.get(i).high);
            }
        }else{
            for (int i = index-(n - 1); i <= index; i++) {
                value = Math.max(value, list.get(i).high);
            }
        }
        return value;
    }

}
