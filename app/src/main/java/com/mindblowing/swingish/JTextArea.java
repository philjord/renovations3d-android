package com.mindblowing.swingish;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by phil on 2/1/2017.
 * Note these are single line and have a done button that hides them
 */

public class JTextArea extends EditText
{

	public JTextArea(Context context, AttributeSet attrs) {
		super(context, attrs);
		//JTextField default to assume no next type options
		setImeOptions(EditorInfo.IME_ACTION_DONE);
		setOnEditorActionListener(new OnEditorActionListener() {
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
	public JTextArea(Context context, String text)
	{
		super(context);
		setText(text);

		//JTextField default to assume no next type options
		setImeOptions(EditorInfo.IME_ACTION_DONE);
		setOnEditorActionListener(new OnEditorActionListener() {
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

	public void setVisible(boolean visible)
	{
		super.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}
}
