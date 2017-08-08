package com.mindblowing.swingish;

import android.content.Context;


import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class JSpinnerDate extends JSpinner
{
	public JSpinnerDate(Context context, SpinnerDateModel model)
	{
		this(context, model, null);
	}

	public JSpinnerDate(Context context, final SpinnerDateModel model, Format format)
	{
		super(context, model, format);
		output.setTextAppearance(context, android.R.style.TextAppearance_Small);
		output.setMinEms(10);// dd/mm/yyyy

		this.setMinimumWidth(50);

		this.requestLayout();
	}

	public void setTimePattern(String timePattern)
	{
		setFormat(new SimpleDateFormat(timePattern, Locale.getDefault()));
	}

}
