package com.mindblowing.swingish;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by phil on 4/28/2017.
 */

public class JPanel extends LinearLayout
{
	protected Activity activity;
	protected ViewGroup inflatedView;

	public JPanel(Activity activity, int rootViewId)
	{
		super(activity);
		this.activity = activity;
		inflatedView = (ViewGroup)activity.getLayoutInflater().inflate(rootViewId, null);
		this.addView(inflatedView);
	}


	protected void swapOut(View newView, int placeHolderId)
	{
		View placeHolder = inflatedView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		replaceView(placeHolder, newView);
	}

	protected void removeView(int placeHolderId)
	{
		removeView(inflatedView.findViewById(placeHolderId));
	}

	public static ViewGroup getParent(View view) {
		return (ViewGroup)view.getParent();
	}

	public static void removeViewFromParent(View view)
	{
		ViewGroup parent = getParent(view);
		if(parent != null) {
			parent.removeView(view);
		}
	}

	public static void replaceView(View currentView, View newView) {
		ViewGroup parent = getParent(currentView);
		if(parent == null) {
			return;
		}
		final int index = parent.indexOfChild(currentView);
		removeViewFromParent(currentView);
		removeViewFromParent(newView);
		parent.addView(newView, index);
	}

	// useful to tell jpanel when teh parent dialog is dismissed
	public void dismissed()
	{
	}
}