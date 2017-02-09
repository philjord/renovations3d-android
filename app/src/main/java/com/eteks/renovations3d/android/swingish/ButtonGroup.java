package com.eteks.renovations3d.android.swingish;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by phil on 1/26/2017.
 */

public class ButtonGroup implements CompoundButton.OnCheckedChangeListener
{
	private ArrayList<CompoundButton> buttons = new ArrayList<CompoundButton>();
	private CompoundButton currentCheckedButton = null;

	public void add(CompoundButton but)
	{
		buttons.add(but);
		if(but.isChecked() )
		{
			// only the first is accepted
			if(currentCheckedButton == null)
				currentCheckedButton = but;
			else
				but.setChecked(false);
		}
		but.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// is it the guy we think is checked?
		if (buttonView == currentCheckedButton)
		{
			// don't let it uncheck !
			if (!isChecked)
				currentCheckedButton.setChecked(true);
		}
		else if(isChecked)
		{
			// ignore it unless is an on!
			currentCheckedButton = buttonView;
			for (CompoundButton b : buttons)
			{
				if (b != currentCheckedButton)
					b.setChecked(false);
			}
		}
	}
}
