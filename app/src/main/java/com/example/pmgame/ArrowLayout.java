package com.example.pmgame;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class ArrowLayout extends View {

    public static final String PROPERTY_X = "PROPERTY_X";
    public static final String PROPERTY_Y = "PROPERTY_Y";

    private Paint mPaint;

    private boolean mDrawArrow = false;
    public Point mPointFrom = new Point();   // current (during animation) arrow start point
    private Point mPointTo = new Point();     // current (during animation)  arrow end point
    private MainActivity mainActivity;

    public ArrowLayout(Context context) {
        super(context);
        init();
    }

    public ArrowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(5);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawArrow) {
            drawLine(mPointFrom, mPointTo, canvas);
        }
    }

    private void drawLine(Point pointFrom, Point pointTo, Canvas canvas) {
        canvas.drawLine(pointFrom.x, pointFrom.y, pointTo.x, pointTo.y, mPaint);
    }

    public ValueAnimator createArrowAnimator(Point pointFrom, Point pointTo, int duration) {

        mPointFrom.y = pointFrom.y;

        PropertyValuesHolder propertyX = PropertyValuesHolder.ofInt(PROPERTY_X, pointFrom.x, pointTo.x);
        PropertyValuesHolder propertyY = PropertyValuesHolder.ofInt(PROPERTY_Y, pointFrom.y, pointTo.y);

        ValueAnimator animator = new ValueAnimator();
        animator.setValues(propertyX, propertyY);
        animator.setDuration(duration);
        // set other interpolator (if needed) here:
        animator.setInterpolator(null);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPointTo.x = (int) valueAnimator.getAnimatedValue(PROPERTY_X);
                mPointTo.y = (int) valueAnimator.getAnimatedValue(PROPERTY_Y);

                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                mainActivity.tasksFinished.getAndIncrement();

                mainActivity.calculateBudget(mPointFrom.x, mPointTo.x);

            }
        });
        mDrawArrow = true;
        return animator;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}