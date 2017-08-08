package com.eteks.renovations3d.android;

import android.content.Context;

import com.mindblowing.swingish.JTextField;

import java.util.List;

/**
 * Created by phil on 2/1/2017.
 */

public class AutoCompleteTextField extends JTextField
{
	public AutoCompleteTextField(Context context, String text, int i, List<String> autoTexts)
	{
		super(context, text);
	}
}
