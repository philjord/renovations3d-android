
package com.mindblowing.swingish;

import android.content.Context;
import android.view.View;

/**
 * Created by phil on 2/1/2017.
 */

public class JButton extends android.support.v7.widget.AppCompatButton
{
	public JButton(Context context, String text)
	{
		super(context);
		setText(text);
	}

	public void addActionListener(final ActionListener actionListener)
	{
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				actionListener.actionPerformed(null);
			}});
	}
}
