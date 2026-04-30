package com.example.mybudgetapp.ui.shared.models

import androidx.compose.ui.graphics.Color
import com.example.mybudgetapp.ui.shared.widgets.BudgetValueTone

data class DashboardQuickStat(
    val label: String,
    val value: String,
    val note: String,
    val highlight: Color,
    val valueTone: BudgetValueTone? = null,
    val noteTone: BudgetValueTone? = null,
    val valueUnitLabel: String? = null,
    val noteUnitLabel: String? = null,
)

data class DashboardLaneUi(
    val label: String,
    val amount: String,
    val progress: Float,
    val accent: Color,
    val iconKey: String,
    val categoryKey: String,
    val onClick: () -> Unit,
)

data class DashboardCardAction(
    val label: String,
    val onClick: () -> Unit,
)

data class HomeTransactionPreview(
    val title: String,
    val categoryKey: String,
    val category: String,
    val categoryColorHex: String = "",
    val amount: String,
    val date: String,
)

