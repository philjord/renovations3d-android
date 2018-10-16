package com.mindblowing.swingish;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by phil on 1/27/2017.
 */

public class JComboBox extends android.support.v7.widget.AppCompatSpinner
{
	private DefaultComboBoxModel dcbm;

	public JComboBox(Context context)
	{
		super(context);
	}


	public JComboBox(Context context, DefaultComboBoxModel dcbm)
	{
		super(context);
		setModel(dcbm);
	}

	public JComboBox(Context context, Object[] objs)
	{
		this(context, new DefaultComboBoxModel(objs));
	}

	public JComboBox(Context context, ArrayList objs)
	{
		this(context, new DefaultComboBoxModel(objs));
	}

	public void setModel(DefaultComboBoxModel dcbm)
	{
		this.dcbm = dcbm;
		setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, dcbm.objs));
	}

	public void setSelectedItem(Object selection)
	{
		int selectionPos = 0;
		for (int i = 0; i < dcbm.objs.size(); i++)
		{
			Object o = dcbm.objs.get(i);
			if (o != null && o.equals(selection))
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