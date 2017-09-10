package com.testapp.android.utils;

/**
 * Created by Administrator on 24/07/2017.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * A simple view that forces a square width and height.
 */
class SquareView extends View {

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}