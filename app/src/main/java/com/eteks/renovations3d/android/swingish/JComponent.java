package com.eteks.renovations3d.android.swingish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.SwingTools;
import com.eteks.renovations3d.android.utils.DrawableView;
import com.eteks.sweethome3d.model.UserPreferences;
import com.mindblowing.renovations3d.R;

import javaawt.Color;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Image;
import javaawt.Insets;
import javaawt.Rectangle;
import javaawt.VMFont;
import javaawt.geom.Rectangle2D;
import javaawt.image.ImageObserver;

import static com.eteks.renovations3d.Renovations3DActivity.PREFS_NAME;

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
		fontSizingPaint.setTextSize(f.getSize());
		fontSizingPaint.setTypeface((Typeface) (((VMFont) f).getDelegate()));
		Paint.FontMetrics fontMetrics = fontSizingPaint.getFontMetrics();

		return fontMetrics;
	}

	// handy util not part of JComponent
	public static Rectangle2D getStringBounds(String text, Font f)
	{
		//TODO: Font now has this method and should be used
		fontSizingPaint.setTypeface((Typeface) (((VMFont) f).getDelegate()));
		fontSizingPaint.setTextSize(f.getSize());
		Rect r = new Rect();
		fontSizingPaint.getTextBounds(text, 0, text.length(), r);
		Rectangle2D textBounds = new Rectangle(r.left, r.bottom, r.right - r.left, r.top - r.bottom);
		return textBounds;
	}

	public static void possiblyShowWelcomeScreen(final Activity activity, final String welcomeScreenName, int welcomeTextId, UserPreferences preferences)
	{
		// only one per session
		if(!Renovations3DActivity.welcomeScreensShownThisSession.contains(welcomeScreenName))
		{
			Renovations3DActivity.welcomeScreensShownThisSession.add(welcomeScreenName);
			SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
			boolean welcomeScreenUnwanted = settings.getBoolean(welcomeScreenName, false);

			if (!welcomeScreenUnwanted)
			{
				final String close = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "about.close");
				final String closeAndNoShow = SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomePane.class, "doNotDisplayTipCheckBox.text");
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				// Add the buttons
				builder.setPositiveButton(close, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// do remind again so no prefs
					}
				});
				builder.setNegativeButton(closeAndNoShow, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// don't remind again
						SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(welcomeScreenName, true);
						editor.apply();

					}
				});

				String welcomeMessage = activity.getString(welcomeTextId);
				TextView textView = new TextView(activity);
				textView.setPadding(10,10,10,10);
				textView.setText(Html.fromHtml(welcomeMessage));
				textView.setMovementMethod(LinkMovementMethod.getInstance());

				builder.setView(textView);

				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}
	}
}
