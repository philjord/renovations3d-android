package com.eteks.renovations3d.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.eteks.renovations3d.android.swingish.JComponent;

import javaawt.Graphics2D;
import javaawt.Insets;
import javaawt.VMGraphics2D;

//http://stackoverflow.com/questions/24890900/using-a-custom-surfaceview-and-thread-for-android-game-programming-example
public class DrawableView extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder holder;
	private JComponent drawer = null;

	public DrawableView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		holder = getHolder();

		holder.addCallback(this);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		setWillNotDraw(false);
	}

	@Override
	// This is always called at least once, after surfaceCreated
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{

	}

	public void setDrawer(JComponent c)
	{
		drawer = c;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (drawer != null)
		{
			Graphics2D g = new VMGraphics2D(canvas);
			drawer.paintComponent(g);
			g.dispose();
		}
	}

	public Insets getInsets()
	{
		Insets insets = new Insets(this.getPaddingLeft(), this.getPaddingRight(), this.getPaddingTop(), this.getPaddingBottom());
		return insets;
	}
}