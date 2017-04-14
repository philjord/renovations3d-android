package com.eteks.renovations3d.android.swingish;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

	public JComboBox(Context context, String[] objs)
	{
		this(context, new DefaultComboBoxModel(objs));
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
			if (objs[i] != null && objs[i].equals(selection))
			{
				selectionPos = i;
				break;
			}
		}
		this.setSelection(selectionPos);
	}

	public void addItemListener( final ItemListener il)
	{
		this.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onNothingSelected(AdapterView<?> parent) {
				if(isEnabled())
					il.itemStateChanged(new ItemListener.ItemEvent());
			}
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
			{
				if(isEnabled())
					il.itemStateChanged(new ItemListener.ItemEvent());
			}
		});
	}

	public void setMaximumRowCount(int maximumRowCount)
	{
		// ignored this.maximumRowCount = maximumRowCount;
	}

}