package com.eteks.renovations3d;

import android.view.View;
import android.view.ViewGroup;

import com.eteks.renovations3d.Renovations3DActivity;
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
	private Renovations3DActivity renovations3DActivity;
	private AdView mBasicLowerBannerAdView;

	public AdMobManager(Renovations3DActivity renovations3DActivity)
	{
		this.renovations3DActivity = renovations3DActivity;
		// set up admob ads if they don't own ad free option and it's not debug
		mBasicLowerBannerAdView = (AdView) renovations3DActivity.findViewById(R.id.lowerBannerAdView);

	}

	/** called by billing to indicate it can now service questions
	 *
	 */
	public void billingServiceConnected()
	{
		if (!renovations3DActivity.getBillingManager().ownsBasicAdFree() )//&& !BuildConfig.DEBUG)
		{
			MobileAds.initialize(renovations3DActivity.getApplicationContext(), "ca-app-pub-7177705441403385~4026888158");
			AdRequest.Builder builder = new AdRequest.Builder();
			builder.addTestDevice("56ACE73C453B9562B288E8C2075BDA73");
			AdRequest adRequest = builder.build();
			mBasicLowerBannerAdView.loadAd(adRequest);
		}
		else
		{
			// same thing should be called when purchase complete
			mBasicLowerBannerAdView.setEnabled(false);
			mBasicLowerBannerAdView.setVisibility(View.GONE);
		}
	}

	public void removeBasicLowerBannerAdView() {

		if (mBasicLowerBannerAdView != null)
		{
			ViewGroup parent = (ViewGroup) mBasicLowerBannerAdView.getParent();
			if(parent != null)
			{
				parent.removeView(mBasicLowerBannerAdView);
				parent.invalidate();
			}
		}
	}
}
