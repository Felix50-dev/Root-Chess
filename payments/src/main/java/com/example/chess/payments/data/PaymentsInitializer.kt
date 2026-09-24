package com.example.chess.payments.data

import android.content.Context
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

data class PaymentsConfig(
    val revenueCatApiKey: String,
    // Forces isPremium=true without any store setup. The APP is responsible for only
    // ever passing true on debug builds — this module trusts the flag as given.
    val simulatePremium: Boolean = false,
)

object PaymentsInitializer {

    @Volatile
    internal var simulatePremium: Boolean = false
        private set

    // Call once from Application.onCreate. A blank API key (developer machine / CI
    // without RevenueCat set up) skips SDK configuration entirely; every repository
    // guards on Purchases.isConfigured and degrades gracefully.
    fun configure(context: Context, config: PaymentsConfig) {
        simulatePremium = config.simulatePremium
        if (config.revenueCatApiKey.isBlank()) return
        Purchases.logLevel = LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(context, config.revenueCatApiKey).build()
        )
    }
}
