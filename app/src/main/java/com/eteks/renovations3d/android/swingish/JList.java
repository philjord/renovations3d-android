package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eteks.renovations3d.android.TextureChoiceComponent;

/**
 * Created by phil on 2/10/2017.
 */

public class JList extends ListView
{
	private AbstractListModel listModel;
	public JList(AbstractListModel listModel, Context context)
	{
		super(context);
		this.listModel = listModel;

	}

	public void setSelectionMode(int selectionMode)
	{
		//this.selectionMode = selectionMode;
	}

	public void setCellRenderer(ListAdapter cellRenderer)
	{
		this.setAdapter(cellRenderer);
	}

	public static class ListSelectionModel
	{

		public static final int SINGLE_SELECTION = 0;
	}

	public static abstract class AbstractListModel
	{
		public abstract Object getElementAt(int index);

		public abstract int getSize();

		public void fireContentsChanged(Object source, int start, int end)
		{
			//TODO: tell people about it? or just update renderererer
		}
	}
}
