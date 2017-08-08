package com.eteks.renovations3d.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.SwingTools;
import com.eteks.sweethome3d.model.UserPreferences;

import static com.eteks.renovations3d.Renovations3DActivity.PREFS_NAME;

/**
 * Created by phil on 8/8/2017.
 */

public class WelcomeDialog
{
	public static void possiblyShowWelcomeScreen(final Renovations3DActivity context, final String welcomeScreenName, int welcomeTextId, UserPreferences preferences)
	{
		// only one per session
		if((context.getTutorial() == null || !context.getTutorial().isEnabled()) && !context.getWelcomeScreensShownThisSession().contains(welcomeScreenName))
		{
			context.getWelcomeScreensShownThisSession().add(welcomeScreenName);
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
				if(!((Activity) context).isFinishing())
				{
					dialog.show();
				}
			}
		}
	}
}
