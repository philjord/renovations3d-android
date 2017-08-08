package com.eteks.renovations3d.android;

import android.content.Context;

import com.mindblowing.swingish.JCheckBox;

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
		this.setChecked(value == null ? Boolean.FALSE : value);
	}

	public Boolean getValue()
	{
		//NOTE!!!! this class can ONLY return Boolean.TRUE Bollean.FALSE and null never new Boolean(b)
		// as all checks are b == Boolean.TRUE etc and the "new Boolean" version will not equal
		if (this.isChecked()) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}

	}


}
