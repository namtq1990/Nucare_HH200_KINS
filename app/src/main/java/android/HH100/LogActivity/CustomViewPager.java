package android.HH100.LogActivity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by inseon.ahn on 2018-07-02.
 */

public class CustomViewPager extends ViewPager {

    private boolean isPageScrollEnabled = true;

    private static final int OFF_SET = 10;


    private float preX;


    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPageScrollEnabled && super.onTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPageScrollEnabled && super.onInterceptTouchEvent(event);
    }





    public void setPageScrollEnabled(boolean isPageScrollEnabled) {
        this.isPageScrollEnabled = isPageScrollEnabled;
    }


}