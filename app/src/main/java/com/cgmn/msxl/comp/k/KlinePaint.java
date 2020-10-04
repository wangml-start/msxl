package com.cgmn.msxl.comp.k;

import android.graphics.*;
import android.view.MotionEvent;

import java.text.DecimalFormat;

public class KlinePaint {
  protected final Paint mDownPaint, mUpPaint;
  protected final Paint mGridPaint, mLabelPaint;
  protected final Paint mk5Paint, mk10Paint, mk20Paint;
  private Paint.FontMetrics fontMetrics = new Paint.FontMetrics();
  private float defaultTextSize;

  protected KlineGroup data;

  protected RectF candleRect = new RectF(), barRect = new RectF();
  private final float candleBarRatio = 0.8f;// candleRect.height / barRect.height = 4

  // contain 4 points to draw 2 lines.
  protected float[] shadowBuffer = new float[8];
  // contain 2 points to draw a rect.
  protected float[] bodyBuffer = new float[4];
  // contain 1 points to get y value.
  protected float[] barBuffer = new float[2];

  private float[] calcTemp = new float[]{0, 0};

  // contain 4 points to draw average lines.
  protected float[] averageBuffer = new float[4];

  /**
   * the space between the entries, default 0.1f (10%)
   */
  protected float mBarSpace = 0.1f;

  /**
   * the max visible entry count.
   */
  protected int visibleCount = 80;

  private boolean highlightEnable = false;
  private float[] highlightPoint = new float[2];

  /**
   * a y value formatter.
   */
  protected DecimalFormat decimalFormatter = new DecimalFormat("0.00");

  public void setColors(int klineUpcolor, int klineDowncolor,
                        int ave5, int ave10, int ave20){
    mUpPaint.setColor(klineUpcolor);
    mDownPaint.setColor(klineDowncolor);
    mk5Paint.setColor(ave5);
    mk10Paint.setColor(ave10);
    mk20Paint.setColor(ave20);
  }

  public KlinePaint() {
    mDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mDownPaint.setStyle(Paint.Style.FILL);
    mDownPaint.setStrokeWidth(1);

    mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mUpPaint.setStyle(Paint.Style.STROKE);
    mUpPaint.setStrokeWidth(1);

    mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mGridPaint.setStyle(Paint.Style.STROKE);
    mGridPaint.setStrokeWidth(0.8f);
    mGridPaint.setColor(Color.BLACK);

    mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mLabelPaint.setColor(Color.BLACK);

    mk5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mk10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mk20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mk5Paint.setStyle(Paint.Style.FILL);
    mk10Paint.setStyle(Paint.Style.FILL);
    mk20Paint.setStyle(Paint.Style.FILL);
    mk5Paint.setStrokeWidth(0.8f);
    mk10Paint.setStrokeWidth(0.8f);
    mk20Paint.setStrokeWidth(0.8f);
  }

  public void setContentRect(RectF contentRect) {
    float barTop = contentRect.bottom -
        (1 - candleBarRatio) * (contentRect.height() - contentRect.top);
    float candleBottom = barTop - contentRect.top;

    this.candleRect.set(contentRect.left, contentRect.top, contentRect.right, candleBottom);
    this.barRect.set(contentRect.left, barTop, contentRect.right, contentRect.bottom);

    defaultTextSize = contentRect.top * 3 / 4;
  }

  public void setData(KlineGroup data) {
    this.data = data;
    float count = visibleCount;
    if(count > data.getNodes().size()){
      count = data.getNodes().size();
    }
    prepareMatrixTouch(count);
    prepareMatrixValue(data.mYMax - data.mYMin, data.mYMin);
    prepareMatrixOffset(candleRect.left, candleRect.top);
  }

  public void enableHighlight(MotionEvent e) {
    highlightEnable = true;

    highlightPoint[0] = e.getX();
    highlightPoint[1] = e.getY();
  }

  public void disableHighlight() {
    highlightEnable = false;

    highlightPoint[0] = -1;
    highlightPoint[1] = -1;
  }

