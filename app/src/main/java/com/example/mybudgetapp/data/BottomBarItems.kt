package com.example.mybudgetapp.data

import androidx.annotation.DrawableRes
import com.example.mybudgetapp.R

data class BottomNavigationItem(
    val title: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unSelectedIcon: Int,
    val hasNews: Boolean,
)

object NavigationItems {
    val items = listOf(
        BottomNavigationItem(
            title = "Year",
            selectedIcon = R.drawable.baseline_view_timeline_24,
            unSelectedIcon = R.drawable.outline_view_timeline_24,
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Month",
            selectedIcon = R.drawable.baseline_calendar_month_24,
            unSelectedIcon = R.drawable.outline_calendar_month_24,
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Vault",
            selectedIcon = R.drawable.baseline_settings_24,
            unSelectedIcon = R.drawable.outline_settings_24,
            hasNews = false,
        ),
    )
}
