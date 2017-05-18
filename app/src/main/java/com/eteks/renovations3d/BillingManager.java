package com.eteks.renovations3d;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.eteks.renovations3d.billing.util.IabHelper.BILLING_RESPONSE_RESULT_OK;

/**
 * Created by phil on 5/15/2017.
 */

public class BillingManager
{
	private static final String BASIC_AD_FREE_SKU = "basic_ad_free";
	private static final String IN_APP = "inapp";
	private static final int PURCHASE_REQUEST_CODE = 3010;

	private Renovations3DActivity renovations3DActivity;
	private IInAppBillingService mService;

	private String basicAdFreePrice = "";
	private Boolean cachedOwnsBasicAdFree = new Boolean(false);

	private ServiceConnection mServiceConn = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mService = IInAppBillingService.Stub.asInterface(service);
			billingServiceConnected();
		}
	};

	public BillingManager(Renovations3DActivity renovations3DActivity)
	{
		this.renovations3DActivity = renovations3DActivity;
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		renovations3DActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

	}

	private void billingServiceConnected()
	{
		setupSKus();
		cacheOwnerShip();
		renovations3DActivity.getAdMobManager().billingServiceConnected();
	}

	private void setupSKus()
	{
		if(mService !=null )
		{
			ArrayList<String> skuList = new ArrayList<String>();
			skuList.add(BASIC_AD_FREE_SKU);
			Bundle querySkus = new Bundle();
			querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

			try
			{
				Bundle skuDetails = mService.getSkuDetails(3, renovations3DActivity.getPackageName(), IN_APP, querySkus);

				int response = skuDetails.getInt("RESPONSE_CODE");
				if (response == BILLING_RESPONSE_RESULT_OK)
				{
					ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
					for (String thisResponse : responseList)
					{
						JSONObject object = new JSONObject(thisResponse);
						String sku = object.getString("productId");
						String price = object.getString("price");
						if (sku.equals(BASIC_AD_FREE_SKU))
						{
							// display it on the menu?
							basicAdFreePrice = price;
						}
					}
				}
				else
				{
					//I should probably care about these
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "setupSKus - response != BILLING_RESPONSE_RESULT_OK response: " + response, null);
				}
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "setupSKus - RemoteException", null);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "setupSKus - JSONException", null);
			}
		}
		else
		{
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "setupSKus - no service", null);
		}
	}

	public String priceBasicAdFree()
	{
		return this.basicAdFreePrice;
	}

	public synchronized boolean ownsBasicAdFree()
	{
		return cachedOwnsBasicAdFree.booleanValue();
	}

	private void cacheOwnerShip()
	{
		if(mService != null)
		{
			try
			{
				Bundle ownedItems = mService.getPurchases(3, renovations3DActivity.getPackageName(), IN_APP, null);

				int response = ownedItems.getInt("RESPONSE_CODE");
				if (response == 0)
				{
					ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
					String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

					for (int i = 0; i < purchaseDataList.size(); ++i)
					{
						String purchaseData = purchaseDataList.get(i);
						String signature = signatureList.get(i);// I should use my public key to verify this some how
						String sku = ownedSkus.get(i);

						// do something with this purchase information
						if (sku.equals(BASIC_AD_FREE_SKU))
						{
							cachedOwnsBasicAdFree = new Boolean(true);
						}
						//TODO: other purchases
					}

					// if continuationToken != null, call getPurchases again
					// and pass in the token to retrieve more items
				}
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "cacheOwnerShip - RemoteException", null);
				Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "cacheOwnerShip - no service", null);
			Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
		}
	}

	public synchronized void buyBasicAdFree()
	{
		if(mService != null)
		{
			try
			{
				String devPayload = "";// might be used to get a generated id from my own servers
				Bundle buyIntentBundle = mService.getBuyIntent(3, renovations3DActivity.getPackageName(), BASIC_AD_FREE_SKU, IN_APP, devPayload);

				PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

				if(pendingIntent != null)
				{
					renovations3DActivity.startIntentSenderForResult(pendingIntent.getIntentSender(),
							PURCHASE_REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
							Integer.valueOf(0));

					// and wait for the onActivityResult of the activity to get back below
				}
				else
				{
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "buyBasicAdFree - null pendingIntent", null);
					Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
				}

			}
			catch (RemoteException e)
			{
				e.printStackTrace();
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "buyBasicAdFree - RemoteException", null);
				Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
			}
			catch (IntentSender.SendIntentException e)
			{
				e.printStackTrace();
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "buyBasicAdFree - IntentSender.SendIntentException", null);
				Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "buyBasicAdFree - no service", null);
			Toast.makeText(renovations3DActivity, "Unable to connect to store", Toast.LENGTH_LONG).show();
		}
	}

	public void onDestroy()
	{
		if (mService != null)
		{
			renovations3DActivity.unbindService(mServiceConn);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == PURCHASE_REQUEST_CODE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				/*purchaseData
				'{
					"orderId":"GPA.1234-5678-9012-34567",
						"packageName":"com.example.app",
						"productId":"exampleSku",
						"purchaseTime":1345678900000,
						"purchaseState":0,
						"developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
						"purchaseToken":"opaque-token-up-to-1000-characters"
					}'*/

				int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");


				try
				{
					JSONObject jo = new JSONObject(purchaseData);
					String sku = jo.getString("productId");

					if (sku.equals(BASIC_AD_FREE_SKU))
					{
						//	alert("You have bought the " + sku + ". Excellent choice, adventurer!");
						//TODO: very much some sort of thank you big time, this will help dev a lot etc

						cachedOwnsBasicAdFree = new Boolean(true);
						// the menu should update on the next prepare
						renovations3DActivity.getAdMobManager().removeBasicLowerBannerAdView();
						Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, "Purchase " + BASIC_AD_FREE_SKU, null);
					}
					else
					{
						// deal with other types of purchase here now
					}
				}
				catch (JSONException e)
				{
					//	alert("Failed to parse purchase data.");
					e.printStackTrace();
					Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onActivityResult - JSONException", null);
				}
			}
			else
			{
				// this should be Activity.RESULT_CANCELED
				Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.POST_SCORE, "onActivityResult - resultCode " + resultCode, null);
			}

		}
	}
}
