package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Format;


/**
 * Created by phil on 2/1/2017.
 * A JSpinner that works like teh java desktop version, with the min/max able to be any values.
 */

public class JSpinner2 extends LinearLayout
{
	protected SpinnerNumberModel model;
	protected Format currentFormat;

	private TextView output;
	private Button upButton;
	private Button downButton;

	public JSpinner2(Context context, SpinnerNumberModel model)
	{
		this(context, model, null);
	}

	public JSpinner2(Context context, final SpinnerNumberModel model, Format format)
	{
		super(context);
		this.model = model;

		setFormat(format);

		this.setOrientation(LinearLayout.VERTICAL);
		output = new TextView(context);


		output.setTextAppearance(context, android.R.style.TextAppearance_Large);
		output.setMinEms(6);
		output.setMaxLines(1);
		output.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		final ChangeListener changerListener = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent ev)
			{
				if(currentFormat != null)
					output.setText(currentFormat.format(model.getValue()));
				else
					output.setText(""+model.getValue());
			}
		};
		model.addChangeListener(changerListener);

		upButton = new Button(context);
		upButton.setText("  +  ");
		downButton = new Button(context);
		downButton.setText("  -  ");

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0, 1f);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		this.addView(upButton, lp);
		this.addView(output, lp2);
		this.addView(downButton, lp);

		upButton.setOnTouchListener(new RepeatListener(400, 75, new OnClickListener() {
			@Override
			public void onClick(View view) {
				setValue((double)model.getNextValue());
			}
		}));

		downButton.setOnTouchListener(new RepeatListener(400, 75, new OnClickListener() {
			@Override
			public void onClick(View view) {
				setValue((double)model.getPreviousValue());
			}
		}));
	}

	public SpinnerNumberModel getModel()
	{
		return model;
	}

	public void setValue(double value)
	{
		model.setValue(value);
	}

	public void setFormat(Format format)
	{
		this.currentFormat = format;
	}





	/**
	 * A class, that can be used as a TouchListener on any view (e.g. a Button).
	 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
	 * click is fired immediately, next one after the initialInterval, and subsequent
	 * ones after the normalInterval.
	 *
	 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
	 * If it runs slow, it does not generate skipped onClicks. Can be rewritten to
	 * achieve this.
	 */
	public class RepeatListener implements OnTouchListener {

		private Handler handler = new Handler();

		private int initialInterval;
		private final int normalInterval;
		private final OnClickListener clickListener;
		private int runCount = 0;

		private Runnable handlerRunnable = new Runnable() {
			@Override
			public void run() {

				int del = normalInterval;
				// double speed after a hold of 25
				runCount++;
				if(runCount>25)
					del = del/2;

				handler.postDelayed(this, del);
				clickListener.onClick(downView);
			}
		};

		private View downView;

		/**
		 * @param initialInterval The interval after first click event
		 * @param normalInterval The interval after second and subsequent click
		 *       events
		 * @param clickListener The OnClickListener, that will be called
		 *       periodically
		 */
		public RepeatListener(int initialInterval, int normalInterval,
							  OnClickListener clickListener) {
			if (clickListener == null)
				throw new IllegalArgumentException("null runnable");
			if (initialInterval < 0 || normalInterval < 0)
				throw new IllegalArgumentException("negative interval");

			this.initialInterval = initialInterval;
			this.normalInterval = normalInterval;
			this.clickListener = clickListener;
		}

		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN:
					handler.removeCallbacks(handlerRunnable);
					handler.postDelayed(handlerRunnable, initialInterval);
					downView = view;
					downView.setPressed(true);
					clickListener.onClick(view);
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					handler.removeCallbacks(handlerRunnable);
					downView.setPressed(false);
					downView = null;
					runCount = 0;
					return true;
			}

			return false;
		}

	}
}
