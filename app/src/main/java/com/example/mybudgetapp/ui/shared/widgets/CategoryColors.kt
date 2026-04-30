package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.example.mybudgetapp.database.CATEGORY_KEY_FOOD
import com.example.mybudgetapp.database.CATEGORY_KEY_INCOME
import com.example.mybudgetapp.database.CATEGORY_KEY_OTHERS
import com.example.mybudgetapp.database.CATEGORY_KEY_TRANSPORTATION
import com.example.mybudgetapp.ui.theme.BudgetTheme

private data class CategoryColorFamilyPalette(
    val family: String,
    val keywords: List<String>,
    val hexes: List<String>,
)

private val categoryColorFamilies = listOf(
    CategoryColorFamilyPalette("Red", listOf("red", "scarlet", "ruby"), listOf("#F87171", "#EF4444", "#DC2626", "#B91C1C", "#991B1B")),
    CategoryColorFamilyPalette("Rose", listOf("rose", "crimson", "berry"), listOf("#FB7185", "#F43F5E", "#E11D48", "#BE123C", "#9F1239")),
    CategoryColorFamilyPalette("Pink", listOf("pink", "blush", "bubblegum"), listOf("#F472B6", "#EC4899", "#DB2777", "#BE185D", "#9D174D")),
    CategoryColorFamilyPalette("Fuchsia", listOf("fuchsia", "magenta", "orchid"), listOf("#E879F9", "#D946EF", "#C026D3", "#A21CAF", "#86198F")),
    CategoryColorFamilyPalette("Purple", listOf("purple", "grape", "plum"), listOf("#C084FC", "#A855F7", "#9333EA", "#7E22CE", "#6B21A8")),
    CategoryColorFamilyPalette("Violet", listOf("violet", "iris", "amethyst"), listOf("#A78BFA", "#8B5CF6", "#7C3AED", "#6D28D9", "#5B21B6")),
    CategoryColorFamilyPalette("Indigo", listOf("indigo", "royal", "midnight"), listOf("#818CF8", "#6366F1", "#4F46E5", "#4338CA", "#3730A3")),
    CategoryColorFamilyPalette("Blue", listOf("blue", "azure", "ocean"), listOf("#60A5FA", "#3B82F6", "#2563EB", "#1D4ED8", "#1E40AF")),
    CategoryColorFamilyPalette("Sky", listOf("sky", "cerulean", "air"), listOf("#38BDF8", "#0EA5E9", "#0284C7", "#0369A1", "#075985")),
    CategoryColorFamilyPalette("Cyan", listOf("cyan", "aqua", "glacier"), listOf("#22D3EE", "#06B6D4", "#0891B2", "#0E7490", "#155E75")),
    CategoryColorFamilyPalette("Teal", listOf("teal", "sea", "lagoon"), listOf("#2DD4BF", "#14B8A6", "#0D9488", "#0F766E", "#115E59")),
    CategoryColorFamilyPalette("Emerald", listOf("emerald", "mint", "jade"), listOf("#34D399", "#10B981", "#059669", "#047857", "#065F46")),
    CategoryColorFamilyPalette("Green", listOf("green", "leaf", "forest"), listOf("#4ADE80", "#22C55E", "#16A34A", "#15803D", "#166534")),
    CategoryColorFamilyPalette("Lime", listOf("lime", "chartreuse", "zest"), listOf("#A3E635", "#84CC16", "#65A30D", "#4D7C0F", "#3F6212")),
    CategoryColorFamilyPalette("Yellow", listOf("yellow", "lemon", "sun"), listOf("#FACC15", "#EAB308", "#CA8A04", "#A16207", "#854D0E")),
    CategoryColorFamilyPalette("Amber", listOf("amber", "honey", "gold"), listOf("#FBBF24", "#F59E0B", "#D97706", "#B45309", "#92400E")),
    CategoryColorFamilyPalette("Orange", listOf("orange", "tangerine", "citrus"), listOf("#FB923C", "#F97316", "#EA580C", "#C2410C", "#9A3412")),
    CategoryColorFamilyPalette("Coral", listOf("coral", "salmon", "peach"), listOf("#FF8A80", "#FF6F61", "#F95D6A", "#E76F51", "#D65A31")),
    CategoryColorFamilyPalette("Copper", listOf("copper", "clay", "burnt"), listOf("#E6A15A", "#C97A3D", "#A95C2B", "#8B4513", "#6F3B18")),
    CategoryColorFamilyPalette("Slate", listOf("slate", "graphite", "storm"), listOf("#94A3B8", "#64748B", "#475569", "#334155", "#1E293B")),
)

val categoryColorCatalog = List(5) { toneIndex ->
    categoryColorFamilies.map { family ->
        CategoryColorOption(
            hex = family.hexes[toneIndex],
            label = "${family.family} ${toneIndex + 1}",
            family = family.family,
            keywords = family.keywords + listOf("palette", "tone ${toneIndex + 1}"),
        )
    }
}.flatten()
    .distinctBy { it.hex.lowercase() }

private val categoryColorOptionByNormalizedHex = categoryColorCatalog.associateBy { it.hex.lowercase() }
private val categoryColorSearchIndexByHex = categoryColorCatalog.associate { option ->
    option.hex to buildString {
        append(option.label.lowercase())
        append(' ')
        append(option.family.lowercase())
        append(' ')
        append(option.hex.lowercase())
        option.keywords.forEach {
            append(' ')
            append(it.lowercase())
        }
    }
}

fun resolveCategoryColorOption(colorHex: String): CategoryColorOption? =
    categoryColorOptionByNormalizedHex[colorHex.trim().lowercase()]

fun matchesCategoryColorOption(
    option: CategoryColorOption,
    query: String,
): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = query.trim().lowercase()
    return categoryColorSearchIndexByHex[option.hex]?.contains(normalizedQuery) == true
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
    return runCatching { Color(colorHex.toColorInt()) }.getOrElse { fallback }
}
