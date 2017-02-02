package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.widget.EditText;

/**
 * Created by phil on 2/1/2017.
 */

public class JTextField extends EditText
{

	public JTextField(Context context, String text)
	{
		super(context);
		setText(text);
	}
}
