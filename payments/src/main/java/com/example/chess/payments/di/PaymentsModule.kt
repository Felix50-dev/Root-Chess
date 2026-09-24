package com.example.chess.payments.di

import com.example.chess.payments.data.RevenueCatBillingRepository
import com.example.chess.payments.data.RevenueCatEntitlementRepository
import com.example.chess.payments.domain.repository.BillingRepository
import com.example.chess.payments.domain.repository.EntitlementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PaymentsModule {

    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(impl: RevenueCatEntitlementRepository): EntitlementRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: RevenueCatBillingRepository): BillingRepository
}
