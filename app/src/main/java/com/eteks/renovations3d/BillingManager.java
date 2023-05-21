package com.eteks.renovations3d;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsParams.Product;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mindblowing.renovations3d.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 5/15/2017.
 */

public class BillingManager implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private static final String BASIC_AD_FREE_SKU = "basic_ad_free";
    private Renovations3DActivity renovations3DActivity;
    private ProductDetails basicAdFreeSKU = null;
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
                //TODO: something here, probably a toast
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
                        if (purchase.getProducts().contains(BASIC_AD_FREE_SKU)) {
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
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                ImmutableList.of(
                                        Product.newBuilder()
                                                .setProductId(BASIC_AD_FREE_SKU)
                                                .setProductType(BillingClient.ProductType.INAPP)
                                                .build()))
                        .build();



        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                new ProductDetailsResponseListener() {
                    public void onProductDetailsResponse(BillingResult billingResult,
                                                         List<ProductDetails> productDetailsList) {
                        if (productDetailsList != null) {
                            for (ProductDetails productDetail : productDetailsList ) {
                                if (productDetail.getProductId().equals(BASIC_AD_FREE_SKU)) {
                                    basicAdFreeSKU = productDetail;
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
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(
                            BillingResult billingResult,
                            List<Purchase> purchases) {
                        // Process the result, forced ads mean we ignore purchases
                        if (purchases != null && !(BuildConfig.DEBUG && AdMobManager.FORCE_DEBUG_ADS)) {
                            for (Purchase ownedPurchases : purchases) {
                                for (String ownedPurchase : ownedPurchases.getProducts()) {
                                    if (ownedPurchase.equals(BASIC_AD_FREE_SKU)) {
                                        cachedOwnsBasicAdFree = new Boolean(true);
                                        // timing for this thread changed and the answer now arrives after AdMobManager is ready to ask,
                                        // so we call back to it now (as well)
                                        renovations3DActivity.getAdMobManager().removeBasicLowerBannerAdView();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    public synchronized void buyBasicAdFree() {
        if (basicAdFreeSKU != null) {
            // Purchase is handled in onPurchasesUpdated
            // Retrieve a value for "productDetails" by calling queryProductDetailsAsync()
            // Set the parameters for the offer that will be presented
            // in the billing flow creating separate productDetailsParamsList variable
            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    ImmutableList.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(basicAdFreeSKU)
                                    .build()
                    );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();

            // Launch the billing flow
            BillingResult billingResult = billingClient.launchBillingFlow(renovations3DActivity, billingFlowParams);

        }
    }


    public void onDestroy() {
        // I don't think it's worth bothering to disconnect billing
    }
}
