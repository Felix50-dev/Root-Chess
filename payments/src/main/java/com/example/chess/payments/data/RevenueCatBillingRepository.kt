package com.example.chess.payments.data

import android.app.Activity
import com.example.chess.payments.domain.model.PlanPeriod
import com.example.chess.payments.domain.model.PlansResult
import com.example.chess.payments.domain.model.PurchaseResult
import com.example.chess.payments.domain.model.SubscriptionPlan
import com.example.chess.payments.domain.repository.BillingRepository
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class RevenueCatBillingRepository @Inject constructor() : BillingRepository {

    // Domain code never sees a RevenueCat Package — plans carry only the package id,
    // resolved back here at purchase time.
    private val packagesById = ConcurrentHashMap<String, Package>()

    override suspend fun getPlans(): PlansResult {
        if (!Purchases.isConfigured) return PlansResult.Error(BILLING_UNAVAILABLE)
        return suspendCoroutine { cont ->
            Purchases.sharedInstance.getOfferingsWith(
                onError = { error -> cont.resume(PlansResult.Error(error.message)) },
                onSuccess = { offerings ->
                    val offering = offerings.current ?: offerings[DEFAULT_OFFERING]
                    val plans = buildList {
                        offering?.monthly?.let { add(toPlan(it, PlanPeriod.MONTHLY)) }
                        offering?.annual?.let { add(toPlan(it, PlanPeriod.YEARLY)) }
                    }
                    cont.resume(
                        if (plans.isEmpty()) PlansResult.Error("No subscription plans are available right now.")
                        else PlansResult.Success(plans)
                    )
                },
            )
        }
    }

    private fun toPlan(pkg: Package, period: PlanPeriod): SubscriptionPlan {
        packagesById[pkg.identifier] = pkg
        return SubscriptionPlan(
            id = pkg.identifier,
            period = period,
            formattedPrice = pkg.product.price.formatted,
            title = pkg.product.title,
        )
    }

    override suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult {
        if (!Purchases.isConfigured) return PurchaseResult.Error(BILLING_UNAVAILABLE)
        val pkg = packagesById[plan.id]
            ?: return PurchaseResult.Error("That plan is no longer available — please reload and try again.")
        return suspendCoroutine { cont ->
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, pkg).build(),
                onError = { error, userCancelled ->
                    cont.resume(
                        if (userCancelled) PurchaseResult.UserCancelled
                        else PurchaseResult.Error(error.message)
                    )
                },
                onSuccess = { _, _ -> cont.resume(PurchaseResult.Success) },
            )
        }
    }

    override suspend fun restore(): PurchaseResult {
        if (!Purchases.isConfigured) return PurchaseResult.Error(BILLING_UNAVAILABLE)
        return suspendCoroutine { cont ->
            Purchases.sharedInstance.restorePurchasesWith(
                onError = { error -> cont.resume(PurchaseResult.Error(error.message)) },
                onSuccess = { customerInfo ->
                    cont.resume(
                        if (customerInfo.entitlements[RevenueCatEntitlementRepository.ENTITLEMENT_PREMIUM]?.isActive == true) {
                            PurchaseResult.Success
                        } else {
                            PurchaseResult.Error("No previous purchase was found for this account.")
                        }
                    )
                },
            )
        }
    }

    private companion object {
        const val DEFAULT_OFFERING = "default"
        const val BILLING_UNAVAILABLE = "Billing is not available in this build."
    }
}
