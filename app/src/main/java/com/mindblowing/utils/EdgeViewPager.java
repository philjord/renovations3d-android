package com.mindblowing.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by phil on 1/6/2017.
 */

public class EdgeViewPager extends ViewPager
{
	public static final float gripWidthMaxDp = 30;
	public float gripWidthMaxPx = 30;

	public EdgeViewPager(Context context)
	{
		super(context);
		final float scale = getResources().getDisplayMetrics().density;
		gripWidthMaxPx = (int) (gripWidthMaxDp * scale + 0.5f);
	}

	public EdgeViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		final float scale = getResources().getDisplayMetrics().density;
		gripWidthMaxPx = (int) (gripWidthMaxDp * scale + 0.5f);
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//see https://github.com/chrisbanes/PhotoView/issues/31

		if (getCurrentItem() == 0 && getChildCount() == 0) {
			return false;
		}

		try {
			return super.onTouchEvent(ev);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if (getCurrentItem() == 0 && getChildCount() == 0) {
			return false;
		}

		final int action = ev.getActionMasked();

		if (action == MotionEvent.ACTION_DOWN)
		{
			float x = ev.getX();
			//float y = ev.getY();
			//int pointerId = MotionEventCompat.getPointerId(ev, 0);
			boolean grip = x < gripWidthMaxPx || (this.getWidth() - x) < gripWidthMaxPx;
			if (!grip || ev.getPointerCount() != 1)
				return false;
		}
		else if(action == MotionEvent.ACTION_MOVE)
		{
			if (ev.getPointerCount() != 1)
				return false;
		}

		//see https://github.com/chrisbanes/PhotoView/issues/31
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
