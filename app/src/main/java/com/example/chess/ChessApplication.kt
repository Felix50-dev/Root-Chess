package com.example.chess

import android.app.Application
import com.example.chess.payments.data.PaymentsConfig
import com.example.chess.payments.data.PaymentsInitializer
import com.example.chess.payments.domain.repository.EntitlementRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
@HiltAndroidApp
class ChessApplication : Application() {

    @Inject
    lateinit var entitlementRepository: EntitlementRepository

    override fun onCreate() {
        super.onCreate()
        PaymentsInitializer.configure(
            this,
            PaymentsConfig(
                revenueCatApiKey = BuildConfig.REVENUECAT_API_KEY,
                // BuildConfig.DEBUG gate means a stale SIMULATE_PREMIUM=true in
                // local.properties can never unlock a release build.
                simulatePremium = BuildConfig.DEBUG && BuildConfig.SIMULATE_PREMIUM,
            ),
        )
        entitlementRepository.refresh()
    }
}
