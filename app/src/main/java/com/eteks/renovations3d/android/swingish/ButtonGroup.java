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

public class ButtonGroup implements CompoundButton.OnCheckedChangeListener, MenuItem.OnMenuItemClickListener
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

	private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
	private HashMap<MenuItem, Drawable> itemsSelectors = new HashMap<MenuItem, Drawable>();
	private MenuItem currentCheckedItem = null;
	public void add(MenuItem item)
	{
		items.add(item);
		//menu items that are part of a radio group MUST have a selector icon to start with
		itemsSelectors.put(item, item.getIcon());
		setIconFromSelector(item);
		// can't call this cos of the one listener bull
		//item.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		setIconFromSelector(item);
		// is it the guy we think is checked?
		if (item == currentCheckedItem)
		{
			// don't let it uncheck !
			if (!item.isChecked())
			{
				currentCheckedItem.setChecked(true);
				setIconFromSelector(currentCheckedItem);
			}
		}
		else if(item.isChecked())
		{
			// ignore it unless is an on!
			currentCheckedItem = item;
			for (MenuItem i : items)
			{
				if (i != currentCheckedItem)
				{
					i.setChecked(false);
					setIconFromSelector(i);
				}
			}
		}
		return false;
	}

	private void setIconFromSelector(MenuItem item)
	{
		StateListDrawable stateListDrawable = (StateListDrawable)itemsSelectors.get(item);
		int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
		stateListDrawable.setState(state);
		item.setIcon(stateListDrawable.getCurrent());
	}
}
