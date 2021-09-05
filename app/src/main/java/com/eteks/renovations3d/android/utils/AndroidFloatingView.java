package com.eteks.renovations3d.android.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.eteks.renovations3d.android.SwingTools;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.JButton;

import java.util.HashMap;

/**
 * Created by phil on 2/3/2017.
 */

public abstract class AndroidFloatingView implements DialogView
{
	protected Button closeButton;

	protected Activity activity;
	protected PopupWindow popupWindow;
	protected android.view.View popupView;
	private int mCurrentX = 20;
	private int mCurrentY = 75;
	private android.view.View parent;

	private ActionMap actionMap;

	public AndroidFloatingView(UserPreferences preferences, Activity activity, View parent, int rootViewId) {
		this.activity = activity;
		this.parent = parent;
		LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupView = layoutInflater.inflate(rootViewId, null);

		// convert to pixel for the location
		mCurrentX = (int)(mCurrentX * activity.getResources().getDisplayMetrics().density);
		mCurrentY = (int)(mCurrentY * activity.getResources().getDisplayMetrics().density);

		popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


		this.closeButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.swing.HomePane.class, "CLOSE.Name"));
		closeButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view) {
				dismissView();
			}
		});
	}

	public void showView()
	{
		popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, mCurrentX, mCurrentY);

		// add a listener for drags that aren't caught by other buttons
		popupView.setOnTouchListener(new View.OnTouchListener() {
			private float mDx;
			private float mDy;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					mDx = mCurrentX - event.getRawX();
					mDy = mCurrentY - event.getRawY();
				} else if (action == MotionEvent.ACTION_MOVE) {
					mCurrentX = (int) (event.getRawX() + mDx);
					mCurrentY = (int) (event.getRawY() + mDy);
					popupWindow.update(mCurrentX, mCurrentY, -1, -1);
				}
				return true;
			}
		});
	}

	public void dismissView() {
		if (popupWindow != null & popupWindow.isShowing() && !activity.isFinishing() )
			popupWindow.dismiss();
	}

	protected void swapOut(View newView, int placeHolderId) {
		View placeHolder = popupView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		newView.setId(placeHolderId);
		replaceView(placeHolder, newView);
	}

	protected void removeView(int placeHolderId) {
		removeViewFromParent(popupView.findViewById(placeHolderId));
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

	protected View findViewById(int resId) {
		return popupView.findViewById(resId);
	}

	protected  void setTitle(String dialogTitle) {
		//TODO: possibly a good idea?
	}

	public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
		popupWindow.setOnDismissListener(onDismissListener);
	}

	public final ActionMap getActionMap() {
		if(actionMap == null)
			actionMap =  new ActionMap();
		return actionMap;
	}

	// just for naming similarity
	public static class ActionMap extends HashMap<Enum, View> {
	}
}
