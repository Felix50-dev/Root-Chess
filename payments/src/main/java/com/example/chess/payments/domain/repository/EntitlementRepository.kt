package com.example.chess.payments.domain.repository

import kotlinx.coroutines.flow.StateFlow

// The one seam the app gates features on. Implementations must never emit spuriously
// false after a purchase — push provider updates into the flow as they arrive.
interface EntitlementRepository {
    val isPremium: StateFlow<Boolean>

    // Re-query the provider (app start, resume, after purchase/restore). Must be safe
    // to call when billing is unavailable (unconfigured SDK, no network) — keep the
    // last known value in that case.
    fun refresh()
}
