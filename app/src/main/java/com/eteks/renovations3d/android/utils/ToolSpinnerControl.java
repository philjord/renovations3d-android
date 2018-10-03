package com.eteks.renovations3d.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.eteks.renovations3d.android.MultipleLevelsPlanPanel;

/**
 * Created by phil on 1/1/2017.
 */
public class ToolSpinnerControl
{
	private Context context;
	private Spinner spinner;

	private String[] toolNames = null;
	private int[] toolIcon = null;

	public ToolSpinnerControl(final Context context )
	{
		this.context = context;
	}

	public void setSpinner(final Spinner spinner,
												 String[] toolNames,
												 int[] toolIcon)
	{
		this.spinner = spinner;
		this.toolNames = toolNames;
		this.toolIcon = toolIcon;
		spinner.setAdapter(buildAdapter());
	}


	private ArrayAdapter<String> buildAdapter()
	{
		return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, toolNames)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				Configuration configuration = getContext().getResources().getConfiguration();
				int screenWidthDp = configuration.screenWidthDp;
				boolean toolsWide = screenWidthDp > MultipleLevelsPlanPanel.TOOLS_WIDE_MIN_DP;

				View view = getTextView(position, toolsWide);
				view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), 0);

				return view;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
				return getTextView(position, true);
			}

			public View getTextView(int position, boolean withText)
			{
				TextView ret = new TextView(getContext());
				ret.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
				String spanText = "* " + (withText ? toolNames[position] : "");
				int drawRes = toolIcon[position];
				SpannableStringBuilder builder = new SpannableStringBuilder(spanText);// it will replace "*" with icon
				builder.setSpan(new ImageSpan(getContext(), drawRes), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ret.setText(builder);

				return ret;
			}
		};
	}


}
