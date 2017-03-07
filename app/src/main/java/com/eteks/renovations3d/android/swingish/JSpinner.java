package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.text.Format;


/**
 * Created by phil on 2/1/2017.
 */

public class JSpinner extends NumberPicker
{
	protected SpinnerNumberModel model;
	protected Format currentFormat;

	public JSpinner(Context context, SpinnerNumberModel model)
	{
		this(context, model, null);
	}

	public JSpinner(Context context, SpinnerNumberModel model, Format format)
	{
		super(context);
		this.model = model;

		setFormat(format);
		resetDisplayValues();

		//get rid of damn keyboard
		EditText numberPickerChild = (EditText) getChildAt(0);
		numberPickerChild.setFocusable(false);
		numberPickerChild.setInputType(InputType.TYPE_NULL);

		final ChangeListener changerListener = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent ev)
			{
				resetDisplayValues();
				setValue(valueToDisplayIndex());
			}
		};
		model.addChangeListener(changerListener );

		this.setOnValueChangedListener(new OnValueChangeListener()
		{
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal)
			{
				getModel().removeChangeListener(changerListener);
				getModel().setValue(displayIndexToValue(newVal));
				getModel().addChangeListener(changerListener);
			}
		});
	}

	public SpinnerNumberModel getModel()
	{
		return model;
	}


	private int valueToDisplayIndex()
	{
		return valueToDisplayIndex(getModel().getValue());
	}

	private int valueToDisplayIndex(double v)
	{
		return (int)(((v - currentDisplayMin) / currentDisplayStepSize));
	}

	private double displayIndexToValue(int idx)
	{
		return (((float)idx * currentDisplayStepSize) + currentDisplayMin);
	}

	private double currentDisplayMin = Float.MIN_VALUE;
	private double currentDisplayStepSize = Float.MIN_VALUE;
	private double currentDisplayMax = Float.MIN_VALUE;
	private Format displayFormat = null;
	protected void resetDisplayValues()
	{
		double displayMin = model.getMinimum();
		double displayMax = model.getMaximum();
		double value = model.getValue();
		double stepSize = model.getStepSize().floatValue();

		int maxSteps = 72; // 72 so the compass spinner 360/5 works

		// as we often get very large value system I will simply limit total step to 500 in each direction
		if( ((value - displayMin) / stepSize ) > maxSteps)
			displayMin = value  - (maxSteps * stepSize);
		if( ((displayMax - value) / stepSize ) > maxSteps)
			displayMax = value + (maxSteps * stepSize);

		// only update if something has changed
		if( displayMin != currentDisplayMin ||
				model.getStepSize().floatValue() != stepSize ||
				displayMax != currentDisplayMax ||
				currentFormat != displayFormat)
		{
			this.setMinValue(0);
			int countOfDisplayIndices = (int)((displayMax - displayMin) / stepSize + 1);
			if( countOfDisplayIndices > 1 )
			{
				this.setMaxValue(countOfDisplayIndices - 1); //-1 cos this is the max index not cout of indices

				String[] valueDisplays = new String[maxSteps*2+1];// we must always fill this up so we can alter the max value later without range check bugs
				int idx = 0;
				for (double i = displayMin; i <= displayMax; i += stepSize)
				{
					double val = i;
					String strVal;
					if (currentFormat != null)
						strVal = currentFormat.format(val);
					else
						strVal = "" + val;

					valueDisplays[idx] = strVal;
					idx++;
				}
				for (int i = idx; i < maxSteps*2+1; i ++)
				{
					valueDisplays[i] = "";
				}

				this.setDisplayedValues(valueDisplays);
			}
			currentDisplayMin = displayMin;
			currentDisplayStepSize = stepSize;
			currentDisplayMax = displayMax;
			displayFormat = currentFormat;
		}

		// notice number spinner is just 0-max display array and index
		this.setValue(valueToDisplayIndex());

	}

	/**
	 * Sets the format used to display the value of this spinner.
	 */
	public void setFormat(Format format)
	{
		this.currentFormat = format;
		resetDisplayValues();

   /* JComponent editor = getEditor();
	if (editor instanceof JSpinner.DefaultEditor) {
      JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();
      AbstractFormatter formatter = textField.getFormatter();
      if (formatter instanceof NumberFormatter) {
        ((NumberFormatter)formatter).setFormat(format);
        fireStateChanged();
      }
    }*/
	}
}
