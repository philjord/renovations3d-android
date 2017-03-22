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
		this.value = value;
		this.minimum = minimum;
		this.maximum = maximum;
		this.stepSize = stepSize;
	}


	public void setMinimum(double minimum)
	{
		this.minimum = minimum;
		fireStateChanged();
	}


	public double getMinimum()
	{
		return minimum;
	}


	public void setMaximum(double maximum)
	{
		this.maximum = maximum;
		fireStateChanged();
	}


	public double getMaximum()
	{
		return maximum;
	}


	public void setStepSize(float stepSize)
	{
		this.stepSize = stepSize;
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
		this.value = value;
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