package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.eteks.renovations3d.android.utils.ChangeListener;

/**
 * Created by phil on 2/1/2017.
 */

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

}
