package com.eteks.renovations3d;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

/**
 * Created by phil on 5/15/2017.
 */

public class AdMobManager
{

	private static final boolean ENABLE_INTERSTITIALS = false;


	private static final boolean SUPPRESS_ADS = false;//for debug and screenshots overrides force_ads
	private static final boolean FORCE_DEBUG_ADS = true;//for debug build to still have ads <= PREFER this one! Less dangerous
	private static final String INTERSTITIAL_POINTS = "INTERSTITIAL_POINTS";
	private static final String INTERSTITIAL_LAST_SHOWN_TIME = "INTERSTITIAL_LAST_SHOWN_TIME";
	private static final int INTERSTITIAL_POINTS_THRESHOLD = 10;
	private static final long INTERSTITIAL_TIME_THRESHOLD = 2 * 24 * 60 * 60 * 1000; // 2 days in ms
	private Renovations3DActivity renovations3DActivity;
	private AdRequest adRequest;
	private AdView mBasicLowerBannerAdView;
	private InterstitialAd mInterstitialAd;


	public AdMobManager(Renovations3DActivity renovations3DActivity)
	{
		this.renovations3DActivity = renovations3DActivity;

		AdRequest.Builder builder = new AdRequest.Builder();
		//example logcat Use AdRequest.Builder.addTestDevice("4A1B3B44655FDE15F64CFD90EFD60699") to get test ads on this device.
		builder.addTestDevice("56ACE73C453B9562B288E8C2075BDA73");//T580
		builder.addTestDevice("4A1B3B44655FDE15F64CFD90EFD60699");//I9505
		builder.addTestDevice("F1F03BC6248C8ECF32CBB4DD027F78B9");//T210
		adRequest = builder.build();

		mBasicLowerBannerAdView = (AdView) renovations3DActivity.findViewById(R.id.lowerBannerAdView);

		mInterstitialAd = new InterstitialAd(renovations3DActivity);
		mInterstitialAd.setAdUnitId("ca-app-pub-7177705441403385/4587558769");

	}

	/**
	 * called by billing to indicate it can now service questions
	 */
	public void billingServiceConnected()
	{
		if (mBasicLowerBannerAdView != null && renovations3DActivity != null)
		{
			// set up admob ads if they don't own ad free option and it's not debug
			if (shouldSuppressAds())
			{
				// same thing should be called when purchase complete
				mBasicLowerBannerAdView.setEnabled(false);
				mBasicLowerBannerAdView.setVisibility(View.GONE);
			}
			else
			{
				MobileAds.initialize(renovations3DActivity.getApplicationContext(), "ca-app-pub-7177705441403385~4026888158");
				mBasicLowerBannerAdView.loadAd(adRequest);
				if(ENABLE_INTERSTITIALS)
					mInterstitialAd.loadAd(adRequest);
			}
		}
	}

	public void removeBasicLowerBannerAdView()
	{
		if (mBasicLowerBannerAdView != null)
		{
			ViewGroup parent = (ViewGroup) mBasicLowerBannerAdView.getParent();
			if (parent != null)
			{
				parent.removeView(mBasicLowerBannerAdView);
				parent.invalidate();
			}
		}
	}

	private boolean shouldSuppressAds()
	{
		return mBasicLowerBannerAdView == null || renovations3DActivity == null || SUPPRESS_ADS || renovations3DActivity.getBillingManager().ownsBasicAdFree() || (BuildConfig.DEBUG && !FORCE_DEBUG_ADS);
	}

	public void hide()
	{
		if (mBasicLowerBannerAdView != null)
		{
			mBasicLowerBannerAdView.setVisibility(View.GONE);
			ViewGroup parent = (ViewGroup) mBasicLowerBannerAdView.getParent();
			if (parent != null)
			{
				parent.invalidate();
			}
		}
	}

	public void show()
	{
		if (mBasicLowerBannerAdView != null && renovations3DActivity != null)
		{
			if (!shouldSuppressAds())
			{
				if (mBasicLowerBannerAdView != null)
				{
					mBasicLowerBannerAdView.setVisibility(View.VISIBLE);
					ViewGroup parent = (ViewGroup) mBasicLowerBannerAdView.getParent();
					if (parent != null)
					{
						parent.invalidate();
					}
				}
			}
		}
	}


	// interstital support
	public enum InterstitialEventType
	{
		HOME_SAVE_AS, //1pt
		HOME_SHARE, //1pt
		NEW_HOME, //1pt
		PHOTO_SAVE_OR_SHARE, //2pt
		IMPORT_FURNITURE, //1pt
		IMPORT_TEXTURE,//1pt
		IMPORT_BACKGROUND,//1pt
	}

	public void eventTriggered(InterstitialEventType type)
	{
		if (renovations3DActivity != null && !shouldSuppressAds())
		{
			SharedPreferences settings = renovations3DActivity.getSharedPreferences(Renovations3DActivity.PREFS_NAME, 0);
			int prevPoints = settings.getInt(INTERSTITIAL_POINTS, 0);
			int points = prevPoints + (type == InterstitialEventType.HOME_SAVE_AS ? 1 :
					type == InterstitialEventType.HOME_SHARE ? 1 :
							type == InterstitialEventType.NEW_HOME ? 1 :
									type == InterstitialEventType.PHOTO_SAVE_OR_SHARE ? 2 :
											type == InterstitialEventType.IMPORT_FURNITURE ? 1 :
													type == InterstitialEventType.IMPORT_TEXTURE ? 1 :
															type == InterstitialEventType.IMPORT_BACKGROUND ? 1 :
																	0);

			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "INTERSTITIAL " + type.name(), "pt=" + points);

			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(INTERSTITIAL_POINTS, points);
			editor.apply();

		}
	}

	public void interstitialDisplayPoint()
	{
		if (renovations3DActivity != null && !shouldSuppressAds())
		{
			renovations3DActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					SharedPreferences settings = renovations3DActivity.getSharedPreferences(Renovations3DActivity.PREFS_NAME, 0);

					int points = settings.getInt(INTERSTITIAL_POINTS, 0);

					long lastShownTime = settings.getLong(INTERSTITIAL_LAST_SHOWN_TIME, 0);
					// is now the time to show an interstitial??
					if (points >= INTERSTITIAL_POINTS_THRESHOLD && System.currentTimeMillis() - lastShownTime > INTERSTITIAL_TIME_THRESHOLD)
					{
						if (ENABLE_INTERSTITIALS && mInterstitialAd.isLoaded())
						{
							Renovations3DActivity.logFireBaseContent("INTERSTITIAL SHOWN", null);
							// zero points and record exact time
							SharedPreferences.Editor editor = settings.edit();
							editor.putInt(INTERSTITIAL_POINTS, 0);
							editor.putLong(INTERSTITIAL_LAST_SHOWN_TIME, System.currentTimeMillis());
							editor.apply();


							mInterstitialAd.setAdListener(new AdListener() {
								@Override
								public void onAdClosed() {
									//This is how I would load the next ad, but because of the time threshold removed to reduce overhead
									// Load the next interstitial.
									//mInterstitialAd.loadAd(adRequest);
								}
								@Override
								public void onAdFailedToLoad(int errorCode) {
									Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "INTERSTITIAL onAdFailedToLoad " + errorCode, null);
								}
							});

							mInterstitialAd.show();

						}
						else
						{
							Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "INTERSTITIAL NOT LOADED", null);
						}
					}
				}});
		}
	}
}
