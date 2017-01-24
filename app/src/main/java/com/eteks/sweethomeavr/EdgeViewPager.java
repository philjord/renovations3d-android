package com.eteks.sweethomeavr;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by phil on 1/6/2017.
 */

public class EdgeViewPager extends ViewPager
{
	public float gripWidthMax = 200;
	public float gripWidthPercent = 0.1f;


	public EdgeViewPager(Context context)
	{
		super(context);
	}

	public EdgeViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

		if (action == MotionEvent.ACTION_DOWN)
		{
			float gripWidth = Math.min(gripWidthMax, this.getWidth() * gripWidthPercent);
			float x = ev.getX();
			//float y = ev.getY();
			//int pointerId = MotionEventCompat.getPointerId(ev, 0);
			boolean grip = x < gripWidth || (this.getWidth() - x) < gripWidth;
			if (!grip) return false;
		}

		return super.onInterceptTouchEvent(ev);
	}
}
