package com.eteks.renovations3d;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 5/15/2017.
 */

public class AdMobManager
{
	private static final boolean SUPPRESS_ADS = false;//for debug and screenshots overrides force_ads
	public static final boolean FORCE_DEBUG_ADS = false;//for debug build to still have ads <= PREFER this one! Less dangerous
	private static final String INTERSTITIAL_POINTS = "INTERSTITIAL_POINTS";
	private static final String INTERSTITIAL_LAST_SHOWN_TIME = "INTERSTITIAL_LAST_SHOWN_TIME";
	private static final int INTERSTITIAL_POINTS_THRESHOLD = 10;
	private static final long INTERSTITIAL_TIME_THRESHOLD = 2 * 24 * 60 * 60 * 1000; // 2 days in ms
	private Renovations3DActivity renovations3DActivity;
	private AdView mBasicLowerBannerAdView;
	private InterstitialAd mInterstitialAd;

	private FirebaseRemoteConfig mFirebaseRemoteConfig;


	public AdMobManager(final Renovations3DActivity renovations3DActivity)
	{
		this.renovations3DActivity = renovations3DActivity;

		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600)
				.build();
		mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
		mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
		mFirebaseRemoteConfig.fetchAndActivate()
				.addOnCompleteListener(renovations3DActivity, new OnCompleteListener<Boolean>() {
					@Override
					public void onComplete(@NonNull Task<Boolean> task) {
						if (task.isSuccessful()) {
							boolean updated = task.getResult();
						} else {
						}
					}
				});

		if(BuildConfig.DEBUG) {
			List<String> testDevices = new ArrayList<>();
			//To get the Device ID Check the logcat output for a message that looks like the one below, which shows you your device ID and how to add it as a test device:

			//I/Ads: Use RequestConfiguration.Builder.setTestDeviceIds(Arrays.asList("33BE2250B43518CCDA7DE426D04EE231"))
			//to get test ads on this device."

			testDevices.add("56ACE73C453B9562B288E8C2075BDA73");//T580
			testDevices.add("4A1B3B44655FDE15F64CFD90EFD60699");//I9505
			testDevices.add("F1F03BC6248C8ECF32CBB4DD027F78B9");//T210
			testDevices.add("3A757DEE674365779CF90E1E82546B04");//samsung 9
			testDevices.add("39B3EC8638721A37B3E6B0DE8E5F414F");//samsung 9  again?
			testDevices.add("1E402426FE6157896DDA311AE9D06182");//s21 plus

			RequestConfiguration requestConfiguration
					= new RequestConfiguration.Builder()
					.setTestDeviceIds(testDevices)
					.build();
			MobileAds.setRequestConfiguration(requestConfiguration);
		}
		MobileAds.initialize(renovations3DActivity.getApplicationContext(), new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "MobileAds.initialize ", null);
			}
		});

		mBasicLowerBannerAdView = (AdView) renovations3DActivity.findViewById(R.id.lowerBannerAdView);

		mBasicLowerBannerAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				//Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "mBasicLowerBannerAdView.onAdLoaded ", null);
			}

			@Override
			public void onAdFailedToLoad(LoadAdError adError) {
				// Code to be executed when an ad request fails.
				// this will be called on BuildConfig.DEBUG==true if the device isn't in the test list above
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "mBasicLowerBannerAdView.onAdFailedToLoad ", null);
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
			}

			@Override
			public void onAdClicked() {
				// Code to be executed when the user clicks on an ad.
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when the user is about to return
				// to the app after tapping on an ad.
			}
		});

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
				removeBasicLowerBannerAdView();
			}
			else
			{
				mBasicLowerBannerAdView.loadAd(new AdRequest.Builder().build());
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "MobileAds.initialize ", null);

				if (mFirebaseRemoteConfig.getBoolean("renovations3d_interstitial_enabled")) {
					InterstitialAd.load(renovations3DActivity.getApplicationContext(), "ca-app-pub-7177705441403385/4587558769", new AdRequest.Builder().build(),
							new InterstitialAdLoadCallback() {
								@Override
								public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
									// The mInterstitialAd reference will be null until an ad is loaded.
									mInterstitialAd = interstitialAd;
								}

								@Override
								public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
									mInterstitialAd = null;
									Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "INTERSTITIAL onAdFailedToLoad " + loadAdError, null);
								}
							});
				}
			}
		}
	}

	public void removeBasicLowerBannerAdView()
	{
		if (mBasicLowerBannerAdView != null)
		{
			// same thing should be called when purchase complete
			mBasicLowerBannerAdView.setEnabled(false);
			mBasicLowerBannerAdView.setVisibility(View.GONE);
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
		return mBasicLowerBannerAdView == null || renovations3DActivity == null || SUPPRESS_ADS
				|| renovations3DActivity.getBillingManager().ownsBasicAdFree();
	}

	//used by the tutorial to not show ads until finished
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

	//used by the tutorial to not show ads until finished
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
					// is now the time to show an interstitial?
					if (points >= INTERSTITIAL_POINTS_THRESHOLD && System.currentTimeMillis() - lastShownTime > INTERSTITIAL_TIME_THRESHOLD)
					{
						// we reset either way as we still want the interstitial counts on firebase correct
						// zero points and record exact time
						SharedPreferences.Editor editor = settings.edit();
						editor.putInt(INTERSTITIAL_POINTS, 0);
						editor.putLong(INTERSTITIAL_LAST_SHOWN_TIME, System.currentTimeMillis());
						editor.apply();

						if (mFirebaseRemoteConfig.getBoolean("renovations3d_interstitial_enabled"))
						{
							if (mInterstitialAd != null)
							{
								Renovations3DActivity.logFireBaseContent("INTERSTITIAL SHOWN", null);
								mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
									@Override
									public void onAdDismissedFullScreenContent() {
										// Called when fullscreen content is dismissed.
										//Log.d("TAG", "The ad was dismissed.");
									}

									@Override
									public void onAdFailedToShowFullScreenContent(AdError adError) {
										// Called when fullscreen content failed to show.
										//Log.d("TAG", "The ad failed to show.");
									}

									@Override
									public void onAdShowedFullScreenContent() {
										// Called when fullscreen content is shown.
										// Make sure to set your reference to null so you don't
										// show it a second time.
										mInterstitialAd = null;
										//Log.d("TAG", "The ad was shown.");
									}
								});
								mInterstitialAd.show(renovations3DActivity);

								//if more than one per day and session then loadAd needs to be called here
							}
							else
							{
								Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "INTERSTITIAL NOT LOADED", null);
							}
						}
						else
						{
							Renovations3DActivity.logFireBaseContent("INTERSTITIAL WOULD HAVE BEEN SHOWN", null);
						}
					}
				}});
		}
	}
}
