package com.quantumquontity.chatgpt.billing;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.quantumquontity.chatgpt.MainActivity;
import com.quantumquontity.chatgpt.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingService {

    private static final String ONE_MONTH_SUBSCRIPTION = "subscription_1_month";
    private static final String THREE_MONTH_SUBSCRIPTION = "sibscription_3_months";
    private static final String TWELVE_MONTH_SUBSCRIPTION = "subscription_12_months";
    public static final String BILLING_SERVICE = "billingService";
    private final BillingClient billingClient;

    private MainActivity mainActivity;

    private List<String> payedProducts = new ArrayList<>();

    private Map<String, ProductDetails> productDetailsMap = new HashMap<>();

    private PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        // To be implemented in a later section.
        //сюда мы попадем когда будет осуществлена покупка
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                payedProducts.addAll(purchase.getProducts());
//                    handlePurchase(purchase);
            }
            if(isPremium()){
                mainActivity.runOnUiThread(() -> mainActivity.onExistPremium());
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    };


    private boolean isBillingClientReady = false;

    public BillingService(MainActivity mainActivity, Runnable onExistPremium) {
        this.mainActivity = mainActivity;
        billingClient = BillingClient.newBuilder(mainActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        if (!isBillingClientReady) {
            startConnection(onExistPremium);
        }
    }

    private void startConnection(Runnable onExistPremium) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    updateInfoFromStore(onExistPremium);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                System.out.println("Error");
            }
        });
    }

    private void updateInfoFromStore(Runnable onExistPremium) {
        QueryProductDetailsParams queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                        .setProductList(
                                Arrays.asList(
                                        buildQueryProduct(ONE_MONTH_SUBSCRIPTION),
                                        buildQueryProduct(THREE_MONTH_SUBSCRIPTION),
                                        buildQueryProduct(TWELVE_MONTH_SUBSCRIPTION)
                                )
                        )
                        .build();

        // Получение данных о доступных подписках
        billingClient.queryProductDetailsAsync(
                queryProductDetailsParams,
                (billingResult1, productDetailsList) -> {
                    productDetailsMap.clear();
                    for (ProductDetails productDetails : productDetailsList) {
                        productDetailsMap.put(productDetails.getProductId(), productDetails);
                    }
                }
        );

        // получение данных о купленных подписках
        queryPurchasesAsync(onExistPremium);
    }

    private QueryProductDetailsParams.Product buildQueryProduct(String productId){
        return QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build();
    }

    private void queryPurchasesAsync(Runnable onExistPremium) {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, purchases) -> {
                    payedProducts.clear();
                    for (Object purchaseObj : purchases) {
                        Purchase purchase = (Purchase) purchaseObj;
                        if (purchase.getProducts() != null) {
                            payedProducts.addAll(purchase.getProducts());
                        }
                    }
                    if(isPremium()){
                        mainActivity.runOnUiThread(onExistPremium);
                    }
                    // check billingResult
                    // process returned purchase list, e.g. display the plans user owns

                }
        );
    }

    public String get1MonthSubscriptionCost() {
        return getSubscriptionCost(ONE_MONTH_SUBSCRIPTION);
    }

    public String get3MonthsSubscriptionCost() {
        return getSubscriptionCost(THREE_MONTH_SUBSCRIPTION);
    }

    public String get12MonthsSubscriptionCost() {
        return getSubscriptionCost(TWELVE_MONTH_SUBSCRIPTION);
    }

    public String getSubscriptionCost(String subscriptionCode) {
        ProductDetails productDetails = productDetailsMap.get(subscriptionCode);
        if(productDetails != null){
            List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
            if(subscriptionOfferDetails != null && !subscriptionOfferDetails.isEmpty()){
                return subscriptionOfferDetails.get(0)
                        .getPricingPhases().getPricingPhaseList().get(0)
                        .getFormattedPrice();
            }
        }
        return mainActivity.getText(R.string.cost_unknown).toString();
    }

    public void buy1MonthSubscription() {
        buySubscription(ONE_MONTH_SUBSCRIPTION);
    }


    public void buy3MonthsSubscription() {
        buySubscription(THREE_MONTH_SUBSCRIPTION);
    }

    public void buy12MonthsSubscription() {
        buySubscription(TWELVE_MONTH_SUBSCRIPTION);
    }

    private BillingResult buySubscription(String subscriptionCode) {
        ProductDetails productDetails = productDetailsMap.get(subscriptionCode);
        if (productDetails != null) {
            BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                    .build();


            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(Arrays.asList(productDetailsParams))
                    .build();

            return billingClient.launchBillingFlow(mainActivity, billingFlowParams);
        }

        return null;
    }

    public void onDestroy() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            isBillingClientReady = false;
        }
    }

    public boolean isPremium() {
        return !payedProducts.isEmpty();
    }
}
