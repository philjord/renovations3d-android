package com.eteks.renovations3d;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * Created by phil on 1/6/2017.
 */

public class EdgeViewPager extends ViewPager
{
	public float gripWidthMaxInch = 0.5f;
	public float gripWidthMaxPix = 200;
	public float gripWidthPercent = 0.1f;


	public EdgeViewPager(Context context)
	{
		super(context);
		// Now determine a fat fingers size for the indicators
		DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
		gripWidthMaxPix = (int)(mDisplayMetrics.densityDpi * gripWidthMaxInch);
	}

	public EdgeViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// Now determine a fat fingers size for the indicators
		DisplayMetrics mDisplayMetrics = getResources().getDisplayMetrics();
		gripWidthMaxPix = (int)(mDisplayMetrics.densityDpi * gripWidthMaxInch);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

		if (action == MotionEvent.ACTION_DOWN)
		{
			float x = ev.getX();
			//float y = ev.getY();
			//int pointerId = MotionEventCompat.getPointerId(ev, 0);
			boolean grip = x < gripWidthMaxPix || (this.getWidth() - x) < gripWidthMaxPix;
			if (!grip) return false;
		}

		return super.onInterceptTouchEvent(ev);
	}
}