  /**
   * draw everything.
   */
  public void render(Canvas canvas) {
    // CALC
    calc();
    // DRAW LABELS
    renderLabels(canvas);

    // set the entry draw area.
    canvas.save();
    canvas.clipRect(candleRect.left, candleRect.top, candleRect.right, barRect.bottom);
    for (int i = visibleXMin; i < visibleXMax; i++) {
      KLine entry = data.getNodes().get(i);

      // draw step 0: set color
      Boolean isUp = entry.open < entry.close;

      // draw step 1: draw shadow
      shadowBuffer[0] = i + 0.5f;
      shadowBuffer[2] = i + 0.5f;
      shadowBuffer[4] = i + 0.5f;
      shadowBuffer[6] = i + 0.5f;
      if (entry.open > entry.close) {
        shadowBuffer[1] = entry.high;
        shadowBuffer[3] = entry.open;
        shadowBuffer[5] = entry.close;
        shadowBuffer[7] = entry.low;
      } else {
        shadowBuffer[1] = entry.high;
        shadowBuffer[3] = entry.close;
        shadowBuffer[5] = entry.open;
        shadowBuffer[7] = entry.low;
      }
      mapPoints(shadowBuffer);
      if(isUp){
        canvas.drawLines(shadowBuffer, mUpPaint);
      }else{
        canvas.drawLines(shadowBuffer, mDownPaint);
      }

      // draw step 2: draw body
      bodyBuffer[0] = i + 1 - mBarSpace;
      bodyBuffer[2] = i + mBarSpace;
      if (entry.open > entry.close) {
        bodyBuffer[1] = entry.open;
        bodyBuffer[3] = entry.close;
      } else {
        bodyBuffer[1] = entry.close;
        bodyBuffer[3] = entry.open;
      }
      mapPoints(bodyBuffer);
      if(isUp){
        canvas.drawRect(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], mUpPaint);
      }else{
        canvas.drawRect(bodyBuffer[0], bodyBuffer[1], bodyBuffer[2], bodyBuffer[3], mDownPaint);
      }


      // draw step 3: draw bar
      barBuffer[0] = 0;
      barBuffer[1] = entry.volume;
      mMatrixBar.mapPoints(barBuffer);
      if(isUp){
        canvas.drawRect(bodyBuffer[0], barRect.bottom - barBuffer[1], bodyBuffer[2], barRect.bottom - 1, mUpPaint);
      }else{
        canvas.drawRect(bodyBuffer[0], barRect.bottom - barBuffer[1], bodyBuffer[2], barRect.bottom - 1, mDownPaint);
      }

      //draw step 4: 均线
      if(i > 0){
        KLine preEntry = data.getNodes().get(i-1);
        if(preEntry.avg5 > 0){
          averageBuffer[0] = i - 1;
          averageBuffer[2] = i;
          averageBuffer[1] = preEntry.avg5;
          averageBuffer[3] = entry.avg5;
          mapPoints(averageBuffer);
          canvas.drawLine(averageBuffer[0],averageBuffer[1],averageBuffer[2],averageBuffer[3], mUpPaint);
        }
        if(preEntry.avg10 > 0){
          averageBuffer[0] = i - 1;
          averageBuffer[2] = i;
          averageBuffer[1] = preEntry.avg10;
          averageBuffer[3] = entry.avg10;
          mapPoints(averageBuffer);
          canvas.drawLine(averageBuffer[0],averageBuffer[1],averageBuffer[2],averageBuffer[3], mUpPaint);
        }
        if(preEntry.avg20 > 0){
          averageBuffer[1] = preEntry.avg20;
          averageBuffer[3] = entry.avg20;
          mapPoints(averageBuffer);
          canvas.drawLine(averageBuffer[0],averageBuffer[1],averageBuffer[2],averageBuffer[3], mUpPaint);
        }
      }

      // extra calc: set highlight position
      if (highlightPoint[0] <= bodyBuffer[2] && highlightPoint[0] >= bodyBuffer[0]) {
        highlightPoint[0] = shadowBuffer[0];
        highlightPoint[1] = (bodyBuffer[1] + bodyBuffer[3]) / 2;

        // DRAW HIGHLIGHT
        if (highlightEnable) {
          renderHighlight(canvas);
        }
      }
    }
    canvas.restore();
  }

  /**
   * Calculate the current range of x and y.
   */
  protected void calc() {
    // calc step 0: calc min&max x index
    float[] pixels = new float[]{
        candleRect.right, 0
    };
    revertMapPoints(pixels);
    visibleXMin = (pixels[0] <= 0) ? 0 : (int) (pixels[0]);
    visibleXMax = visibleXMin + visibleCount + 1;// plus visibleCount+1 for smooth disappear both side.
    if (visibleXMax > data.getNodes().size()) {
      visibleXMax = data.getNodes().size();
    }

    // calc step 1: calc min&max y value
    data.calcMinMax(visibleXMin, visibleXMax);
    prepareMatrixValue(data.mYMax - data.mYMin, data.mYMin);
    prepareMatrixBar(data.mMaxYVolume);
  }

  /**
   * Draw x and y labels.
   */
  protected void renderLabels(Canvas canvas) {
    // DRAW Y LABELS
    mLabelPaint.setTextAlign(Paint.Align.RIGHT);

    //draw max y value
    calcTemp[1] = candleRect.top;
    revertMapPoints(calcTemp);
    String value = decimalFormatter.format(calcTemp[1]);
    mLabelPaint.setTextSize(10);
    mLabelPaint.setTextSize(candleRect.left * 9 / mLabelPaint.measureText(value));
    mLabelPaint.getFontMetrics(fontMetrics);
    canvas.drawText(
        value,
        candleRect.left * 9 / 10,
        candleRect.top - fontMetrics.top - fontMetrics.bottom,
        mLabelPaint);
    canvas.drawLine(candleRect.left, candleRect.top - fontMetrics.top - fontMetrics.bottom, candleRect.right, candleRect.top - fontMetrics.top - fontMetrics.bottom, mGridPaint);

    // draw min y value
    calcTemp[1] = candleRect.bottom;
    revertMapPoints(calcTemp);
    value = decimalFormatter.format(calcTemp[1]);
    mLabelPaint.setTextSize(10);
    mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
    mLabelPaint.getFontMetrics(fontMetrics);
    canvas.drawText(
        value,
        candleRect.left * 9 / 10,
        candleRect.bottom - fontMetrics.bottom,
        mLabelPaint);
    canvas.drawLine(candleRect.left, candleRect.bottom - fontMetrics.bottom, candleRect.right, candleRect.bottom - fontMetrics.bottom, mGridPaint);

    calcTemp[1] = candleRect.height() / 3 + candleRect.top;
    revertMapPoints(calcTemp);
    value = decimalFormatter.format(calcTemp[1]);
    mLabelPaint.setTextSize(10);
    mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
    mLabelPaint.getFontMetrics(fontMetrics);
    canvas.drawText(
        value,
        candleRect.left * 9 / 10,
        candleRect.height() / 3 + candleRect.top + fontMetrics.bottom,
        mLabelPaint);
    canvas.drawLine(candleRect.left, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);

    calcTemp[1] = candleRect.height() * 2 / 3 + candleRect.top;
    revertMapPoints(calcTemp);
    value = decimalFormatter.format(calcTemp[1]);
    mLabelPaint.setTextSize(10);
    mLabelPaint.setTextSize(candleRect.left * 10 / mLabelPaint.measureText(value));
    mLabelPaint.getFontMetrics(fontMetrics);
    canvas.drawText(
        value,
        candleRect.left * 9 / 10,
        candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom,
        mLabelPaint);
    canvas.drawLine(candleRect.left, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, candleRect.right, candleRect.height() * 2 / 3 + candleRect.top + fontMetrics.bottom, mGridPaint);
    canvas.save();
    canvas.restore();
  }

  /**
   * Draw highlight.
   */
  protected void renderHighlight(Canvas canvas) {
    canvas.drawLine(candleRect.left, highlightPoint[1], candleRect.right, highlightPoint[1], mGridPaint);
    canvas.drawLine(highlightPoint[0], candleRect.top, highlightPoint[0], barRect.bottom, mGridPaint);
  }

  /**
   * Transform an array of points with all matrices.
   * VERY IMPORTANT: Keep matrix order "value-touch-offset" when transforming.
   */
  protected void mapPoints(float[] pts) {
    mMatrixValue.mapPoints(pts);
    mMatrixTouch.mapPoints(pts);
    mMatrixOffset.mapPoints(pts);
  }

  protected void revertMapPoints(float[] pixels) {
    Matrix tmp = new Matrix();

    // invert all matrices to convert back to the original value
    mMatrixOffset.invert(tmp);
    tmp.mapPoints(pixels);

    mMatrixTouch.invert(tmp);
    tmp.mapPoints(pixels);

    mMatrixValue.invert(tmp);
    tmp.mapPoints(pixels);
  }

  public void prepareMatrixValue(float deltaY, float yMin) {
    deltaY += yMin * 0.07;
    float scaleX = candleRect.width() / data.getNodes().size();
    float scaleY = candleRect.height() / deltaY;

    mMatrixValue.reset();
    mMatrixValue.postTranslate(0, -yMin);
    // the negative scale factor is used to draw x axis from right to left,y from down to up
    mMatrixValue.postScale(-scaleX, -scaleY);
    mMatrixValue.postTranslate(candleRect.width(), candleRect.height());
  }

  public void prepareMatrixTouch(float visibleCount) {
    float scaleX = data.getNodes().size() / visibleCount;
    float scaleY = 1;

    mMatrixTouch.reset();
    mMatrixTouch.postScale(scaleX, scaleY);

    resetScrollRange(scaleX);

//    mMatrixTouch.postTranslate(-maxTouchOffset, 0);
  }

  public void prepareMatrixOffset(float offsetX, float offsetY) {
    mMatrixOffset.reset();
    mMatrixOffset.postTranslate(offsetX, offsetY);
  }

  public void prepareMatrixBar(float maxY) {
    // increase the y range for good looking.
    maxY = maxY * 11 / 10;

    mMatrixBar.reset();
    mMatrixBar.postScale(1, barRect.height() / maxY);
  }

  private void resetScrollRange(float scaleX) {
    minTouchOffset = 0;
    maxTouchOffset = candleRect.width() * (scaleX - 1f);
  }

  /**
   * matrix to map the values to the screen pixels
   */
  protected Matrix mMatrixValue = new Matrix();

  /**
   * matrix to map chart scaled pixels
   */
  protected Matrix mMatrixTouch = new Matrix();

  /**
   * matrix to map the chart offset
   */
  protected Matrix mMatrixOffset = new Matrix();

  /**
   * matrix to map the volume value
   */
  protected Matrix mMatrixBar = new Matrix();

  protected int visibleXMin, visibleXMax;
  protected float maxTouchOffset, minTouchOffset;

  protected float[] matrixValues = new float[9];
  private boolean isOnBorder = true;

  public void refreshTouchMatrix(float dx, float dy) {
    isOnBorder = true;

    mMatrixTouch.getValues(matrixValues);

    matrixValues[Matrix.MTRANS_X] += -dx;
    matrixValues[Matrix.MTRANS_Y] += dy;

    if (matrixValues[Matrix.MTRANS_X] < -maxTouchOffset) {
      matrixValues[Matrix.MTRANS_X] = -maxTouchOffset;
      isOnBorder = false;
    }
    if (matrixValues[Matrix.MTRANS_X] > 0) {
      matrixValues[Matrix.MTRANS_X] = 0;
      isOnBorder = false;
    }

    mMatrixTouch.setValues(matrixValues);
  }

  public boolean canScroll() {
    return isOnBorder;
  }

  /**
   * TODO Zoom in.
   *
   * @param x pivot x
   * @param y pivot y
   */
  public void zoomIn(float x, float y) {
    mMatrixTouch.postScale(1.4f, 1.0f, x, y);
    mMatrixTouch.getValues(matrixValues);
    if (matrixValues[Matrix.MSCALE_X] < 1) {
      matrixValues[Matrix.MSCALE_X] = 1;
    }
    mMatrixTouch.setValues(matrixValues);
    resetScrollRange(matrixValues[Matrix.MSCALE_X]);
  }

  /**
   * TODO Zoom out.
   *
   * @param x pivot x
   * @param y pivot y
   */
  public void zoomOut(float x, float y) {
    mMatrixTouch.postScale(0.7f, 1.0f, x, y);
    mMatrixTouch.getValues(matrixValues);
    if (matrixValues[Matrix.MSCALE_X] < 1) {
      matrixValues[Matrix.MSCALE_X] = 1;
    }
    mMatrixTouch.setValues(matrixValues);
    resetScrollRange(matrixValues[Matrix.MSCALE_X]);

    prepareMatrixValue(data.mYMax - data.mYMin, data.mYMin);
    prepareMatrixOffset(candleRect.left, candleRect.top);
  }
}
