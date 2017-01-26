package com.eteks.sweethomeavr.android.swingish;

import android.widget.CompoundButton;

import java.util.ArrayList;

import static android.R.attr.checked;


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
