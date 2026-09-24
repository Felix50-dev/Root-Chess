package com.example.chess.payments.data

import com.example.chess.payments.domain.repository.EntitlementRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueCatEntitlementRepository @Inject constructor() : EntitlementRepository {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    // Push channel: RevenueCat calls this after purchases/restores/renewals, so the
    // flow flips without polling the moment a paywall purchase completes.
    private val listener = UpdatedCustomerInfoListener { customerInfo -> apply(customerInfo) }

    override fun refresh() {
        if (PaymentsInitializer.simulatePremium) {
            _isPremium.value = true
            return
        }
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.updatedCustomerInfoListener = listener
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { /* offline/unavailable: keep the last known value */ },
            onSuccess = { apply(it) },
        )
    }

    private fun apply(customerInfo: CustomerInfo) {
        _isPremium.value = customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true
    }

    companion object {
        // Must match the entitlement identifier configured in the RevenueCat dashboard.
        const val ENTITLEMENT_PREMIUM = "premium"
    }
}
