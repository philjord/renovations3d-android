package com.mindblowing.swingish;

import android.content.Context;
import android.text.Spanned;
import android.view.View;

/**
 * Created by phil on 2/1/2017.
 */

public class JLabel extends android.support.v7.widget.AppCompatTextView
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

	public void setVisible(boolean visible)
	{
		super.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}
}
