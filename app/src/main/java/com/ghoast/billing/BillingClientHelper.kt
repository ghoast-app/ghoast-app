package com.ghoast.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class BillingResultState {
    object Success : BillingResultState()
    object Cancelled : BillingResultState()
    data class Failed(val message: String) : BillingResultState()
}

class BillingClientHelper(private val context: Context) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null

    private val _billingResultFlow = MutableSharedFlow<BillingResultState>()
    val billingResultFlow = _billingResultFlow.asSharedFlow()

    init {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
    }

    fun startConnection(onConnected: () -> Unit) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onConnected()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun launchPurchaseFlow(activity: Activity, productId: String, isSubscription: Boolean) {
        val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()
                billingClient?.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> _billingResultFlow.tryEmit(BillingResultState.Success)
            BillingClient.BillingResponseCode.USER_CANCELED -> _billingResultFlow.tryEmit(BillingResultState.Cancelled)
            else -> _billingResultFlow.tryEmit(BillingResultState.Failed(billingResult.debugMessage))
        }
    }

    fun endConnection() {
        billingClient?.endConnection()
    }
}
