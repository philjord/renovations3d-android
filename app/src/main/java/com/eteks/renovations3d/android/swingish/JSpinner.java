package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eteks.sweethome3d.viewcontroller.LabelController;

import java.text.Format;
import java.text.ParseException;


/**
 * Created by phil on 2/1/2017.
 * A JSpinner that works like the java desktop version
 */

public class JSpinner extends LinearLayout
{
	protected AbstractSpinnerModel model;
	protected Format currentFormat;

	protected TextView output;
	private Button upButton;
	private Button downButton;

	private TextWatcher textWatcher = new TextWatcher(){
	public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
	public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
	public void afterTextChanged(Editable arg0) {
		model.setValue(Float.parseFloat(output.getText().toString()));
	}};



	public JSpinner(Context context, AbstractSpinnerModel model)
	{
		this(context, model, null);
	}

	public JSpinner(Context context, final AbstractSpinnerModel model, Format format)
	{
		 this(context, model, format, false);
	}

	public JSpinner(Context context, final AbstractSpinnerModel model, Format format, boolean allowTextEntry)
	{
		super(context);
		this.model = model;

		this.setOrientation(LinearLayout.VERTICAL);
		if(!allowTextEntry)
		{
			output = new TextView(context);
		}
		else
		{
			output = new EditText(context);
			output.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		}
		setFormat(format);


		output.setTextAppearance(context, android.R.style.TextAppearance_Large);
		output.setMinEms(6);
		output.setMaxLines(1);
		output.addTextChangedListener(textWatcher);

		final ChangeListener changerListener = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent ev)
			{
				output.removeTextChangedListener(textWatcher);
				if(currentFormat != null)
					output.setText(currentFormat.format(model.getValue()));
				else
					output.setText("" + model.getValue());
				output.addTextChangedListener(textWatcher);
			}
		};
		model.addChangeListener(changerListener);



		SpannableStringBuilder upSB = new SpannableStringBuilder("*");// it will replace "*" with icon
		upSB.setSpan(new ImageSpan(context, android.R.drawable.arrow_up_float), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		upButton = new Button(context);
		upButton.setText(upSB);
		upButton.setPadding(0,0,0,0);

		SpannableStringBuilder downSB = new SpannableStringBuilder("*");// it will replace "*" with icon
		downSB.setSpan(new ImageSpan(context, android.R.drawable.arrow_down_float), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		downButton = new Button(context);
		downButton.setText(downSB);
		downButton.setPadding(0,0,0,0);


		final float scale = getResources().getDisplayMetrics().density;
		int heightSizePx = (int) (30 * scale + 0.5f);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, heightSizePx, 0);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		this.addView(upButton, lp);
		this.addView(output, lp2);
		this.addView(downButton, lp);

		upButton.setOnTouchListener(new RepeatListener(400, 75, new OnClickListener() {
			@Override
			public void onClick(View view) {
				setValue(model.getNextValue());
			}
		}));

		downButton.setOnTouchListener(new RepeatListener(400, 75, new OnClickListener() {
			@Override
			public void onClick(View view) {
				setValue(model.getPreviousValue());
			}
		}));

		this.setValue(model.getValue());
	}

	public void setEnabled(boolean enabled)
	{
		upButton.setEnabled(enabled);
		output.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

	public AbstractSpinnerModel getModel()
	{
		return model;
	}

	public void setValue(Object value)
	{
		model.setValue(value);
	}

	public void setFormat(Format format)
	{
		this.currentFormat = format;
		if(currentFormat != null)
			output.setText(currentFormat.format(model.getValue()));
		else
			output.setText("" + model.getValue());
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


			//TODO: add some slop in so the users can be a vague down, instead of all moves returning false;
			//mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
			return false;
		}

	}
}
