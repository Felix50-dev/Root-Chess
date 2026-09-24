package com.example.chess.payments.domain.repository

import android.app.Activity
import com.example.chess.payments.domain.model.PlansResult
import com.example.chess.payments.domain.model.PurchaseResult
import com.example.chess.payments.domain.model.SubscriptionPlan

interface BillingRepository {
    suspend fun getPlans(): PlansResult

    // Activity is required by every store billing flow — it is the one Android type
    // the domain layer cannot abstract away.
    suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult

    suspend fun restore(): PurchaseResult
}
