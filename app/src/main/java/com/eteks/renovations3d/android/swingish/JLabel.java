package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.text.Spanned;
import android.widget.TextView;

import com.eteks.renovations3d.SweetHomeAVRActivity;

/**
 * Created by phil on 2/1/2017.
 */

public class JLabel extends TextView
{
	public JLabel(Context context, String text)
	{
		super(context);
		setText(text);
	}

	public JLabel(Context context, Spanned spanned)
	{
		super(context);
		setText(spanned);
	}
}
