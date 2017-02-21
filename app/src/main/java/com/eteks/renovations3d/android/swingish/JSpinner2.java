package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.Format;


/**
 * Created by phil on 2/1/2017.
 */

public class JSpinner2 extends EditText
{
	protected SpinnerNumberModel model;
	protected Format currentFormat;

	public JSpinner2(Context context, SpinnerNumberModel model)
	{
		this(context, model, null);
	}

	public JSpinner2(Context context, final SpinnerNumberModel model, Format format)
	{
		super(context);
		this.model = model;

		setFormat(format);

		this.setMinEms(10);
		this.setMaxLines(1);
		setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

		final ChangeListener changerListener = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent ev)
			{
				setText("" + model.getValue());// textWatcher formats later
			}
		};
		model.addChangeListener(changerListener);

		TextWatcher textWatcher = new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
				/*removeTextChangedListener(this);
				String text = s.toString();
				if (currentFormat != null)
					s.replace(0, s.length(), currentFormat.format(Double.parseDouble(text)));
				else
					s.replace(0, s.length(), text);

				addTextChangedListener(this);*/
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
		};

		addTextChangedListener(textWatcher);

	}

	public SpinnerNumberModel getModel()
	{
		return model;
	}

	public void setValue(int value)
	{
		model.setValue(value);
	}

	public void setFormat(Format format)
	{
		this.currentFormat = format;
	}
}
