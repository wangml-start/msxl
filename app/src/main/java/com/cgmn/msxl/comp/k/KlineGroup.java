package com.cgmn.msxl.comp.k;

import java.util.LinkedList;

public class KlineGroup {
  private LinkedList<KLine> nodes = new LinkedList<>();

  public float mYMax = 0.0f;
  public float mYMin = 0.0f;
  public float mMaxYVolume = 0.0f;

  public LinkedList<Float> ks5 = new LinkedList<>();
  public LinkedList<Float> ks10 = new LinkedList<>();
  public LinkedList<Float> ks20 = new LinkedList<>();


  public LinkedList<KLine> getNodes() {
    return nodes;
  }

  public void addKline(KLine kline) {
    nodes.addFirst(kline);
  }

  public void calcMinMax(int start, int end) {
    int lastIndex;
    if (end == 0 || end >= nodes.size()){
      lastIndex = nodes.size() - 1;
    }else{
      lastIndex = end;
    }
    mYMin = Float.MAX_VALUE;
    mYMax = -Float.MAX_VALUE;
    mMaxYVolume = -Float.MAX_VALUE;
    for (int i = start; i <= lastIndex; i++) {
      KLine entry = nodes.get(i);
      if (entry.low < mYMin) {
        mYMin = entry.low;
      }
      if (entry.high > mYMax){
        mYMax = entry.high;
      }
      if (entry.volume > mMaxYVolume){
        mMaxYVolume = entry.volume;
      }
    }
  }

  public Float calcAverage(int days){
    Float blanckVal = -1f;
    LinkedList<Float> temp = null;
    if(5 == days){
      if(ks5.size() < 5){
        return blanckVal;
      }
      temp = ks5;
    }else if(10 == days){
      if(ks10.size() < 10){
        return blanckVal;
      }
      temp = ks10;
    }else {
      if(ks20.size() < 20){
        return blanckVal;
      }
      temp = ks20;
    }
    Float sum = 0f;
    for(Float num : temp){
      sum += num;
    }

    return sum/days;
  }

  public void calcAverage(){
    for(int i = nodes.size()-1; i>=0; i--){
      KLine kline = nodes.get(i);
      ks5.addLast(kline.close);
      ks10.addLast(kline.close);
      ks20.addLast(kline.close);

      if(ks5.size() > 5){
        ks5.removeFirst();
      }
      if(ks10.size() > 10){
        ks10.removeFirst();
      }
      if(ks20.size() > 20){
        ks20.removeFirst();
      }
      kline.avg5 = calcAverage(5);
      kline.avg10 = calcAverage(10);
      kline.avg20 = calcAverage(20);
    }
  }
}
