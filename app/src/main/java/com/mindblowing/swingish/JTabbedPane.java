package com.mindblowing.swingish;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

/**
 * Created by phil on 1/1/2017.
 */
public class JTabbedPane
{
	private Context context;
	private RadioGroup radioGrp;
	private ArrayList<RadioButton> buttons = new ArrayList<RadioButton>();
	private ArrayList<Object> userObjects = new ArrayList<Object>();
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
				if (changeListener != null)
				{
					changeListener.stateChanged(new ChangeListener.ChangeEvent());
				}
			}
		});
	}


	public Object getSelectedComponent()
	{
		int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
		return userObjects.get(checkedRadioButtonId);
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
		userObjects.clear();
	}

	public void remove(int index)
	{
		radioGrp.removeViewAt(index);
		buttons.remove(index);
		userObjects.remove(index);
	}

	public RadioButton insertTab(String title, Object icon, Object userObject, Object tooltip, int index)
	{
		RadioGroup.LayoutParams lParams = new RadioGroup.LayoutParams(
				RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);

		lParams.gravity = Gravity.LEFT;

		RadioButton radioButton = new RadioButton(context);
		radioButton.setText(title);
		radioButton.setTextColor(Color.BLACK);// cos my sexy white background
		radioButton.setId(index);
		radioButton.setOnLongClickListener(onLongClickListener);
		radioButton.setOnTouchListener(new View.OnTouchListener()
		{
			private View lastTouchView = null;
			private long lastTouchTime = 0;

			@Override
			public boolean onTouch(View v, MotionEvent ev)
			{
				final int action = ev.getActionMasked();

				switch (action & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
					{
						RadioButton rb = (RadioButton) v;
						if (!rb.isChecked())
							rb.setChecked(true);
						break;
					}
					case MotionEvent.ACTION_UP:
					{
						if (onLongClickListener != null)
						{
							if (lastTouchTime == 0 || lastTouchView != v || System.currentTimeMillis() - lastTouchTime > 500)
							{
								lastTouchTime = System.currentTimeMillis();
								lastTouchView = v;
							}
							else
							{
								if (lastTouchView == v && System.currentTimeMillis() - lastTouchTime < 500)
									onLongClickListener.onLongClick(v);

								// either way on a second touch clear the record
								lastTouchTime = 0;
								lastTouchView = null;
							}
						}
						break;
					}
				}
				return true;
			}
		});
		radioGrp.addView(radioButton, index, lParams);

		buttons.add(index, radioButton);
		userObjects.add(index, userObject);

		// the setselected come through too late, so let's just auto selct anything that is added
		// I don;t know why the listeners in multilevel are set in proper order.
		radioButton.setChecked(true);

		return radioButton;
	}

	public RadioButton addTab(String title, Object userObject)
	{
		return addTab(title, null, userObject, null);
	}

	public RadioButton addTab(String title, Object icon, Object userObject, Object tooltip)
	{
		return insertTab(title, icon, userObject, tooltip, buttons.size());
	}

	public void repaint()
	{
		radioGrp.postInvalidate();
	}

	public void setSelectedIndex(int i)
	{
		if( i < buttons.size())
		{
			RadioButton rb = buttons.get(i);
			if (rb != null)
				rb.setChecked(true);
		}
		else
		{
			// I could record this desire and select it when the level is added after
		}
	}
}
