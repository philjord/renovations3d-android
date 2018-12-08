package com.mindblowing.swingish;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 2/10/2017.
 */

public class JList extends ListView {
	private AbstractListModel listModel;
	public JList(Context context, AbstractListModel listModel) {
		super(context);
		this.listModel = listModel;
		listModel.setList(this);
	}

	public AbstractListModel getModel() {
		return listModel;
	}

	/**
	* for checkable selection to occur you must call this with
	 * JList.ListSelectionModel.SINGLE_SELECTION
	 */
	public void setSelectionMode(int selectionMode) {
		if(selectionMode == JList.ListSelectionModel.SINGLE_SELECTION) {
			setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		} else {
			setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}

	}

	public void setCellRenderer(ListAdapter cellRenderer) {
		this.setAdapter(cellRenderer);
		listModel.fireContentsChanged(this, 0, 0);
	}

	public int[] getSelectedIndices() {

		//TODO: how do we implement this?
		//TODO:! ok I've had a pretty poor quality of selection vs activated time to sort it out for JLIst
		//https://stackoverflow.com/questions/11504860/what-is-the-difference-between-the-states-selected-checked-and-activated-in-and
		List<Integer> list = new ArrayList<>();
		SparseBooleanArray sp = getCheckedItemPositions();
		for (int i = 0; i < sp.size(); i++) {
			if (sp.valueAt(i)) {
				list.add(sp.keyAt(i));
			}
		}
		int[] ids = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ids[i] = list.get(i);
		}
		return ids;
	}

	public static class ListSelectionModel {
		public static final int SINGLE_SELECTION = 0;
		public static final int MULTIPLE_INTERVAL_SELECTION = 1;
	}

	/**
	 * do NOT share models
	 */
	public static abstract class AbstractListModel {
		// for the JList itself to set a reference when using this model
		private JList list;

		ArrayList<ListDataListener> listDataListeners = new ArrayList<ListDataListener>();

		public abstract Object getElementAt(int index);

		public abstract int getSize();

		public abstract List toList();

		void setList(JList list) {
			this.list = list;
			fireContentsChanged(null, 0, 0);
		}

		public void fireContentsChanged(Object source, int start, int end) {
			if(list != null) {
				if(list.getAdapter() instanceof ArrayAdapter) {
					((ArrayAdapter)list.getAdapter()).clear();
					((ArrayAdapter)list.getAdapter()).addAll(this.toList());
					((ArrayAdapter)list.getAdapter()).notifyDataSetChanged();
					list.postInvalidate();
				}
			}

			for(ListDataListener  ldl : listDataListeners) {
				ldl.contentsChanged(null);
			}
		}

		public void addListDataListener(ListDataListener listener) {
			listDataListeners.add(listener);
		}
	}


	public static class DefaultListModel extends AbstractListModel
	{
		private List<?>   data;

		public DefaultListModel(List data) {
			this.data = data;
		}

		public Object getElementAt(int index) {
			return this.data.get(index);
		}

		public int getSize() {
			return this.data.size();
		}

		public List<?> toList()
		{
			return data;
		}
	}


	public interface ListDataListener {
		public void contentsChanged(ListDataEvent ev) ;

		public void intervalRemoved(ListDataEvent ev);

		public void intervalAdded(ListDataEvent ev);
	}
	public interface ListDataEvent {
	}
}
