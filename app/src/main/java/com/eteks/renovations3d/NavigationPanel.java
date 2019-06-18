package com.eteks.renovations3d;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


import com.mindblowing.renovations3d.R;

public class NavigationPanel {
	private Context context;
	private PopupWindow popupWindow;
	private View popupView;
	private int mCurrentX = 20;
	private int mCurrentY = 75;
	private View parent;

	private Handler handler = new Handler();

	public NavigationPanel(Context context, View parent) {
		this.context = context;
		this.parent = parent;
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupView = layoutInflater.inflate(R.layout.navigationpanelpopup, null);

		// convert to pixel for the location
		mCurrentX = (int)(mCurrentX * context.getResources().getDisplayMetrics().density);
		mCurrentY = (int)(mCurrentY * context.getResources().getDisplayMetrics().density);

		popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

		/*View navPanelLeftButton = popupView.findViewById(R.id.navPanelLeftButton);
		navPanelLeftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (popupWindow != null & popupWindow.isShowing() && !((Activity) NavigationPanel.this.context).isFinishing() ) {
					popupWindow.dismiss();
				}
			}
		});*/
	}

	public void showTooltip()
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
				} else
				if (action == MotionEvent.ACTION_MOVE) {
					mCurrentX = (int) (event.getRawX() + mDx);
					mCurrentY = (int) (event.getRawY() + mDy);
					popupWindow.update(mCurrentX, mCurrentY, -1, -1);
				}
				return true;
			}
		});
	}

	public void hideTooltip() {
		if (popupWindow != null & popupWindow.isShowing() && !((Activity) NavigationPanel.this.context).isFinishing() )
			popupWindow.dismiss();
	}

	public View getLeftButton() {
		return popupView.findViewById(R.id.navPanelLeftButton);
	}

	public View getForwardButton() {
		return popupView.findViewById(R.id.navPanelForwardButton);
	}

	public View getRightButton() {
		return popupView.findViewById(R.id.navPanelRightButton);
	}

	public View getBackButton() {
		return popupView.findViewById(R.id.navPanelBackButton);
	}

	public View getUpButton() {
		return popupView.findViewById(R.id.navPanelUpButton);
	}

	public View getDownButton() {
		return popupView.findViewById(R.id.navPanelDownButton);
	}
}
