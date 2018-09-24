package com.eteks.renovations3d.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.eteks.renovations3d.android.MultipleLevelsPlanPanel;
import com.eteks.renovations3d.android.MultipleLevelsPlanPanel.LevelLabel;
import com.mindblowing.swingish.ChangeListener;

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
				Configuration configuration = getContext().getResources().getConfiguration();
				int screenWidthDp = configuration.screenWidthDp;

				boolean toolsWide = screenWidthDp > MultipleLevelsPlanPanel.TOOLS_WIDE_MIN_DP;
				boolean levelsWide = screenWidthDp > MultipleLevelsPlanPanel.LEVELS_WIDE_MIN_DP;

				TextView view = getTextView(position, levelsWide);
				view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);

				//https://stackoverflow.com/questions/11244918/action-bar-icon-size
				// state a 48dp per item (with a icon inside of 24)


				//https://developer.android.com/guide/practices/ui_guidelines/icon_design_action_bar
				//has a list at different *DPI
				//												ldpi (120 dpi)	mdpi (160 dpi)	hdpi (240 dpi)	xhdpi (320 dpi)
				//Action Bar Icon Size 	18 x 18 px 			24 x 24 px 				36 x 36 px 			48 x 48 px
				//density = getResources().getDisplayMetrics().density;

				// return 0.75 if it's LDPI
				// return 1.0 if it's MDPI
				// return 1.5 if it's HDPI
				// return 2.0 if it's XHDPI
				// return 3.0 if it's XXHDPI
				// return 4.0 if it's XXXHDPI

				// that supports the 48dp per item

				//tools wide = 550, 4*58=232 +200 for tools = 432 leave at least 118 for levels

				// size of more menu and it's margin then 3 buttons with 24 gaps then tools
				float othersSpace = (48+10) + (3 * (48+10)) + (toolsWide ? 200 : 60);

				// give it at least an spinner space, (max ensure no negatives)
				float dpAllowed = Math.max(screenWidthDp - othersSpace, 60);
				float density = getContext().getResources().getDisplayMetrics().density;
				float pixel = dpAllowed * density;
				view.setMaxWidth((int)pixel);

				//System.out.println("setMaxWidth toolsWide " + toolsWide + "=" + (toolsWide ? 200 : 60) );
				//System.out.println("setMaxWidth screenWidthDp " + screenWidthDp  );
				//System.out.println("setMaxWidth density " + density );
				//System.out.println("setMaxWidth othersSpace " + othersSpace  );
				//System.out.println("setMaxWidth dpAllowed " + dpAllowed + "=" + pixel );
				return view;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
				View view = getTextView(position, true);
				return view;
			}

			public TextView getTextView(int position, boolean withText)
			{
				TextView ret = new TextView(getContext());
				ret.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
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
