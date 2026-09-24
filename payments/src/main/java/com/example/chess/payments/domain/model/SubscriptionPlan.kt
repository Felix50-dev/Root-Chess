package com.example.chess.payments.domain.model

enum class PlanPeriod { MONTHLY, YEARLY }

// Provider-agnostic view of a purchasable subscription. The data layer maps its own
// SDK types into this; nothing outside payments/data may see a billing-SDK type.
data class SubscriptionPlan(
    val id: String,             // stable identifier the data layer can resolve back to a store product
    val period: PlanPeriod,
    val formattedPrice: String, // localized, e.g. "$4.99"
    val title: String,
)
