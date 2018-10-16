package com.eteks.renovations3d.android.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by phil on 1/26/2017.
 */

public class MenuItemGroup implements  MenuItem.OnMenuItemClickListener
{
	private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
	private HashMap<MenuItem, Drawable> itemsSelectors = new HashMap<MenuItem, Drawable>();
	private MenuItem currentCheckedItem = null;
	public void add(MenuItem item)
	{
		items.add(item);
		//menu items that are part of a radio group MUST have a selector icon to start with
		itemsSelectors.put(item, item.getIcon());
		if(item.isChecked() )
		{
			// only the first is accepted
			if(currentCheckedItem == null)
				currentCheckedItem = item;
			else
				item.setChecked(false);
		}
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

	/**
	 * For menu options prepare to sort out icons etc
	 */
	public void refreshIcons()
	{
		for (MenuItem i : items)
		{
			i.setChecked(i == currentCheckedItem);
			setIconFromSelector(i);
		}
	}

	private void setIconFromSelector(MenuItem item)
	{
		StateListDrawable stateListDrawable = (StateListDrawable)itemsSelectors.get(item);
		int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
		stateListDrawable.setState(state);
		item.setIcon(stateListDrawable.getCurrent());
	}
}
