package com.example.mybudgetapp.ui.widgets

import android.graphics.Color as AndroidColor
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.example.mybudgetapp.R
import com.example.mybudgetapp.database.CATEGORY_KEY_FOOD
import com.example.mybudgetapp.database.CATEGORY_KEY_INCOME
import com.example.mybudgetapp.database.CATEGORY_KEY_OTHERS
import com.example.mybudgetapp.database.CATEGORY_KEY_TRANSPORTATION
import com.example.mybudgetapp.ui.theme.BudgetTheme

data class CategoryIconChoice(
    val key: String,
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
)

val categoryIconChoices = listOf(
    CategoryIconChoice("fastfood", "Food", iconRes = R.drawable.baseline_fastfood_24),
    CategoryIconChoice("directions_transit", "Transit", iconRes = R.drawable.baseline_directions_transit_24),
    CategoryIconChoice("cookie", "General", iconRes = R.drawable.baseline_cookie_24),
    CategoryIconChoice("attach_money", "Money", iconRes = R.drawable.baseline_attach_money_24),
    CategoryIconChoice("calendar", "Schedule", iconRes = R.drawable.baseline_calendar_month_24),
    CategoryIconChoice("timeline", "Trend", iconRes = R.drawable.baseline_view_timeline_24),
    CategoryIconChoice("settings_drawable", "Utility", iconRes = R.drawable.baseline_settings_24),
    CategoryIconChoice("money_off", "Bills", iconRes = R.drawable.baseline_money_off_24),
    CategoryIconChoice("add", "Misc", iconVector = Icons.Filled.Add),
    CategoryIconChoice("home", "Home", iconVector = Icons.Filled.Home),
    CategoryIconChoice("shopping_cart", "Shopping", iconVector = Icons.Filled.ShoppingCart),
    CategoryIconChoice("favorite", "Health", iconVector = Icons.Filled.Favorite),
    CategoryIconChoice("star", "Goals", iconVector = Icons.Filled.Star),
    CategoryIconChoice("settings", "Tools", iconVector = Icons.Filled.Settings),
    CategoryIconChoice("search", "Discovery", iconVector = Icons.Filled.Search),
    CategoryIconChoice("edit", "Work", iconVector = Icons.Filled.Edit),
    CategoryIconChoice("delete", "Cleanup", iconVector = Icons.Filled.Delete),
    CategoryIconChoice("info", "Info", iconVector = Icons.Filled.Info),
    CategoryIconChoice("help", "Support", iconVector = Icons.Filled.Info),
    CategoryIconChoice("call", "Phone", iconVector = Icons.Filled.Call),
    CategoryIconChoice("email", "Mail", iconVector = Icons.Filled.Email),
    CategoryIconChoice("send", "Transfer", iconVector = Icons.AutoMirrored.Filled.Send),
    CategoryIconChoice("place", "Travel", iconVector = Icons.Filled.Place),
    CategoryIconChoice("check_circle", "Completed", iconVector = Icons.Filled.CheckCircle),
    CategoryIconChoice("lock", "Security", iconVector = Icons.Filled.Lock),
    CategoryIconChoice("more_vert", "More", iconVector = Icons.Filled.MoreVert),
)

val categoryColorChoices = listOf(
    "#5EBB4A",
    "#2D9CDB",
    "#9AAF47",
    "#4FAF33",
    "#F2994A",
    "#EB5757",
    "#BB6BD9",
    "#56CCF2",
    "#F2C94C",
    "#6FCF97",
    "#8D6E63",
    "#5C6BC0",
    "#FF6B6B",
    "#FF8E72",
    "#FFB347",
    "#FFD166",
    "#06D6A0",
    "#118AB2",
    "#3A86FF",
    "#4361EE",
    "#7209B7",
    "#B5179E",
    "#E76F51",
    "#F4A261",
    "#2A9D8F",
    "#457B9D",
    "#A8DADC",
    "#E9C46A",
    "#90BE6D",
    "#577590",
    "#264653",
    "#1D3557",
    "#355070",
    "#6D597A",
    "#B56576",
    "#E56B6F",
    "#EAAC8B",
    "#D4A373",
    "#7F5539",
    "#283618",
    "#606C38",
    "#386641",
    "#588157",
    "#A3B18A",
    "#A7C957",
    "#0081A7",
    "#00AFB9",
    "#48CAE4",
    "#7B2CBF",
    "#9D4EDD",
    "#C77DFF",
    "#FF5D8F",
    "#E5383B",
    "#D00000",
    "#FF7F51",
    "#F9844A",
    "#F8961E",
    "#43AA8B",
    "#4D908E",
    "#277DA1",
)

@Composable
fun categoryIconPainter(
    iconKey: String,
    fallbackCategoryKey: String = "",
): Painter {
    val choice = categoryIconChoices.firstOrNull { it.key == iconKey }
    choice?.iconVector?.let { return rememberVectorPainter(it) }
    choice?.iconRes?.let { return painterResource(id = it) }

    val fallbackRes = when (fallbackCategoryKey) {
        CATEGORY_KEY_FOOD -> R.drawable.baseline_fastfood_24
        CATEGORY_KEY_TRANSPORTATION -> R.drawable.baseline_directions_transit_24
        CATEGORY_KEY_OTHERS -> R.drawable.baseline_cookie_24
        CATEGORY_KEY_INCOME -> R.drawable.baseline_attach_money_24
        else -> R.drawable.baseline_cookie_24
    }
    return painterResource(id = fallbackRes)
}

@Composable
fun categoryAccentColor(
    colorHex: String,
    fallbackCategoryKey: String = "",
): Color {
    val fallback = when (fallbackCategoryKey) {
        CATEGORY_KEY_FOOD -> BudgetTheme.extendedColors.food
        CATEGORY_KEY_TRANSPORTATION -> BudgetTheme.extendedColors.transit
        CATEGORY_KEY_OTHERS -> BudgetTheme.extendedColors.others
        CATEGORY_KEY_INCOME -> BudgetTheme.extendedColors.income
        else -> MaterialTheme.colorScheme.primary
    }
    return runCatching { Color(AndroidColor.parseColor(colorHex)) }.getOrElse { fallback }
}
