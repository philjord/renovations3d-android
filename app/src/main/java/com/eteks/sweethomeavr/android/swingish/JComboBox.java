package com.eteks.sweethomeavr.android.swingish;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.eteks.sweethomeavr.android.UserPreferencesPanel;

/**
 * Created by phil on 1/27/2017.
 */

public class JComboBox extends Spinner
{
	private Object[] objs;

	public JComboBox(Context context)
	{
		super(context);
	}


	public JComboBox(Context context, DefaultComboBoxModel dcbm)
	{
		super(context);
		setModel(dcbm);
	}

	public void setModel(DefaultComboBoxModel dcbm)
	{
		this.objs = dcbm.objs;
		setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, dcbm.objs));
	}

	public void setSelectedItem(Object selection)
	{
		int selectionPos = 0;
		for (int i = 0; i < objs.length; i++)
		{
			if (objs[i].equals(selection))
			{
				selectionPos = i;
				break;
			}
		}
		this.setSelection(selectionPos);
	}

}