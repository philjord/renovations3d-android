package com.eteks.renovations3d;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

/**
 * Created by phil on 5/15/2017.
 */

public class AdMobManager
{
	private static final boolean SUPPRESS_ADS = false;//for debug and screenshots overrides force_ads
	private static final boolean FORCE_ADS = true;//for debug build to still have ads <= PREFER this one! Less dangerous
	private Renovations3DActivity renovations3DActivity;
	private AdView mBasicLowerBannerAdView;

	public AdMobManager(Renovations3DActivity renovations3DActivity)
	{
		this.renovations3DActivity = renovations3DActivity;
		mBasicLowerBannerAdView = (AdView) renovations3DActivity.findViewById(R.id.lowerBannerAdView);
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
				AdRequest.Builder builder = new AdRequest.Builder();
				//example logcat Use AdRequest.Builder.addTestDevice("4A1B3B44655FDE15F64CFD90EFD60699") to get test ads on this device.
				builder.addTestDevice("56ACE73C453B9562B288E8C2075BDA73");//T580
				builder.addTestDevice("4A1B3B44655FDE15F64CFD90EFD60699");//I9505
				builder.addTestDevice("F1F03BC6248C8ECF32CBB4DD027F78B9");//T210
				AdRequest adRequest = builder.build();
				mBasicLowerBannerAdView.loadAd(adRequest);
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
		return mBasicLowerBannerAdView == null || renovations3DActivity == null || SUPPRESS_ADS || renovations3DActivity.getBillingManager().ownsBasicAdFree() || (BuildConfig.DEBUG && !FORCE_ADS);
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
}
