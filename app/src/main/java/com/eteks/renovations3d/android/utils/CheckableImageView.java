package com.eteks.renovations3d.android.utils;

import android.content.Context;
import android.widget.Checkable;

public class CheckableImageView extends android.support.v7.widget.AppCompatImageView implements Checkable
{
	private Boolean checked = false;

	public CheckableImageView(Context context)
	{
		super(context);
	}
	@Override
	public boolean isChecked()
	{
		return checked;
	}

	@Override
	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	@Override
	public void toggle()
	{
		checked = !checked;
	}
}
