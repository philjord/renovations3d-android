package com.eteks.renovations3d.android.swingish;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.eteks.renovations3d.android.DrawableView;

import javaawt.Color;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Image;
import javaawt.Insets;
import javaawt.Rectangle;
import javaawt.VMFont;
import javaawt.geom.Rectangle2D;
import javaawt.image.ImageObserver;

/**
 * Very much an assistant with various methods to be sorted out
 * Note the Fragment part is not always employed, if this is just used to paint itself on other components
 */
public abstract class JComponent extends Fragment implements ImageObserver
{
	private DrawableView drawableView;

	public JComponent()
	{

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// retain this fragment
		setRetainInstance(true);
	}

	public void setDrawableView(DrawableView drawableView)
	{
		this.drawableView = drawableView;
	}

	protected View getDrawableView()
	{
		return drawableView;
	}


	public Insets getInsets()
	{
		if (this.drawableView != null)
		{
			return this.drawableView.getInsets();
		}
		else
		{
			return new Insets(0, 0, 0, 0);
		}
	}

	//from ImageObserver, but hopefully never used
	public boolean imageUpdate(Image var1, int var2, int var3, int var4, int var5, int var6)
	{
		System.out.println("JComponent  imageUpdate called, should I now call repaint?");
		return true;
	}

	//from ImageObserver, but hopefully never used
	public Object getDelegate()
	{
		return null;
	}

	public void setOpaque(boolean b)
	{
	}

	public void setFocusable(boolean b)
	{
	}

	public void setAutoscrolls(boolean b)
	{
	}

	public boolean isEnabled()
	{
		return true;
	}

	public boolean isOpaque()
	{
		return true;
	}

	public Color getBackground()
	{
		return Color.WHITE;
	}

	public Color getForeground()
	{
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
	public void setWidth(int w)
	{
		width = w;
	}

	public void setHeight(int h)
	{
		height = h;
	}


	// not getParent()!

	protected void scrollRectToVisible(Rectangle shapePixelBounds)
	{
		//TODO: this feels important
		System.out.println("JComponent  scrollRectToVisible(Rectangle shapePixelBounds)");
	}

	public void repaint()
	{
		if (this.drawableView != null)
		{
			this.drawableView.postInvalidate();
		}
	}


	public void revalidate()
	{
		if (this.drawableView != null)
			this.drawableView.postInvalidate();
	}


	// this guy is the guy
	public abstract void paintComponent(Graphics g);


	private Font currentFont = new VMFont(Typeface.DEFAULT, 24);

	public Font getFont()
	{
		return currentFont;
	}

	public void setFont(Font f)
	{
		currentFont = f;
	}

	//terrible utility always null
	// mainly used by getItems bounds which just wants the font
	public Graphics getGraphics()
	{
		return null;
	}

	public Rectangle getVisibleRect()
	{
		Rect r = new Rect();
		// view null if not showing!
		if( this.getView() != null )
		{
			this.getView().getLocalVisibleRect(r);
		}
		return new Rectangle(r.left, r.bottom, r.right - r.left, r.top - r.bottom);
	}

	private static android.graphics.Paint fontSizingPaint = new android.graphics.Paint();

	protected static Paint.FontMetrics getFontMetrics(Font f)
	{
		//PJPJPJ
		//android.graphics.Paint.FontMetrics
		//https://developer.android.com/reference/android/graphics/Paint.FontMetrics.html
		fontSizingPaint.setTypeface((Typeface) (((VMFont) f).getDelegate()));
		Paint.FontMetrics fontMetrics = fontSizingPaint.getFontMetrics();
		return fontMetrics;
	}

	// handy util not part of JComponent
	public static Rectangle2D getStringBounds(String text, Font f)
	{
		fontSizingPaint.setTypeface((Typeface) (((VMFont) f).getDelegate()));
		fontSizingPaint.setTextSize(f.getSize());
		Rect r = new Rect();
		fontSizingPaint.getTextBounds(text, 0, text.length(), r);
		Rectangle2D textBounds = new Rectangle(r.left, r.bottom, r.right - r.left, r.top - r.bottom);
		return textBounds;
	}

}
