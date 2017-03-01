package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.text.Spanned;
import android.widget.TextView;

/**
 * Created by phil on 2/1/2017.
 */

public class JLabel extends TextView
{
	public JLabel(Context context, String text)
	{
		super(context);
		setText(text);
		this.setTextAppearance(context, android.R.style.TextAppearance_Medium);
	}

	public JLabel(Context context, Spanned spanned)
	{
		super(context);
		setText(spanned);
		this.setTextAppearance(context, android.R.style.TextAppearance_Medium);
	}
}
