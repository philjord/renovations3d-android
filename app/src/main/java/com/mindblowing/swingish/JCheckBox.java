package com.mindblowing.swingish;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by phil on 2/1/2017.
 */
@SuppressLint("AppCompatCustomView")
public class JCheckBox extends CheckBox
{
	public JCheckBox(Context context, String text)
	{
		super(context);
		setText(text);
	}

	public JCheckBox(Context context, String text, boolean isChecked)
	{
		super(context);
		setText(text);
		setChecked(isChecked);
	}

	public void setToolTipText(String toolTipText)
	{
		//ignored
	}
	@Override
	public void setSelected(boolean selected)
	{
		//This override whatever setSelected means in textview
		setChecked(selected);
	}
	@Override
	public boolean isSelected() {
		// this overrides isSelected, hope I never need that
		return isChecked();
	}

	public void addChangeListener(final ChangeListener changeListener)
	{
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				changeListener.stateChanged(null);
			}});
	}

	public void addActionListener(final ActionListener actionListener)
	{
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				actionListener.actionPerformed(null);
			}});
	}

	public void addItemListener( final ItemListener il)
	{
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				il.itemStateChanged(null);
			}
		});
	}

	public void setVisible(boolean visible)
	{
		if(!visible)
			setVisibility(android.view.View.GONE);
		else
			setVisibility(android.view.View.VISIBLE);
	}
}
