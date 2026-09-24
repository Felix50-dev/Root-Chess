package com.example.chess.payments.domain.model

sealed interface PurchaseResult {
    data object Success : PurchaseResult

    // Distinct from Error so the UI can stay silent — a user backing out of the
    // store sheet is not a failure to report.
    data object UserCancelled : PurchaseResult

    data class Error(val message: String) : PurchaseResult
}

sealed interface PlansResult {
    data class Success(val plans: List<SubscriptionPlan>) : PlansResult
    data class Error(val message: String) : PlansResult
}
