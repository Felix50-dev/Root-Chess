package com.example.chess.payments.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.payments.domain.model.PlanPeriod
import com.example.chess.payments.domain.model.PlansResult
import com.example.chess.payments.domain.model.PurchaseResult
import com.example.chess.payments.domain.model.SubscriptionPlan
import com.example.chess.payments.domain.usecase.GetSubscriptionPlans
import com.example.chess.payments.domain.usecase.ObservePremiumStatus
import com.example.chess.payments.domain.usecase.PurchasePlan
import com.example.chess.payments.domain.usecase.RestorePurchases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val loading: Boolean = true,
    val purchaseInProgress: Boolean = false,
    val error: String? = null,
    val isPremium: Boolean = false,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    observePremiumStatus: ObservePremiumStatus,
    private val getSubscriptionPlans: GetSubscriptionPlans,
    private val purchasePlan: PurchasePlan,
    private val restorePurchases: RestorePurchases,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallUiState())
    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observePremiumStatus().collect { premium ->
                _state.update { it.copy(isPremium = premium) }
            }
        }
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = getSubscriptionPlans()) {
                is PlansResult.Success -> _state.update { current ->
                    current.copy(
                        loading = false,
                        plans = result.plans,
                        // Default-select yearly (the better deal) unless the user already chose.
                        selectedPlanId = current.selectedPlanId
                            ?: result.plans.firstOrNull { it.period == PlanPeriod.YEARLY }?.id
                            ?: result.plans.firstOrNull()?.id,
                    )
                }
                is PlansResult.Error -> _state.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun selectPlan(planId: String) {
        _state.update { it.copy(selectedPlanId = planId) }
    }

    fun purchaseSelected(activity: Activity) {
        val current = _state.value
        if (current.purchaseInProgress) return
        val plan = current.plans.find { it.id == current.selectedPlanId } ?: return
        viewModelScope.launch {
            _state.update { it.copy(purchaseInProgress = true, error = null) }
            val result = purchasePlan(activity, plan)
            _state.update {
                it.copy(
                    purchaseInProgress = false,
                    // UserCancelled stays silent — backing out of the store sheet is not an error.
                    error = (result as? PurchaseResult.Error)?.message,
                )
            }
        }
    }

    fun restore() {
        if (_state.value.purchaseInProgress) return
        viewModelScope.launch {
            _state.update { it.copy(purchaseInProgress = true, error = null) }
            val result = restorePurchases()
            _state.update {
                it.copy(
                    purchaseInProgress = false,
                    error = (result as? PurchaseResult.Error)?.message,
                )
            }
        }
    }
}
