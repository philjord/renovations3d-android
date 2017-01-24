package com.eteks.sweethomeavr.android;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;

import javaxswing.Icon;
import javaxswing.ImageIcon;

import static android.R.attr.type;
import static android.R.id.message;
import static android.R.string.cancel;
import static com.mindblowing.sweethomeavr.R.id.delete;


/**
 * Created by phil on 1/23/2017.
 */

public class JOptionPane
{
	//message types
	public static final int INFORMATION_MESSAGE = 0;
	public static final int ERROR_MESSAGE = 1;
	public static final int QUESTION_MESSAGE = 2;
	public static final int WARNING_MESSAGE = 3;

	// return types
	public static final int OK_OPTION = 0;
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;

	// option types
	public static final int OK_CANCEL_OPTION = 0;
	public static final int YES_NO_CANCEL_OPTION = 1;
	public static final int YES_NO_OPTION = 2;


	/**
	 *
	 * @param context
	 * @param message can /should be html
	 * @param title
	 * @param type ignored
	 * @param icon must be an ImageIcon
	 */
	public static void showMessageDialog(Context context, String message, String title, int type, Icon icon)
	{
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle(title);
		if(icon != null && icon instanceof ImageIcon)
		{
			BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
			dialog.setIcon(bmd);
		}
		dialog.setMessage(Html.fromHtml(message));
		dialog.show();
	}
	public static void showMessageDialog(Context context, String message, String title, int type)
	{
		showMessageDialog(context, message, title, type, null);
	}

	public static int showOptionDialog(Context context, String message, String title, int options, int type,
					 Icon icon, Object [] optionsText, Object defaultText)
	{
		AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.setTitle(title);
		if(icon != null && icon instanceof ImageIcon)
		{
			BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
			dialog.setIcon(bmd);
		}
		dialog.setMessage(Html.fromHtml(message));
		dialog.show();
		//TODO: fixme in some modal manner!, see FileContent Manager too the showFileChooser method
		// modal is hard http://stackoverflow.com/questions/6120567/android-how-to-get-a-modal-dialog-or-similar-modal-behavior
		// but there are a few solutions so long as you are not on teh vent thread (probably true??
		return OK_OPTION;
	}

}
