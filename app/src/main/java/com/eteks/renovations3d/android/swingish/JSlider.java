package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.view.View;
import android.widget.SeekBar;

import com.eteks.renovations3d.android.ChangeListener;

/**
 * Created by phil on 2/1/2017.
 */

public class JSlider extends SeekBar
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
}
