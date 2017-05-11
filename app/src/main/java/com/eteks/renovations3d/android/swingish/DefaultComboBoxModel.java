package com.eteks.renovations3d.android.swingish;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by phil on 1/27/2017.
 */

public class DefaultComboBoxModel
{
	public ArrayList objs;
	public DefaultComboBoxModel(Object[] objs)
	{
		this.objs = new ArrayList(Arrays.asList(objs));
	}

	public DefaultComboBoxModel(ArrayList objs)
	{
		this.objs = objs;
	}


	public void insertElementAt(Object o, int idx)
	{
		if(objs==null)
			objs = new ArrayList();

		objs.add(idx,o);
	}
}
