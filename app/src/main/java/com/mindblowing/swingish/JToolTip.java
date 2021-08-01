package com.mindblowing.swingish;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class JToolTip extends TextView {
	public JToolTip(Context context) {
		super(context);
		this.setTextAppearance(context, android.R.style.TextAppearance_Medium);
	}

	public void setTipText(String toolTipFeedback) {
		this.setText(Html.fromHtml(toolTipFeedback));
	}
}
