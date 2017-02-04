package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import com.eteks.renovations3d.android.utils.ChangeListener;

/**
 * Created by phil on 2/1/2017.
 */

public class JRadioButton extends RadioButton
{

	public JRadioButton(Context context, String text)
	{
		super(context);
		setText(text);
	}
	public JRadioButton(Context context, String text, boolean isChecked)
	{
		super(context);
		setText(text);
		setChecked(isChecked);
	}
	@Override
	public void setSelected(boolean selected)
	{
		//This override whatever setSelected means in textview
		setChecked(selected);
	}
	public void addChangeListener(final ChangeListener changeListener)
	{
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				changeListener.stateChanged(null);
			}});
	}
}
