package com.mindblowing.swingish;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.fragment.app.Fragment;
import javaawt.Color;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.Image;
import javaawt.Insets;
import javaawt.Rectangle;
import javaawt.VMFont;
import javaawt.VMGraphics2D;
import javaawt.image.ImageObserver;

/**
 * Very much an assistant with various methods to be sorted out
 * Note the Fragment part is not always employed, if this is just used to paint itself on other components
 */
public abstract class JComponent extends Fragment implements ImageObserver {
	private DrawableView drawableView;

	public JComponent() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// retain this fragment
		setRetainInstance(true);
	}

	public void setDrawableView(DrawableView drawableView) {
		this.drawableView = drawableView;
	}

	public View getDrawableView() {
		return drawableView;
	}


	public Insets getInsets() {
		if (this.drawableView != null) {
			return this.drawableView.getInsets();
		} else {
			return new Insets(0, 0, 0, 0);
		}
	}

	//from ImageObserver, but hopefully never used
	public boolean imageUpdate(Image var1, int var2, int var3, int var4, int var5, int var6) {
		System.out.println("JComponent  imageUpdate called, should I now call repaint?");
		return true;
	}

	//from ImageObserver, but hopefully never used
	public Object getDelegate() {
		return null;
	}

	public void setOpaque(boolean b) {
	}

	public void setFocusable(boolean b) {
	}

	public void setAutoscrolls(boolean b) {
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isOpaque() {
		return true;
	}

	public Color getBackground() {
		return Color.WHITE;
	}

	public Color getForeground() {
		return Color.BLACK;
	}


	public int getWidth()
	{
		return drawableView != null ? drawableView.getWidth() : width;
	}

	public int getHeight()
	{
		return drawableView != null ? drawableView.getHeight() : height;
	}

	private int width = 0;
	private int height = 0;

	// used if the drawable is null
	public void setWidth(int w) {
		width = w;
	}

	public void setHeight(int h) {
		height = h;
	}

	// not getParent()!

	//must be implemtned in subclasses
	protected void scrollRectToVisible(Rectangle shapePixelBounds) {
	}

	public void repaint() {
		if (this.drawableView != null) {
			this.drawableView.postInvalidate();
		}
	}


	public void revalidate() {
		if (this.drawableView != null)
			this.drawableView.postInvalidate();
	}


	// this guy is the guy
	public abstract void paintComponent(Graphics g);


	private Font currentFont = new VMFont(Typeface.DEFAULT, 24);

	public Font getFont() {
		return currentFont;
	}

	public void setFont(Font f) {
		currentFont = f;
	}

	//terrible utility always null
	// mainly used by getItems bounds which just wants the font
	public Graphics getGraphics() {
		return null;
	}

	public Rectangle getVisibleRect() {
		Rect r = new Rect();
		// view null if not showing!
		if (this.getView() != null) {
			this.getView().getLocalVisibleRect(r);
		}
		return new Rectangle(r.left, r.bottom, r.right - r.left, r.top - r.bottom);
	}

	private static android.graphics.Paint fontSizingPaint = new android.graphics.Paint();

	protected static Paint.FontMetrics getFontMetrics(Font f) {
		//PJPJPJ
		//android.graphics.Paint.FontMetrics
		//https://developer.android.com/reference/android/graphics/Paint.FontMetrics.html
		synchronized(fontSizingPaint) {
			fontSizingPaint.setTextSize(f.getSize());
			fontSizingPaint.setTypeface((Typeface) f.getDelegate());
			Paint.FontMetrics fontMetrics = fontSizingPaint.getFontMetrics();
			return fontMetrics;
		}
	}

	//http://stackoverflow.com/questions/24890900/using-a-custom-surfaceview-and-thread-for-android-game-programming-example
	public static class DrawableView extends SurfaceView implements SurfaceHolder.Callback {
		private SurfaceHolder holder;
		private JComponent drawer = null;

		public DrawableView(Context context, AttributeSet attrs) {
			super(context, attrs);
			holder = getHolder();

			holder.addCallback(this);
		}

		protected void onSizeChanged (int w, int h, int oldw, int oldh) {
			if(drawer != null)
				drawer.revalidate();
		}
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			setWillNotDraw(false);
		}

		@Override
		// This is always called at least once, after surfaceCreated
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}

		public void setDrawer(JComponent c) {
			drawer = c;
		}

		private Canvas previousCanvas = null;
		private VMGraphics2D previousVMGraphics2D = null;
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (drawer != null) {
				Graphics2D g = new VMGraphics2D(canvas);
				drawer.paintComponent(g);
				g.dispose();
			}
		}

		public Insets getInsets() {
			Insets insets = new Insets(this.getPaddingLeft(), this.getPaddingRight(), this.getPaddingTop(), this.getPaddingBottom());
			return insets;
		}
	}
}
