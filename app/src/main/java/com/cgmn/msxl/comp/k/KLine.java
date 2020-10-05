package com.cgmn.msxl.comp.k;

public class KLine {
  public final float high;
  public final float low;
  public final float open;
  public final float close;

  public final int volume;

  public float avg5;
  public float avg10;
  public float avg20;

  public float dif;
  public float dea;
  public float macd;

  public boolean isOpen=false;

  public KLine(float high, float low, float open, float close, int volume) {
    this.high = high;
    this.low = low;
    this.open = open;
    this.close = close;
    this.volume = volume;
  }

  public KLine(float open) {
    this.high = open;
    this.low = open;
    this.open = open;
    this.close = open;
    this.volume = 1;
    this.isOpen = true;
  }
}
