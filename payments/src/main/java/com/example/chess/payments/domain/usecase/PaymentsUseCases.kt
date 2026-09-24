package com.example.chess.payments.domain.usecase

import android.app.Activity
import com.example.chess.payments.domain.model.PlansResult
import com.example.chess.payments.domain.model.PurchaseResult
import com.example.chess.payments.domain.model.SubscriptionPlan
import com.example.chess.payments.domain.repository.BillingRepository
import com.example.chess.payments.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObservePremiumStatus @Inject constructor(
    private val entitlements: EntitlementRepository,
) {
    operator fun invoke(): StateFlow<Boolean> = entitlements.isPremium
}

class GetSubscriptionPlans @Inject constructor(
    private val billing: BillingRepository,
) {
    suspend operator fun invoke(): PlansResult = billing.getPlans()
}

class PurchasePlan @Inject constructor(
    private val billing: BillingRepository,
    private val entitlements: EntitlementRepository,
) {
    suspend operator fun invoke(activity: Activity, plan: SubscriptionPlan): PurchaseResult =
        billing.purchase(activity, plan).also {
            // Provider listeners usually push the new entitlement anyway; refreshing here
            // makes success visible even if that push is delayed or dropped.
            if (it is PurchaseResult.Success) entitlements.refresh()
        }
}

class RestorePurchases @Inject constructor(
    private val billing: BillingRepository,
    private val entitlements: EntitlementRepository,
) {
    suspend operator fun invoke(): PurchaseResult =
        billing.restore().also {
            if (it is PurchaseResult.Success) entitlements.refresh()
        }
}
