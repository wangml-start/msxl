package com.cgmn.msxl.comp.k;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Interpolator;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.VelocityTrackerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ScrollerCompat;
import com.cgmn.msxl.R;

public class KlineChart extends View {

    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final int SCROLL_STATE_SETTLING = 2;

    private int mScrollState = SCROLL_STATE_IDLE;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mScrollPointerId = -1;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private ViewFlinger mViewFlinger = new ViewFlinger();

    private RectF contentRect;
    private float contentMinOffset;

    private KlinePaint klinePaint;
    private KlineGroup mData;

    public KlineChart(Context context) {
        this(context, null);
    }

    public KlineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KlineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final ViewConfiguration vc = ViewConfiguration.get(this.getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();

        contentRect = new RectF();
        contentMinOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());

        klinePaint = new KlinePaint();

        klinePaint.setColors(
                getResources().getColor(R.color.kline_up),
                getResources().getColor(R.color.kline_down),
                getResources().getColor(R.color.kline_ave_5),
                getResources().getColor(R.color.kline_ave_10),
                getResources().getColor(R.color.kline_ave_20)
        );

        detector.setIsLongpressEnabled(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(mData != null){
            contentRect.set(contentMinOffset * 2, contentMinOffset, w - contentMinOffset, h - contentMinOffset);
            notifyDataSetChanged(false);
        }
    }

    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     */
    public void setData(KlineGroup data) {
        mData = data;
    }

    public void notifyDataSetChanged(boolean invalidate) {
        mData.calcMinMax(0, mData.getNodes().size());
        klinePaint.setContentRect(contentRect);
        klinePaint.setData(mData);

        if (invalidate) {
            invalidate();
        }
    }

    boolean onLongPress = false;
    GestureDetector detector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            onLongPress = true;
            highlight(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            klinePaint.zoomOut(e.getX(),e.getY());
            return true;
        }
    });

    private void highlight(MotionEvent e) {
        klinePaint.enableHighlight(e);
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        detector.onTouchEvent(e);

        final int action = MotionEventCompat.getActionMasked(e);
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (onLongPress) {
                    highlight(e);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                onLongPress = false;
                klinePaint.disableHighlight();
                invalidate();
                break;
            }
        }
        return onLongPress || super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;

        final MotionEvent vtev = MotionEvent.obtain(e);
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = MotionEventCompat.getPointerId(e, 0);
                mLastTouchX = (int) (e.getX() + 0.5f);
                mLastTouchY = (int) (e.getY() + 0.5f);

                if (mScrollState == SCROLL_STATE_SETTLING) {
                    setScrollState(SCROLL_STATE_DRAGGING);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                mScrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
                mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int index = MotionEventCompat.findPointerIndex(e, mScrollPointerId);
                if (index < 0) {
                    return false;
                }

                final int x = (int) (MotionEventCompat.getX(e, index) + 0.5f);
                final int y = (int) (MotionEventCompat.getY(e, index) + 0.5f);

                float dx = mLastTouchX - x;
                float dy = mLastTouchY - y;

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (Math.abs(dx) > mTouchSlop) {
                        if (dx > 0) {
                            dx -= mTouchSlop;
                        } else {
                            dx += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x;
                    mLastTouchY = y;

                    scroll(dx, 0);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP: {
                final int lastActionIndex = MotionEventCompat.getActionIndex(e);
                if (MotionEventCompat.getPointerId(e, lastActionIndex) == mScrollPointerId) {
                    // Pick a new pointer to pick up the slack.
                    final int newIndex = lastActionIndex == 0 ? 1 : 0;
                    mScrollPointerId = MotionEventCompat.getPointerId(e, newIndex);
                    mLastTouchX = (int) (MotionEventCompat.getX(e, newIndex) + 0.5f);
                    mLastTouchY = (int) (MotionEventCompat.getY(e, newIndex) + 0.5f);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float xvel =
                        -VelocityTrackerCompat.getXVelocity(mVelocityTracker, mScrollPointerId);
                final float yvel =
                        -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId);
                if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                resetTouch();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                resetTouch();
                setScrollState(SCROLL_STATE_IDLE);
                break;
            }
        }

        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();

        return true;
    }

    private void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
            stopScrollersInternal();
        }
    }

    private void stopScrollersInternal() {
        mViewFlinger.stop();
    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
    }

    private boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < mMinFlingVelocity) {
            velocityX = 0;
        }
        if (Math.abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityX == 0 && velocityY == 0) {
            // If we don't have any velocity, return false
            return false;
        }

        velocityX = Math.max(-mMaxFlingVelocity, Math.min(velocityX, mMaxFlingVelocity));
        velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity));
        mViewFlinger.fling(velocityX, velocityY);
        return true;
    }

    private void scroll(float dx, float dy) {
        klinePaint.refreshTouchMatrix(dx, dy);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mData != null){
            klinePaint.render(canvas);
        }
    }

    private class ViewFlinger implements Runnable {
        private int mLastFlingX;
        private int mLastFlingY;
        private ScrollerCompat mScroller;
        private final Interpolator sQuinticInterpolator = new Interpolator() {
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };

        public ViewFlinger() {
            mScroller = ScrollerCompat.create(getContext(), sQuinticInterpolator);
        }

        @Override
        public void run() {
            final ScrollerCompat scroller = mScroller;
            if (scroller.computeScrollOffset()) {
                final int x = scroller.getCurrX();
                final int y = scroller.getCurrY();
                final int dx = x - mLastFlingX;
                final int dy = y - mLastFlingY;
                mLastFlingX = x;
                mLastFlingY = y;
                int overscrollX = 0, overscrollY = 0;

                scroll(dx, 0);
                if (klinePaint.canScroll() && !scroller.isFinished()) {
                    postOnAnimation();
                }
            }
        }

        void postOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(KlineChart.this, this);
        }

        public void fling(int velocityX, int velocityY) {
            setScrollState(SCROLL_STATE_SETTLING);
            mLastFlingX = mLastFlingY = 0;
            mScroller.fling(0, 0, velocityX, velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        public void stop() {
            removeCallbacks(this);
            mScroller.abortAnimation();
        }
    }
}
