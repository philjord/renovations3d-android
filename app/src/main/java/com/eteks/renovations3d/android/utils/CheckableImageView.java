package com.eteks.renovations3d.android.utils;

import android.content.Context;
import android.widget.Checkable;
import android.widget.ImageView;

public class CheckableImageView extends ImageView implements Checkable
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
