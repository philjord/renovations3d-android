package com.eteks.renovations3d.android.swingish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.SwingTools;
import com.eteks.sweethome3d.model.UserPreferences;

import org.xml.sax.XMLReader;

import java.util.concurrent.Semaphore;

import javaxswing.Icon;
import javaxswing.ImageIcon;

import static com.eteks.renovations3d.Renovations3DActivity.PREFS_NAME;


/**
 * Created by phil on 1/23/2017.
 */

public class JOptionPane
{
	//
	// Option types
	//

	/**
	 * Type meaning Look and Feel should not supply any options -- only
	 * use the options from the <code>JOptionPane</code>.
	 */
	public static final int         DEFAULT_OPTION = -1;
	/** Type used for <code>showConfirmDialog</code>. */
	public static final int         YES_NO_OPTION = 0;
	/** Type used for <code>showConfirmDialog</code>. */
	public static final int         YES_NO_CANCEL_OPTION = 1;
	/** Type used for <code>showConfirmDialog</code>. */
	public static final int         OK_CANCEL_OPTION = 2;

	//
	// Return values.
	//
	/** Return value from class method if YES is chosen. */
	public static final int         YES_OPTION = 0;
	/** Return value from class method if NO is chosen. */
	public static final int         NO_OPTION = 1;
	/** Return value from class method if CANCEL is chosen. */
	public static final int         CANCEL_OPTION = 2;
	/** Return value form class method if OK is chosen. */
	public static final int         OK_OPTION = 0;
	/** Return value from class method if user closes window without selecting
	 * anything, more than likely this should be treated as either a
	 * <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>. */
	public static final int         CLOSED_OPTION = -1;

	//
	// Message types. Used by the UI to determine what icon to display,
	// and possibly what behavior to give based on the type.
	//
	/** Used for error messages. */
	public static final int  ERROR_MESSAGE = 0;
	/** Used for information messages. */
	public static final int  INFORMATION_MESSAGE = 1;
	/** Used for warning messages. */
	public static final int  WARNING_MESSAGE = 2;
	/** Used for questions. */
	public static final int  QUESTION_MESSAGE = 3;
	/** No icon is used. */
	public static final int   PLAIN_MESSAGE = -1;

	public static void showMessageDialog(Context context, String message, String title, int type)
	{
		showMessageDialog(context, message, title, type, null, "OK");
	}
	public static void showMessageDialog(final Context context, final String message, final String title, final int type, final Icon icon)
	{
		showMessageDialog(context, message, title, type, icon, "OK");
	}
	public static void showMessageDialog(Context context, String message, String title, int type, final String closeText)
	{
		showMessageDialog(context, message, title, type, null, closeText);
	}

