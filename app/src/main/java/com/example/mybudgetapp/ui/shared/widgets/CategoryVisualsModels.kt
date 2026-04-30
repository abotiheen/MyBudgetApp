package com.example.mybudgetapp.ui.shared.widgets

data class CategoryIconChoice(
    val key: String,
    val label: String,
    val ligature: String,
    val group: String,
    val keywords: List<String> = emptyList(),
)

data class CategoryColorOption(
    val hex: String,
    val label: String,
    val family: String,
    val keywords: List<String> = emptyList(),
)

const val defaultCategoryIconKey = "misc"
const val defaultCategoryColorHex = "#34944C"
