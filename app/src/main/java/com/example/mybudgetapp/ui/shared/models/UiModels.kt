package com.example.mybudgetapp.ui.shared.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class ScreenItemsUiState(
    val isDropDownMenuVisible: Boolean = false,
    val itemHeight: Dp = 0.dp,
    val offSet: DpOffset = DpOffset.Zero,
)
