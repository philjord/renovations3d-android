package com.eteks.renovations3d.android;

/**
 * Created by phil on 3/20/2017.
 */

import com.mindblowing.swingish.SpinnerNumberModel;

/**
 * A spinner number model that will reset to minimum when maximum is reached.
 */
public class SpinnerModuloNumberModel extends SpinnerNumberModel
{
	public SpinnerModuloNumberModel(int value, int minimum, int maximum, int stepSize) {
		super(value, minimum, maximum, stepSize);
	}

	@Override
	public Object getNextValue() {
		if (getNumber().intValue() + getStepSize().intValue() < ((Number)getMaximum()).intValue()) {
			return ((Number)super.getNextValue()).intValue();
		} else {
			return getNumber().intValue() + getStepSize().intValue() - ((Number)getMaximum()).intValue() + ((Number)getMinimum()).intValue();
		}
	}

	@Override
	public Object getPreviousValue() {
		if (getNumber().intValue() - getStepSize().intValue() >= ((Number)getMinimum()).intValue()) {
			return ((Number)super.getPreviousValue()).intValue();
		} else {
			return getNumber().intValue() - getStepSize().intValue() - ((Number)getMinimum()).intValue() + ((Number)getMaximum()).intValue();
		}
	}
}
