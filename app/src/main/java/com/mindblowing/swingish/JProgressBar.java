package com.mindblowing.swingish;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.ProgressBar;

import com.mindblowing.renovations3d.R;

public class JProgressBar extends ProgressBar {
	private int min = 0;//min = 0  lower than APi 26 so dummied into this class for now
	private ChangeListener changeListener;

	public JProgressBar(Context context, int min, int max, int value) {
		super(new ContextThemeWrapper(context, R.style.Widget_AppCompat_ProgressBar_Horizontal), null, 0);
		setMin(min);
		setMax(max);
		setValue(value);
	}

	public JProgressBar(Context context, int min, int max) {
		this(context, min, max, (min + max) / 2);
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;

		if(min!=0)
			System.out.println("JProgressBar with non 0 min, needs serious looking at!");
	}

	// just to match the core version
	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return getMax();
	}

	public int getValue() {
		int value = getProgress();
		return value;
	}

	public void setValue(int value) {
		this.setIndeterminate(false);
		setProgress(value);
		if(changeListener != null) {
			changeListener.stateChanged(null);
		}
	}

	public synchronized void setIndeterminate(boolean indeterminate) {
		super.setIndeterminate(indeterminate);
	}

	public void addChangeListener(final ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	public void removeChangeListener(final ChangeListener changeListener) {
		this.changeListener = null;
	}

	public void setPaintTicks(boolean paintTicks) {
		//ignored for now
	}

	public void setMajorTickSpacing(int majorTickSpacing) {
		//ignored for now
	}

	public void setSnapToTicks(boolean b) {
		//PJ TODO: should I use a incrementProgressBy call?
	}
}
