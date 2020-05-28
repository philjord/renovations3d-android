
package com.mindblowing.swingish;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by phil on 2/1/2017.
 */

public class JImageButton extends ImageButton
{
	public JImageButton(Context context, Bitmap image)
	{
		super(context);
		setImageBitmap(image);
	}

	public void addActionListener(final ActionListener actionListener)
	{
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				actionListener.actionPerformed(null);
			}});
	}
}
