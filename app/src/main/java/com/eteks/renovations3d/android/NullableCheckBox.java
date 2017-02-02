package com.eteks.renovations3d.android;

import android.content.Context;

import com.eteks.renovations3d.android.swingish.JCheckBox;

import static android.R.attr.value;

/**
 * Created by phil on 2/1/2017.
 */

public class NullableCheckBox extends JCheckBox
{
	private boolean nullable = false;
	public NullableCheckBox(Context context, String text)
	{
		super(context, text);
	}

	public NullableCheckBox(Context context, String text, boolean isChecked)
	{
		super(context, text, isChecked);
	}

	public void setNullable(boolean nullable)
	{
		this.nullable = nullable;
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public void setValue(Boolean value)
	{
		this.setChecked(value);
	}

	public Boolean getValue()
	{
		return new Boolean(this.isChecked());
	}


}
