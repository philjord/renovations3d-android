package com.mindblowing.utils;

import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * Created by phil on 8/27/2017.
 */


public class LongHoldHandler implements android.view.View.OnTouchListener
{
	private int JITTER_DP = 10;
	private int maxJitter = 10;
	private Handler handler = new Handler();
	public boolean pushingDown = false;
	private float xFirstMouseDown = -1;
	private float yFirstMouseDown = -1;
	private MotionEvent lastMotionEvent = null;
	private Callback callback;
	private long firstDelay;
	private long repeatDelay;

	/**
	 *
	 * @param mDisplayMetrics
	 * @param callback
	 * @param firstDelay
	 * @param repeatDelay use 0 for a single fire
	 */
	public LongHoldHandler(DisplayMetrics mDisplayMetrics, long firstDelay, long repeatDelay, Callback callback)
	{
		this.firstDelay = firstDelay;
		this.repeatDelay = repeatDelay;
		this.callback = callback;
		final float scale = mDisplayMetrics.density;
		maxJitter = (int) (JITTER_DP * scale + 0.5f);
	}
	private Runnable repeater = new Runnable() {
		@Override
		public void run() {
			// make sure the long hold is still running
			if (pushingDown) {
				callback.longHoldRepeat(lastMotionEvent);
				if(repeatDelay > 0 )
					handler.postDelayed(this, repeatDelay);
			}
		}
	};
	@Override
	public boolean onTouch(android.view.View v, MotionEvent ev)
	{
		final int action = MotionEventCompat.getActionMasked(ev);

		switch (action & MotionEvent.ACTION_MASK)
		{
			case MotionEvent.ACTION_DOWN:
			{
				if (ev.getPointerCount() == 1)
				{
					if(!pushingDown)
					{
						pushingDown = true;
						xFirstMouseDown = ev.getX();
						yFirstMouseDown = ev.getY();
						lastMotionEvent = MotionEvent.obtain(ev);
						handler.postDelayed(repeater, firstDelay);// real long to get out of single and double tap times

						// don't consume the event as taps etc need it as well.
						return false;
					}
				}
			}
			case MotionEvent.ACTION_MOVE:
			{
				// we want to consume jitters as we are running
				if (ev.getPointerCount() == 1)
				{
					if(pushingDown)
					{
						if( Math.abs(xFirstMouseDown - ev.getX()) < maxJitter
								&& Math.abs(yFirstMouseDown - ev.getY()) < maxJitter)
							return true;

						// other wise fall through to stop teh long hold repeats
					}
				}
			}
		}
		pushingDown = false;
		return false;
	}


	public interface Callback
	{
		void longHoldRepeat(MotionEvent lastMotionEvent);
	}
}
