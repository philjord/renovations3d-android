package com.eteks.renovations3d.android.swingish;


import com.eteks.renovations3d.android.ChangeListener;

import java.util.ArrayList;

public class SpinnerNumberModel
{
	private float stepSize, value;
	private float minimum, maximum;

	public SpinnerNumberModel(float value, float minimum, float maximum, float stepSize)
	{

		this.value = value;
		this.minimum = minimum;
		this.maximum = maximum;
		this.stepSize = stepSize;
	}


	public void setMinimum(float minimum)
	{
		this.minimum = minimum;
		fireStateChanged();
	}


	public float getMinimum()
	{
		return minimum;
	}


	public void setMaximum(float maximum)
	{
		this.maximum = maximum;
		fireStateChanged();
	}


	public float getMaximum()
	{
		return maximum;
	}


	public void setStepSize(float stepSize)
	{
		this.stepSize = stepSize;
		fireStateChanged();
	}

	public float getStepSize()
	{
		return stepSize;
	}

	public float getValue()
	{
		return value;
	}


	public void setValue(float value)
	{
		this.value = value;
		fireStateChanged();
	}


	protected ArrayList<ChangeListener> listenerList = new ArrayList<ChangeListener>();


	public void addChangeListener(ChangeListener l)
	{
		listenerList.add(l);
	}

	public void removeChangeListener(ChangeListener l)
	{
		listenerList.remove(l);
	}

	protected void fireStateChanged()
	{
		for (ChangeListener cl : listenerList)
		{
			cl.stateChanged(null);
		}
	}

}