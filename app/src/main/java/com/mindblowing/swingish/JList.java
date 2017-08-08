package com.mindblowing.swingish;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

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


		listModel.list = this;
	}

	/**
	* for checkable selection to occur ou must call  this with
	 * JList.ListSelectionModel.SINGLE_SELECTION
	 */
	public void setSelectionMode(int selectionMode)
	{

		if(selectionMode == JList.ListSelectionModel.SINGLE_SELECTION)
			setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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

		public abstract List toList();


		JList list;
		public void fireContentsChanged(Object source, int start, int end)
		{
			//TODO: tell people about it? or just update renderererer
			if(list != null)
			{
				if(list.getAdapter() instanceof ArrayAdapter)
				{
					((ArrayAdapter)list.getAdapter()).clear();
					((ArrayAdapter)list.getAdapter()).addAll(this.toList());
					((ArrayAdapter)list.getAdapter()).notifyDataSetChanged();
					list.postInvalidate();
				}
			}
		}
	}
}
