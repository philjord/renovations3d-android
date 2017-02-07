
package com.eteks.renovations3d.android.swingish;

import android.content.Context;
import android.view.View;
import android.widget.Button;

/**
 * Created by phil on 2/1/2017.
 */

public class JButton extends Button
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