	/**
	 *
	 * @param context
	 * @param message can /should be html
	 * @param title
	 * @param type ignored
	 * @param icon must be an ImageIcon
	 */
	public static void showMessageDialog(final Context context, final String message, final String title, final int type, final Icon icon, final String closeText)
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			public void run()
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle(title);
				if (icon != null && icon instanceof ImageIcon)
				{
					BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
					dialog.setIcon(bmd);
				}
				String messageLessStyle = message.replaceAll("<style([\\s\\S]+?)</style>", "");
				dialog.setMessage(Html.fromHtml(messageLessStyle, null, new ListTagHandler()));
				dialog.setPositiveButton(closeText,  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
				dialog.create().show();
		}	});
	}

	/**
	 *
	 * @param context
	 * @param root a text view
	 * @param title
	 * @param type ignored
	 * @param icon must be an ImageIcon
	 */
	public static void showMessageDialog(final Context context, final View root, final String title, final int type, final Icon icon, final String closeText)
	{
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			public void run()
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle(title);
				if (icon != null && icon instanceof ImageIcon)
				{
					BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
					dialog.setIcon(bmd);
				}
				dialog.setView(root);
				dialog.setPositiveButton(closeText,  new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
				dialog.create().show();
			}	});
	}

	public static int showConfirmDialog(final Context context, final String message, final String title, final int options, final int type)
	{
		//TODO: for teh null case I should use those real localized proeprty file values
		return showOptionDialog(context, message, title, options, type, null, null, null);
	}

	public static int showOptionDialog(final Context context, final String message, final String title, final int options, final int type,
									   final Icon icon, final Object [] optionsText, Object defaultText)
	{
		if(Looper.getMainLooper().getThread() == Thread.currentThread())
		{
			new Throwable().printStackTrace();
			System.err.println("JOptionPane asked to showOptionDialog (String message) on EDT thread you MUST not as I will block!");
			// In this case you likely want just a new Thread (not the handler version)
			return NO_OPTION;
		}

		final int[] selectedOption = new int[]{CANCEL_OPTION};
		final Semaphore dialogSemaphore = new Semaphore(0, true);

		// if this is not a loopery thread you get java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
					 {
						 public void run()
						 {
							 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
								 @Override
								 public void onClick(DialogInterface dialog, int which) {
									 switch (which){
										 case DialogInterface.BUTTON_POSITIVE:
											 selectedOption[0] = OK_OPTION;
											 dialogSemaphore.release();
											 break;
										 case DialogInterface.BUTTON_NEGATIVE:
											 selectedOption[0] = NO_OPTION;
											 dialogSemaphore.release();
											 break;
									 }
								 }
							 };

							 AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
							 dialog.setTitle(title);
							 if (icon != null && icon instanceof ImageIcon)
							 {
								 BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
								 dialog.setIcon(bmd);
							 }

							 // remove the style tags
							 String messageLessStyle = message.replaceAll("<style([\\s\\S]+?)</style>", "");
							 dialog.setMessage(Html.fromHtml(messageLessStyle, null, new ListTagHandler()));

								//TODO: for the null case I should use those real localized proeprty file values
							 dialog.setPositiveButton((String)((optionsText!=null && optionsText.length>0)?optionsText[0]:"Yes"), dialogClickListener);
							 dialog.setNegativeButton((String)((optionsText!=null && optionsText.length>1)?optionsText[1]:"No"), dialogClickListener);
							 dialog.setCancelable(options == YES_NO_OPTION || options == OK_CANCEL_OPTION);
							 dialog.create().show();
						 }
					 });

		try
		{
			dialogSemaphore.acquire();
		}
		catch (InterruptedException e)
		{
		}

		return selectedOption[0] ;

	}





	public static int showOptionDialog(final Context context, final View root, final String title, final int options, final int type,
									   final Icon icon, final Object [] optionsText, Object defaultText)
	{
		if(Looper.getMainLooper().getThread() == Thread.currentThread())
		{
			new Throwable().printStackTrace();
			System.err.println("JOptionPane asked to showOptionDialog (View root) on EDT thread you MUST not as I will block!");
			return NO_OPTION;
		}

		final int[] selectedOption = new int[]{CANCEL_OPTION};
		final Semaphore dialogSemaphore = new Semaphore(0, true);

		// if this is not a loopery thread you get java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{
			public void run()
			{
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
							case DialogInterface.BUTTON_POSITIVE:
								selectedOption[0] = OK_OPTION;

								//https://console.firebase.google.com/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/1ac9ba79?duration=2592000000&appVersions=190
								if(root.getParent() != null)
									((ViewGroup)root.getParent()).removeView(root);//oddly dismiss doesn't do this
								dialogSemaphore.release();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								selectedOption[0] = NO_OPTION;
								if(root.getParent() != null)
									((ViewGroup)root.getParent()).removeView(root);//oddly dismiss doesn't do this
								dialogSemaphore.release();
								break;
						}
					}
				};

				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				dialog.setTitle(title);
				if (icon != null && icon instanceof ImageIcon)
				{
					BitmapDrawable bmd = new BitmapDrawable(context.getResources(), (Bitmap) ((ImageIcon) icon).getImage().getDelegate());
					dialog.setIcon(bmd);
				}
				dialog.setView(root);

				dialog.setPositiveButton((String)((optionsText!=null && optionsText.length>0)?optionsText[0]:"Yes"), dialogClickListener);
				dialog.setNegativeButton((String)((optionsText!=null && optionsText.length>1)?optionsText[1]:"No"), dialogClickListener);
				dialog.setCancelable(options == YES_NO_OPTION || options == OK_CANCEL_OPTION);
				dialog.create().show();
			}
		});

		try
		{
			dialogSemaphore.acquire();
		}
		catch (InterruptedException e)
		{
		}

		return selectedOption[0] ;

	}




	public static class ListTagHandler implements Html.TagHandler
	{
		boolean first= true;
		String parent=null;
		int index=1;
		@Override
		public void handleTag(boolean opening, String tag, Editable output,
							  XMLReader xmlReader) {

			if(tag.equals("ul"))
				parent="ul";
			else if(tag.equals("ol"))
				parent="ol";

			if(tag.equals("li")){
				if(parent.equals("ul")){
					if(first){
						output.append("\n\t•");
						first= false;
					}else{
						first = true;
					}
				}
				else{
					if(first){
						output.append("\n\t"+index+". ");
						first= false;
						index++;
					}else{
						first = true;
					}
				}
			}
		}
	}


	public static void possiblyShowWelcomeScreen(final Context context, final String welcomeScreenName, int welcomeTextId, UserPreferences preferences)
	{
		// only one per session
		if(!Renovations3DActivity.welcomeScreensShownThisSession.contains(welcomeScreenName))
		{
			Renovations3DActivity.welcomeScreensShownThisSession.add(welcomeScreenName);
			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
			boolean welcomeScreenUnwanted = settings.getBoolean(welcomeScreenName, false);

			if (!welcomeScreenUnwanted)
			{
				final String close = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "about.close");
				final String closeAndNoShow = SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.HomePane.class, "doNotDisplayTipCheckBox.text");
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
						SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(welcomeScreenName, true);
						editor.apply();

					}
				});

				String welcomeMessage = context.getString(welcomeTextId);
				TextView textView = new TextView(context);
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
