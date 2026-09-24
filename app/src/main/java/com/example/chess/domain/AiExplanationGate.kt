package com.example.chess.domain

import com.example.chess.data.billing.AiReviewQuotaManager
import com.example.chess.payments.domain.repository.EntitlementRepository
import javax.inject.Inject
import javax.inject.Singleton

enum class GateResult { ALLOWED, QUOTA_EXHAUSTED }

// The single decision point for "may this review request AI move explanations?".
// Kept out of ChessGameViewModel so it is unit-testable (the VM drags in the native
// engine) and out of MoveExplainer so that repository stays pure.
@Singleton
class AiExplanationGate @Inject constructor(
    private val entitlements: EntitlementRepository,
    private val quota: AiReviewQuotaManager,
) {
    suspend fun check(gameKey: String): GateResult = when {
        // Premium is checked FIRST and never touches the quota — a subscriber's review
        // must not claim (and thereby waste) the free daily slot.
        entitlements.isPremium.value -> GateResult.ALLOWED
        quota.claimOrCheck(gameKey) -> GateResult.ALLOWED
        else -> GateResult.QUOTA_EXHAUSTED
    }
}
