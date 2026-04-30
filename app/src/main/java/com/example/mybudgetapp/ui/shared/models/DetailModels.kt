package com.example.mybudgetapp.ui.shared.models

data class DetailHeroChipUi(
    val label: String,
    val value: String,
    val isMonetary: Boolean = false,
)

enum class DetailGroupGranularity {
    DAY,
    MONTH,
}

data class DetailGroupUi<T>(
    val key: String,
    val label: String,
    val displayTotal: String,
    val totalLabel: String,
    val items: List<T>,
)
