package com.mindblowing.swingish;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * Created by phil on 2/1/2017.
 * Note these are single line and have a done button that hides them
 */

public class JTextField extends android.support.v7.widget.AppCompatEditText
{

	public JTextField(Context context)
	{
		super(context);
		setSingleLine();
		//JTextField default to assume no next type options
		setImeOptions(EditorInfo.IME_ACTION_DONE);
		setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_DONE){
					InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
									Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
				}
				return false;
			}
		});
	}
	public JTextField(Context context, int minEMS)
	{
		this(context);
		this.setMinEms(minEMS);
	}
	public JTextField(Context context, String text)
	{
		this(context);
		setText(text);
	}
	public JTextField(Context context, String text, int minEMS)
	{
		this(context);
		setText(text);
		this.setMinEms(minEMS);
	}
}
