package com.example.chess.payments.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chess.payments.domain.model.PlanPeriod
import com.example.chess.payments.domain.model.SubscriptionPlan

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    // Invoked once the premium entitlement becomes active. Pass a no-op when the screen
    // is hosted inline (e.g. a gated route that recomposes into the real content itself).
    onPurchased: () -> Unit,
    accent: Color = Color(0xFFA26232),
    background: Color = Color(0xFFF5EFE6),
    termsUrl: String? = null,
    privacyUrl: String? = null,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.isPremium) {
        if (state.isPremium) onPurchased()
    }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text("Go Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = background,
                    titleContentColor = accent,
                    navigationIconContentColor = accent,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("♛", fontSize = 56.sp, color = accent)
            Spacer(Modifier.height(8.dp))
            Text(
                "Train with your own AI coach",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))

            BenefitRow(accent, "Unlimited AI move explanations for every game you review")
            BenefitRow(accent, "AI Coach chat — ask anything about your games")
            BenefitRow(accent, "Support ongoing development")

            Spacer(Modifier.height(24.dp))

            when {
                state.loading -> CircularProgressIndicator(color = accent)
                state.plans.isEmpty() -> {
                    Text(
                        state.error ?: "No plans available right now.",
                        color = Color(0xFF7A5C43),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = viewModel::loadPlans) {
                        Text("Retry", color = accent, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    state.plans.forEach { plan ->
                        PlanCard(
                            plan = plan,
                            selected = plan.id == state.selectedPlanId,
                            accent = accent,
                            onClick = { viewModel.selectPlan(plan.id) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    state.error?.let {
                        Text(it, color = Color(0xFFB3392E), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { activity?.let(viewModel::purchaseSelected) },
                        enabled = !state.purchaseInProgress && state.selectedPlanId != null,
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        if (state.purchaseInProgress) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp),
                            )
                        } else {
                            Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick = viewModel::restore,
                        enabled = !state.purchaseInProgress,
                    ) {
                        Text("Restore purchases", color = accent)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                termsUrl?.let {
                    Text(
                        "Terms",
                        color = Color(0xFF7A5C43),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { uriHandler.openUri(it) },
                    )
                }
                privacyUrl?.let {
                    Text(
                        "Privacy policy",
                        color = Color(0xFF7A5C43),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { uriHandler.openUri(it) },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BenefitRow(accent: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("✓", color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.size(12.dp))
        Text(text, color = Color(0xFF4A3728))
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val periodLabel = when (plan.period) {
        PlanPeriod.MONTHLY -> "per month"
        PlanPeriod.YEARLY -> "per year"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else Color(0xFFD8C7B4),
                shape = RoundedCornerShape(14.dp),
            )
            .background(
                color = if (selected) accent.copy(alpha = 0.10f) else Color.White,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                when (plan.period) {
                    PlanPeriod.MONTHLY -> "Monthly"
                    PlanPeriod.YEARLY -> "Yearly"
                },
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3728),
            )
            if (plan.period == PlanPeriod.YEARLY) {
                Text("Best value", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "${plan.formattedPrice} $periodLabel",
            color = Color(0xFF4A3728),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
