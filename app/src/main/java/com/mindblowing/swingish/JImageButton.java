
package com.mindblowing.swingish;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by phil on 2/1/2017.
 */
@SuppressLint("AppCompatCustomView")
public class JImageButton extends ImageButton
{
	public JImageButton(Context context, Bitmap image)
	{
		super(context);
		setScaleType(ScaleType.FIT_CENTER);
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
