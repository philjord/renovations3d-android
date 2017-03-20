package com.eteks.renovations3d.android.swingish;


import java.util.ArrayList;

public class SpinnerNumberModel
{
	protected ArrayList<ChangeListener> listenerList = new ArrayList<ChangeListener>();

	private double stepSize, value;
	private double minimum, maximum;

	public SpinnerNumberModel(double value, double minimum, double maximum, double stepSize)
	{

		if (value < minimum || value > maximum)
			throw new RuntimeException("Bad setting for spinner model v=" + this.value + " min=" +
					this.minimum + " max=" + this.maximum + " step=" + this.stepSize);

		// value has to be a multiple of the stepSize or we have terrible trouble
		this.value = stepSize * (Math.round(value / stepSize));
		this.minimum = stepSize * (Math.round(minimum / stepSize));
		this.maximum = stepSize * (Math.round(maximum / stepSize));
		this.stepSize = stepSize;
	}


	public void setMinimum(double minimum)
	{
		this.minimum = stepSize * (Math.round(minimum / stepSize));
		fireStateChanged();
	}


	public double getMinimum()
	{
		return minimum;
	}


	public void setMaximum(double maximum)
	{
		this.maximum = stepSize * (Math.round(maximum / stepSize));
		fireStateChanged();
	}


	public double getMaximum()
	{
		return maximum;
	}


	public void setStepSize(float stepSize)
	{
		// value has to be a multiple of the stepSize or we have terrible trouble
		this.value = stepSize * (Math.round(value / stepSize));
		fireStateChanged();
	}

	public Number getStepSize()
	{
		return stepSize;
	}

	public double getValue()
	{
		return value;
	}

	public Number getNumber()
	{
		return getValue();
	}

	public void setValue(double value)
	{
		// value has to be a multiple of the stepSize or we have terrible trouble
		this.value = stepSize * (Math.round(value / stepSize));
		fireStateChanged();
	}

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


	public Object getNextValue()
	{
		if ( value + stepSize <= maximum) {
			return value + stepSize;
		} else {
			return value;
		}
	}


	public Object getPreviousValue()
	{
		if ( value - stepSize >= minimum) {
			return value - stepSize;
		} else {
			return value;
		}
	}

}