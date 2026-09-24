package com.example.chess

import com.example.chess.data.billing.AiReviewQuotaManager
import com.example.chess.domain.AiExplanationGate
import com.example.chess.domain.GateResult
import com.example.chess.payments.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

private class FakeEntitlements(premium: Boolean) : EntitlementRepository {
    override val isPremium: StateFlow<Boolean> = MutableStateFlow(premium)
    override fun refresh() {}
}

class AiExplanationGateTest {

    private val quota: AiReviewQuotaManager = Mockito.mock(AiReviewQuotaManager::class.java)

    @Test
    fun premium_isAllowed_withoutConsultingQuota() = runBlocking<Unit> {
        val gate = AiExplanationGate(FakeEntitlements(premium = true), quota)

        assertEquals(GateResult.ALLOWED, gate.check("gameA"))

        // A subscriber's review must never claim (waste) the free daily slot.
        Mockito.verify(quota, Mockito.never()).claimOrCheck(anyString())
    }

    @Test
    fun freeUser_withQuotaAvailable_isAllowed() = runBlocking<Unit> {
        Mockito.`when`(quota.claimOrCheck(anyString())).thenReturn(true)
        val gate = AiExplanationGate(FakeEntitlements(premium = false), quota)

        assertEquals(GateResult.ALLOWED, gate.check("gameA"))
    }

    @Test
    fun freeUser_withQuotaExhausted_isBlocked() = runBlocking<Unit> {
        Mockito.`when`(quota.claimOrCheck(anyString())).thenReturn(false)
        val gate = AiExplanationGate(FakeEntitlements(premium = false), quota)

        assertEquals(GateResult.QUOTA_EXHAUSTED, gate.check("gameA"))
    }
}
