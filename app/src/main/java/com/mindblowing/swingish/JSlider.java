package com.mindblowing.swingish;

import android.content.Context;
import android.widget.SeekBar;

/**
 * Created by phil on 2/1/2017.
 */

public class JSlider extends android.support.v7.widget.AppCompatSeekBar
{
	private int min = 0;

	public JSlider(Context context, int min, int max, int value)
	{
		super(context);
		setMin(min);
		setMax(max);
		setValue(value);
	}

	public JSlider(Context context, int min, int max)
	{
		this(context, min, max, (min + max) / 2);
	}

	public int getMin()
	{
		return min;
	}

	public void setMin(int min)
	{
		this.min = min;

		if(min!=0)
			System.out.println("JSlider with non 0 min, needs serious looking at!");
	}

	// just to match the core version
	public int getMinimum()
	{
		return min;
	}

	public int getMaximum()
	{
		return getMax();
	}

	public int getValue()
	{
		int value = getProgress();
		return value;
	}

	public void setValue(int value)
	{
		setProgress(value);
	}
	public void addChangeListener(final ChangeListener changeListener)
	{
		this.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				changeListener.stateChanged(null);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});
	}

	public void setPaintTicks(boolean paintTicks)
	{
		//ignored for now
	}

	public void setMajorTickSpacing(int majorTickSpacing)
	{
		//ignored for now
	}



	public void setSnapToTicks(boolean b)
	{
		//PJ TODO: should I use a incrementProgressBy call?
	}

}
