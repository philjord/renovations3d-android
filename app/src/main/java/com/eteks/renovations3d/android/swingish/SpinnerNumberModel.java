package com.eteks.renovations3d.android.swingish;

public class SpinnerNumberModel extends AbstractSpinnerModel
{
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

	public Object getValue()
	{
		return value;
	}

	public Number getNumber()
	{
		return (Number)getValue();
	}

	public void setValue(Object value)
	{
		//this really really needs to only accept values in range
		double newValue = ((Number) value).doubleValue();
		this.value = newValue < minimum ? minimum : newValue > maximum ? maximum : newValue;
		fireStateChanged();
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