package com.example.chess.payments

import android.app.Activity
import com.example.chess.payments.domain.model.PlanPeriod
import com.example.chess.payments.domain.model.PlansResult
import com.example.chess.payments.domain.model.PurchaseResult
import com.example.chess.payments.domain.model.SubscriptionPlan
import com.example.chess.payments.domain.repository.BillingRepository
import com.example.chess.payments.domain.repository.EntitlementRepository
import com.example.chess.payments.domain.usecase.GetSubscriptionPlans
import com.example.chess.payments.domain.usecase.ObservePremiumStatus
import com.example.chess.payments.domain.usecase.PurchasePlan
import com.example.chess.payments.domain.usecase.RestorePurchases
import com.example.chess.payments.presentation.PaywallViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

private val MONTHLY = SubscriptionPlan("m", PlanPeriod.MONTHLY, "$4.99", "Premium Monthly")
private val YEARLY = SubscriptionPlan("y", PlanPeriod.YEARLY, "$29.99", "Premium Yearly")

private class FakeBillingRepository : BillingRepository {
    var plansResult: PlansResult = PlansResult.Success(listOf(MONTHLY, YEARLY))
    var purchaseResult: PurchaseResult = PurchaseResult.Success
    override suspend fun getPlans(): PlansResult = plansResult
    override suspend fun purchase(activity: Activity, plan: SubscriptionPlan): PurchaseResult = purchaseResult
    override suspend fun restore(): PurchaseResult = purchaseResult
}

private class FakeEntitlementRepository : EntitlementRepository {
    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium
    var premiumOnRefresh = false
    override fun refresh() {
        if (premiumOnRefresh) _isPremium.value = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private lateinit var billing: FakeBillingRepository
    private lateinit var entitlements: FakeEntitlementRepository
    private val activity: Activity = Mockito.mock(Activity::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        billing = FakeBillingRepository()
        entitlements = FakeEntitlementRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PaywallViewModel(
        observePremiumStatus = ObservePremiumStatus(entitlements),
        getSubscriptionPlans = GetSubscriptionPlans(billing),
        purchasePlan = PurchasePlan(billing, entitlements),
        restorePurchases = RestorePurchases(billing, entitlements),
    )

    @Test
    fun plansLoadOnInit_andYearlyIsPreselected() {
        val vm = viewModel()
        val state = vm.state.value
        assertFalse(state.loading)
        assertEquals(listOf(MONTHLY, YEARLY), state.plans)
        assertEquals(YEARLY.id, state.selectedPlanId)
        assertNull(state.error)
    }

    @Test
    fun plansLoadError_exposesErrorAndRetryRecovers() {
        billing.plansResult = PlansResult.Error("offline")
        val vm = viewModel()
        assertEquals("offline", vm.state.value.error)
        assertTrue(vm.state.value.plans.isEmpty())

        billing.plansResult = PlansResult.Success(listOf(MONTHLY))
        vm.loadPlans()
        assertNull(vm.state.value.error)
        assertEquals(listOf(MONTHLY), vm.state.value.plans)
    }

    @Test
    fun purchaseSuccess_refreshesEntitlement_andPremiumFlips() {
        entitlements.premiumOnRefresh = true
        val vm = viewModel()
        vm.purchaseSelected(activity)
        val state = vm.state.value
        assertTrue(state.isPremium)
        assertNull(state.error)
        assertFalse(state.purchaseInProgress)
    }

    @Test
    fun userCancelled_isSilent_noErrorShown() {
        billing.purchaseResult = PurchaseResult.UserCancelled
        val vm = viewModel()
        vm.purchaseSelected(activity)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isPremium)
    }

    @Test
    fun purchaseError_isShown() {
        billing.purchaseResult = PurchaseResult.Error("card declined")
        val vm = viewModel()
        vm.purchaseSelected(activity)
        assertEquals("card declined", vm.state.value.error)
        assertFalse(vm.state.value.isPremium)
    }

    @Test
    fun restoreSuccess_flipsPremium() {
        entitlements.premiumOnRefresh = true
        val vm = viewModel()
        vm.restore()
        assertTrue(vm.state.value.isPremium)
        assertNull(vm.state.value.error)
    }

    @Test
    fun premiumBypass_selectionStateStillSane() {
        val vm = viewModel()
        vm.selectPlan(MONTHLY.id)
        assertEquals(MONTHLY.id, vm.state.value.selectedPlanId)
        assertNotNull(vm.state.value.plans.find { it.id == MONTHLY.id })
    }
}
