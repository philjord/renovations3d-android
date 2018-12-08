package com.mindblowing.swingish;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

/**
 * Created by phil on 2/1/2017.
 */

public class JRadioButton extends RadioButton
{
	private ChangeListener changeListener = null;
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
		if(changeListener != null) {
			changeListener.stateChanged(null);
		}
	}
	@Override
	public boolean isSelected() {
		// this overrides isSelectedd, hope I never need that
		 return isChecked();
	}
	public void addChangeListener(final ChangeListener changeListener)
	{
		this.changeListener = changeListener;
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				changeListener.stateChanged(null);
			}});
	}

	public void removeChangeListener(final ChangeListener changeListener)
	{
		this.changeListener = null;
		setOnClickListener(null);
	}
}
