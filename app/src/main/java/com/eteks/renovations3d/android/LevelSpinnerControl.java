package com.eteks.renovations3d.android;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.eteks.renovations3d.android.MultipleLevelsPlanPanel.LevelLabel;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.mindblowing.renovations3d.R;

import java.util.ArrayList;

/**
 * Created by phil on 1/1/2017.
 */
public class LevelSpinnerControl
{
	private Context context;
	private Spinner spinner;

	private ChangeListener changeListener = null; //note singleton
	private View.OnLongClickListener onLongClickListener = null; //note singleton
	private View.OnLongClickListener longClickPassThough;

	private int selectedLevel = 0;
	private ArrayList<String> levelNames = new ArrayList<String>();
	private ArrayList<LevelLabel> levelLabels = new ArrayList<LevelLabel>();

	public LevelSpinnerControl(final Context context)
	{
		this.context = context;
	}

	public void setSpinner(final Spinner spinner)
	{
		this.spinner = spinner;
		spinner.setAdapter(buildAdapter());
		spinner.setSelection(selectedLevel);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				selectedLevel = position;
				if (changeListener != null)
				{
					changeListener.stateChanged(new ChangeListener.ChangeEvent());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				System.err.println("LevelSpinnerControl has nothing selected, does this matter?");
			}
		});

		longClickPassThough = new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View view)
			{
				if (onLongClickListener != null)
				{
					onLongClickListener.onLongClick(view);
				}
				return false;
			}
		};
		spinner.setOnLongClickListener(longClickPassThough);
		spinner.setLongClickable(true);
	}


	private ArrayAdapter<String> buildAdapter()
	{
		return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, levelNames)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				Configuration configuration = context.getResources().getConfiguration();
				int screenWidthDp = configuration.screenWidthDp;

				boolean withText = screenWidthDp > 400;
				View view = getTextView(position, withText);
				view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);

				return view;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
				View view = getTextView(position, true);
				return view;
			}

			public View getTextView(int position, boolean withText)
			{
				TextView ret = new TextView(context);
				ret.setTextAppearance(context, android.R.style.TextAppearance_Large);
				String spanText = "L" + position + (withText ? "-" + levelNames.get(position) : "");
				ret.setText(spanText);
				//int drawRes = toolIcon[position];
				//SpannableStringBuilder builder = new SpannableStringBuilder(spanText);// it will replace "*" with icon
				//builder.setSpan(new ImageSpan(context, drawRes), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				//ret.setText(builder);

				return ret;
			}
		};
	}


	public LevelLabel getSelectedComponent()
	{
		if (selectedLevel < levelLabels.size())
			return levelLabels.get(selectedLevel);
		else
			return null;
	}

	public void addChangeListener(ChangeListener changeListener)
	{
		this.changeListener = changeListener;
	}

	public void addOnLongClickListener(View.OnLongClickListener onLongClickListener)
	{
		this.onLongClickListener = onLongClickListener;
	}

	public void setTitleAt(int index, String newValue)
	{
		levelNames.set(index, newValue);
	}

	public void removeChangeListener(ChangeListener changeListener)
	{
		this.changeListener = null;
	}

	public void removeOnLongClickListener(View.OnLongClickListener onLongClickListener)
	{
		this.onLongClickListener = null;
	}

	public void removeAll()
	{
		levelNames.clear();
		levelLabels.clear();
		if (spinner != null)
			spinner.setAdapter(buildAdapter());
	}

	public void remove(int index)
	{
		levelNames.remove(index);
		levelLabels.remove(index);
		if (spinner != null)
			spinner.setAdapter(buildAdapter());
	}

	public void insertTab(String title, Object icon, LevelLabel levelLabel, int index)
	{
		levelNames.add(index, title);
		levelLabels.add(index, levelLabel);

		selectedLevel = index;
		if (spinner != null)
		{
			spinner.setSelection(index);
			spinner.setAdapter(buildAdapter());
		}
	}

	public void addTab(String title, LevelLabel levelLabel)
	{
		insertTab(title, null, levelLabel, levelNames.size());
	}

	public void addTab(String title, Object icon, LevelLabel levelLabel)
	{
		insertTab(title, icon, levelLabel, levelNames.size());
	}

	public void setSelectedIndex(int i)
	{
		if (i < levelNames.size())
		{
			selectedLevel = i;
			if (spinner != null)
				spinner.setSelection(i);
		}
		else
		{
			selectedLevel = i;
		}
	}
}
