package com.eteks.renovations3d.android;

import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Created by phil on 3/20/2017.
 */

public class NullableSpinnerNumberModel extends SpinnerNumberModel
{
	private boolean isNull = false;
	private boolean nullable = false;

	public NullableSpinnerNumberModel(float value, float minimum, float maximum, float stepSize)
	{
		super(value, minimum, maximum, stepSize);
	}

	@Override
	public Object getNextValue() {
		if (this.isNull) {
			return super.getValue();
		}
		Object nextValue = super.getNextValue();
		if (nextValue == null) {
			// Force to maximum value
			return getMaximum();
		} else {
			return nextValue;
		}
	}

	@Override
	public Object getPreviousValue() {
		if (this.isNull) {
			return super.getValue();
		}
		Object previousValue = super.getPreviousValue();
		if (previousValue == null) {
			// Force to minimum value
			return getMinimum();
		} else {
			return previousValue;
		}
	}

	@Override
	public Object getValue() {
		if (this.isNull) {
			return null;
		} else {
			return super.getValue();
		}
	}

	/**
	 * Sets model value. This method is overridden to store whether current value is <code>null</code>
	 * or not (super class <code>setValue</code> doesn't accept <code>null</code> value).
	 */
	@Override
	public void setValue(Object value) {
		if (value == null && isNullable()) {
			if (!this.isNull) {
				this.isNull = true;
				fireStateChanged();
			}
		} else {
			if (this.isNull
					&& value != null
					&& value.equals(super.getValue())) {
				// Fire a state change if the value set is the same one as the one stored by number model
				// and this model exposed a null value before
				this.isNull = false;
				fireStateChanged();
			} else {
				this.isNull = false;
				super.setValue(value);
			}
		}
	}

	@Override
	public Number getNumber() {
		return (Number)getValue();
	}

	/**
	 * Returns <code>true</code> if this spinner model is nullable.
	 */
	public boolean isNullable() {
		return this.nullable;
	}

	/**
	 * Sets whether this spinner model is nullable.
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
		if (!nullable && getValue() == null) {
			setValue(getMinimum());
		}
	}



	/**
	 * A nullable spinner number model that will reset to minimum when maximum is reached.
	 */
	public static class NullableSpinnerModuloNumberModel extends NullableSpinnerNumberModel
	{
		public NullableSpinnerModuloNumberModel(int value, int minimum, int maximum, int stepSize)
		{
			super(value, minimum, maximum, stepSize);
		}

		@Override
		public Object getNextValue() {
			if (getValue() == null
					|| getNumber().intValue() + getStepSize().intValue() < ((Number)getMaximum()).intValue()) {
				return ((Number)super.getNextValue()).intValue();
			} else {
				return getNumber().intValue() + getStepSize().intValue() - ((Number)getMaximum()).intValue() + ((Number)getMinimum()).intValue();
			}
		}

		@Override
		public Object getPreviousValue() {
			if (getValue() == null
					|| getNumber().intValue() - getStepSize().intValue() >= ((Number)getMinimum()).intValue()) {
				return ((Number)super.getPreviousValue()).intValue();
			} else {
				return getNumber().intValue() - getStepSize().intValue() - ((Number)getMinimum()).intValue() + ((Number)getMaximum()).intValue();
			}
		}
	}

	/**
	 * Nullable spinner model displaying length values matching preferences unit.
	 */
	public static class NullableSpinnerLengthModel extends NullableSpinnerNumberModel
	{
		private final UserPreferences preferences;

		/**
		 * Creates a model managing lengths between the given <code>minimum</code> and <code>maximum</code> values in centimeter.
		 */
		public NullableSpinnerLengthModel(UserPreferences preferences, float minimum, float maximum)
		{
			this(preferences, minimum, minimum, maximum);
		}

		/**
		 * Creates a model managing lengths between the given <code>minimum</code> and <code>maximum</code> values in centimeter.
		 */
		public NullableSpinnerLengthModel(UserPreferences preferences, float value, float minimum, float maximum)
		{
			super(value, minimum, maximum,
					preferences.getLengthUnit() == LengthUnit.INCH
							|| preferences.getLengthUnit() == LengthUnit.INCH_DECIMALS
							? LengthUnit.inchToCentimeter(0.125f) : 0.5f);
			this.preferences = preferences;
		}

		/**
		 * Returns the displayed value in centimeter.
		 */
		public Float getLength() {
			if (getValue() == null) {
				return null;
			} else {
				return Float.valueOf(((Number)getValue()).floatValue());
			}
		}

		/**
		 * Sets the length in centimeter displayed in this model.
		 */
		public void setLength(Float length) {
			setValue(length);
		}

		/**
		 * Sets the minimum length in centimeter displayed in this model.
		 */
		public void setMinimumLength(float minimum) {
			setMinimum(Float.valueOf(minimum));
		}

		/**
		 * Returns the length unit used by this model.
		 */
		public LengthUnit getLengthUnit() {
			return this.preferences.getLengthUnit();
		}
	}
}
