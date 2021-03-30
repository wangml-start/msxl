package com.cgmn.msxl.comp.k;

public class KLine {
  public final float high;
  public final float low;
  public final float open;
  public final float close;
  public final float lastClose;

  public final int volume;

  public float avg5;
  public float avg10;
  public float avg20;

  public float dif;
  public float dea;
  public float macd;

  public String ch;

  public boolean isOpen=false;

  public KLine(float high, float low, float open, float close, float lsclose, int volume) {
    this.high = high;
    this.low = low;
    this.open = open;
    this.close = close;
    this.volume = volume;
    this.lastClose = lsclose;
  }

  public KLine(float open, float lastClose) {
    this.high = open;
    this.low = open;
    this.open = open;
    this.close = open;
    this.volume = 1;
    this.isOpen = true;
    this.lastClose = lastClose;
  }

  public String getCh() {
    return ch;
  }

  public void setCh(String ch) {
    this.ch = ch;
  }

  public Integer getState() {
    float delta = open - close;
    if (delta == 0) {
      return 0;
    } else if (delta > 0) {
      return -1;
    } else {
      return 1;
    }
  }
}
