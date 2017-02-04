package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.eteks.renovations3d.android.utils.ChangeListener;
import com.eteks.renovations3d.android.MultipleLevelsPlanPanel.LevelLabel;

import java.util.ArrayList;

/**
 * Created by phil on 1/1/2017.
 */
public class JTabbedPane
{
	private Context context;
	private RadioGroup radioGrp;
	private ArrayList<RadioButton> buttons = new ArrayList<RadioButton>();
	private ArrayList<LevelLabel> levelLabels = new ArrayList<LevelLabel>();
	private ChangeListener changeListener = null; //note singleton
	private View.OnLongClickListener onLongClickListener = null; //note singleton

	public JTabbedPane(Context context2, RadioGroup radioGrp2)
	{
		this.context = context2;
		this.radioGrp = radioGrp2;
		radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				if(changeListener!=null)
				{
					changeListener.stateChanged(new ChangeListener.ChangeEvent());
				}
			}
		});
	}


	public LevelLabel getSelectedComponent()
	{
		int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
		return levelLabels.get(checkedRadioButtonId);
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
		RadioButton radioButton = buttons.get(index);
		radioButton.setText(newValue);
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
		radioGrp.removeAllViews();
		buttons.clear();
		levelLabels.clear();
	}

	public void remove(int index)
	{
		radioGrp.removeViewAt(index);
		buttons.remove(index);
		levelLabels.remove(index);
	}

	public RadioButton insertTab(String title, Object icon, LevelLabel levelLabel, Object tooltip, int index)
	{
		RadioGroup.LayoutParams lParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);

		lParams.gravity = Gravity.LEFT;

		RadioButton radioButton = new RadioButton(context);
		radioButton.setText(title);
		radioButton.setTextColor(Color.BLACK);// cos my sexy white background
		radioButton.setId(index);
		radioButton.setOnLongClickListener(onLongClickListener);
		radioGrp.addView(radioButton, index, lParams);

		buttons.add(index, radioButton);
		levelLabels.add(index, levelLabel);
		return radioButton;
	}

	public RadioButton addTab(String title, LevelLabel levelLabel)
	{
		return addTab(title, null, levelLabel, null);
	}

	public RadioButton addTab(String title, Object icon, LevelLabel levelLabel, Object tooltip)
	{
		return insertTab(title, icon, levelLabel, tooltip, buttons.size());
	}

	public void repaint()
	{
		radioGrp.postInvalidate();
	}

	public void setSelectedIndex(int i)
	{
		radioGrp.check(i);
	}
}
