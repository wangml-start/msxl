package com.cgmn.msxl.comp.k;

public class KLine {
  public final float high;
  public final float low;
  public final float open;
  public final float close;

  public final int volume;

  public String xValue;

  public float avg5;
  public float avg10;
  public float avg20;

  public KLine(float high, float low, float open, float close, int volume, String xValue) {
    this.high = high;
    this.low = low;
    this.open = open;
    this.close = close;
    this.volume = volume;
    this.xValue = xValue;
  }
}
