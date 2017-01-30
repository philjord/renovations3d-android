package com.eteks.renovations3d.android.swingish;

import java.util.ArrayList;

/**
 * Created by phil on 1/27/2017.
 */

public class DefaultComboBoxModel
{
	public Object[] objs;
	public DefaultComboBoxModel(Object[] objs)
	{
		this.objs = objs;
	}

	public void insertElementAt(Object o, int idx)
	{
		ArrayList a = new ArrayList();
		if(objs!=null)
			for(Object ob : objs)
				a.add(ob);
		a.add(idx,o);
		objs = a.toArray();

	}
}
