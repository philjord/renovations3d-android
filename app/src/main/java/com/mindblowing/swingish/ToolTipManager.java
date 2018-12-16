package com.mindblowing.swingish;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.renovations3d.R;

public class ToolTipManager {
	private Context context;
	private PopupWindow popupWindow;
	private View popupView;
	private int mCurrentX = 20;
	private int mCurrentY = 75;
	private View parent;
	public ToolTipManager(Context context, View parent) {
		this.context = context;
		this.parent = parent;
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupView = layoutInflater.inflate(R.layout.tooltippopup, null);

		// convert to pixel for the location
		mCurrentX = (int)(mCurrentX * context.getResources().getDisplayMetrics().density);
		mCurrentY = (int)(mCurrentY * context.getResources().getDisplayMetrics().density);

		popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		View btnClose = popupView.findViewById(R.id.btnClose);

		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				popupWindow.dismiss();
			}
		});

	}

	public void showTooltip(View toolTipComponent)
	{

		View placeHolder = popupView.findViewById(R.id.toolTipTextView);
		toolTipComponent.setLayoutParams(placeHolder.getLayoutParams());
		toolTipComponent.setId(R.id.toolTipTextView);
		AndroidDialogView.replaceView(placeHolder, toolTipComponent);

		popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, mCurrentX, mCurrentY);

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
		// delay so the user can move it around if they wish
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				popupWindow.dismiss();
			}
		}, 4000);

	}
}
