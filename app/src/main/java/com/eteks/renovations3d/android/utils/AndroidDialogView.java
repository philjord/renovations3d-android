package com.eteks.renovations3d.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.eteks.renovations3d.android.SwingTools;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.VCView;

/**
 * Created by phil on 2/3/2017.
 */

public abstract class AndroidDialogView extends Dialog implements DialogView
{

	protected Activity activity;
	protected LinearLayout rootView;
	protected Button closeButton;

	protected LinearLayout.LayoutParams rowInsets;
	protected LinearLayout.LayoutParams labelInsets;
	protected LinearLayout.LayoutParams labelInsetsWithSpace;
	protected LinearLayout.LayoutParams rightComponentInsets;
	protected LinearLayout.LayoutParams rightComponentInsetsWithSpace;

	public AndroidDialogView(UserPreferences preferences, Activity activity)
	{
		super(activity);
		this.activity = activity;
		this.rootView = new LinearLayout(activity);
		rootView.setOrientation(LinearLayout.VERTICAL);
		ScrollView sv = new ScrollView(activity);
		sv.addView(rootView);
		this.setContentView(sv);

		this.closeButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.HomePane.class, "CLOSE.Name"));
		closeButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view)
			{
				AndroidDialogView.this.activity.invalidateOptionsMenu();
				dismiss();
			}
		});

		Resources r = activity.getResources();
		int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
		int px10dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
		int px15dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());
		int px20dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

		rootView.setPadding(px10dp,px10dp,px10dp,px10dp);

		rowInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		rowInsets.setMargins(px10dp,px10dp,px10dp,px10dp);
		labelInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		labelInsets.setMargins(px10dp,px10dp,px15dp,px15dp);
		labelInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		labelInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px15dp);
		rightComponentInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		rightComponentInsets.setMargins(px10dp,px10dp,px15dp,px10dp);
		rightComponentInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		rightComponentInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px10dp);
	}

}
