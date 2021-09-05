package com.eteks.renovations3d.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.eteks.renovations3d.android.SwingTools;
import com.mindblowing.swingish.JButton;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;

/**
 * Created by phil on 2/3/2017.
 */

public abstract class AndroidDialogView extends Dialog implements DialogView
{
	protected Activity activity;
	protected Button closeButton;

	protected ViewGroup inflatedView;


	public AndroidDialogView(UserPreferences preferences, Activity activity, int rootViewId)
	{
		this(preferences,  activity,  rootViewId, false);
	}

	public AndroidDialogView(UserPreferences preferences, Activity activity, int rootViewId, boolean removeTitle)
	{
		super(activity);
		// this must be called early apparently

		Configuration configuration = activity.getResources().getConfiguration();
		if( configuration.screenHeightDp < 600)
				this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.activity = activity;
		inflatedView = (ViewGroup)this.getLayoutInflater().inflate(rootViewId, null);
		this.setContentView(inflatedView);

		this.closeButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.swing.HomePane.class, "CLOSE.Name"));
		closeButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view)
			{
				AndroidDialogView.this.activity.invalidateOptionsMenu();
				dismiss();
			}
		});
	}


	protected void swapOut(View newView, int placeHolderId)
	{
		View placeHolder = inflatedView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		replaceView(placeHolder, newView);
	}

	protected void removeView(int placeHolderId)
	{
		removeViewFromParent(inflatedView.findViewById(placeHolderId));
	}

	public static ViewGroup getParent(View view) {
		return (ViewGroup)view.getParent();
	}

	public static void removeViewFromParent(View view) {
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

}
