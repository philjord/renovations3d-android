package com.eteks.renovations3d;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 5/15/2017.
 */

public class BillingManager implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private static final String BASIC_AD_FREE_SKU = "basic_ad_free";
    private Renovations3DActivity renovations3DActivity;
    private SkuDetails basicAdFreeSKU = null;
    private Boolean cachedOwnsBasicAdFree = new Boolean(false);

    public BillingManager(Renovations3DActivity renovations3DActivity) {
        this.renovations3DActivity = renovations3DActivity;
    }

    public void initialize() {
        billingClient = BillingClient.newBuilder(renovations3DActivity)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    setupSKus();
                    cacheOwnerShip();
                    renovations3DActivity.getAdMobManager().billingServiceConnected();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        if (purchase.getSku().equals(BASIC_AD_FREE_SKU)) {
                            cachedOwnsBasicAdFree = new Boolean(true);
                            // the menu should update on the next prepare
                            renovations3DActivity.getAdMobManager().removeBasicLowerBannerAdView();
                            Renovations3DActivity.logFireBase(FirebaseAnalytics.Event.PURCHASE, "Purchase " + BASIC_AD_FREE_SKU, null);
                        } else {
                            // deal with other types of purchase here now
                        }
                    }
                });
            }
        }

    }


    private void setupSKus() {
        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add(BASIC_AD_FREE_SKU);

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        if (skuDetailsList != null) {
                            for (SkuDetails skuDetail : skuDetailsList) {
                                if (skuDetail.getSku().equals(BASIC_AD_FREE_SKU)) {
                                    basicAdFreeSKU = skuDetail;
                                }
                            }
                        }
                    }
                });
    }

    public synchronized boolean ownsBasicAdFree() {
        return cachedOwnsBasicAdFree.booleanValue();
    }

    private void cacheOwnerShip() {
        Purchase.PurchasesResult ownedItems = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchaseDataList = ownedItems.getPurchasesList();
        if (purchaseDataList != null) {
            for (Purchase ownedPurchase : purchaseDataList) {
                if (ownedPurchase.getSku().equals(BASIC_AD_FREE_SKU)) {
                    cachedOwnsBasicAdFree = new Boolean(true);
                }
            }
        }
    }

    public synchronized void buyBasicAdFree() {
        if (basicAdFreeSKU != null) {
            BillingFlowParams purchaseParams =
                    BillingFlowParams.newBuilder()
                            .setSkuDetails(basicAdFreeSKU)
                            .build();

            billingClient.launchBillingFlow(renovations3DActivity, purchaseParams);
            // Purchase is handled in onPurchasesUpdated
        }
    }


    public void onDestroy() {
        // I don't think it's worth bothering to disconnect billing
    }
}
