package com.example.mybudgetapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybudgetapp.R
import com.example.mybudgetapp.database.BudgetCategory
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.CategoryChangeResult
import com.example.mybudgetapp.ui.viewmodels.ItemDatesUiState
import com.example.mybudgetapp.ui.viewmodels.ItemDatesViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ItemDatesScreenNavigationDestination : NavigationDestination {
    override val route = "ItemDates"
    override val titleRes = R.string.spending_on_category_screen
    const val title = "title"
    const val category = "category"
    const val type = "type"
    const val year = "year"
    const val month = "month"
    val routeWithArgs = "$route/{$title}/{$category}/{$type}/{$year}/{$month}"

    fun createRoute(
        title: String,
        category: String,
        type: String,
        year: Int,
        month: Int,
    ): String = "$route/${Uri.encode(title)}/$category/$type/$year/$month"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDatesScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: ItemDatesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val context = LocalContext.current
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                title = stringResource(id = R.string.item_dates),
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
            )
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            ItemDatesBody(
                uiState = uiState,
                availableCategories = availableCategories,
                onChangeCategory = { categoryKey ->
                    viewModel.changeCategory(categoryKey) { result ->
                        when (result) {
                            is CategoryChangeResult.Invalid -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }

                            is CategoryChangeResult.Success -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                showCategorySheet = false
                            }
                        }
                    }
                },
                onOpenCategorySheet = { showCategorySheet = true },
            )
        }
    }

    if (showCategorySheet) {
        ChangeCategoryBottomSheet(
            currentCategory = uiState.category,
            categories = availableCategories,
            isSaving = uiState.isUpdatingCategory,
            onSelectCategory = { categoryKey ->
                viewModel.changeCategory(categoryKey) { result ->
                    when (result) {
                        is CategoryChangeResult.Invalid -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }

                        is CategoryChangeResult.Success -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            showCategorySheet = false
                        }
                    }
                }
            },
            onDismissRequest = { showCategorySheet = false },
        )
    }
}

@Composable
fun ItemDatesBody(
    modifier: Modifier = Modifier,
    uiState: ItemDatesUiState,
    availableCategories: List<BudgetCategory>,
    onOpenCategorySheet: () -> Unit,
    onChangeCategory: (String) -> Unit,
) {
    val spacing = BudgetTheme.spacing
    val accent = categoryAccentColor(uiState.categoryColorHex, uiState.category)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item {
            ItemDetailOverviewCard(
                title = uiState.name,
                amount = uiState.amount,
                typeLabel = uiState.typeLabel,
                categoryLabel = uiState.categoryLabel,
                imagePath = uiState.picturePath,
                iconKey = uiState.categoryIconKey,
                fallbackCategoryKey = uiState.category,
                accent = accent,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    label = "Type",
                    value = uiState.typeLabel,
                    subtitle = "Entry behavior",
                )
                DetailInfoTile(
                    modifier = Modifier.weight(1f),
                    label = if (uiState.historyCount > 1) "Latest save" else "Saved on",
                    value = uiState.date,
                    subtitle = "Recorded timeline",
                )
            }
        }
        item {
            ChangeCategoryCard(
                currentCategory = uiState.category,
                categoryLabel = uiState.categoryLabel,
                availableCategories = availableCategories,
                isUpdating = uiState.isUpdatingCategory,
                canChangeCategory = uiState.canChangeCategory,
                onOpenCategorySheet = onOpenCategorySheet,
                onChangeCategory = onChangeCategory,
            )
        }
        item {
            DetailHistoryCard(
                title = "Saved history",
                subtitle = if (uiState.historyCount > 1) {
                    "Every recorded save attached to this grouped item."
                } else {
                    "The exact recorded value attached to this entry."
                },
                history = uiState.itemDatesList,
                accent = accent,
            )
        }
    }
}

@Composable
private fun ChangeCategoryCard(
    currentCategory: String,
    categoryLabel: String,
    availableCategories: List<BudgetCategory>,
    isUpdating: Boolean,
    canChangeCategory: Boolean,
    onOpenCategorySheet: () -> Unit,
    onChangeCategory: (String) -> Unit,
) {
    val alternativeCategories = availableCategories.filterNot { it.categoryKey == currentCategory }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Current category: $categoryLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!canChangeCategory) {
                Text(
                    text = "This item is no longer available to edit in the current view.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "Changing the category updates every saved entry attached to this item in the current period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                Button(
                    onClick = onOpenCategorySheet,
                    enabled = canChangeCategory && !isUpdating && alternativeCategories.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Text(text = if (isUpdating) "Updating..." else "Change category")
                }
                if (alternativeCategories.size == 1) {
                    OutlinedButton(
                        onClick = { onChangeCategory(alternativeCategories.first().categoryKey) },
                        enabled = canChangeCategory && !isUpdating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(BudgetTheme.radii.lg),
                    ) {
                        Text("Quick change")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeCategoryBottomSheet(
    currentCategory: String,
    categories: List<BudgetCategory>,
    isSaving: Boolean,
    onSelectCategory: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissSheet() {
        coroutineScope.launch {
            delay(120)
            sheetState.hide()
            onDismissRequest()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BudgetTheme.spacing.xl, vertical = BudgetTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Text(
                text = "Change category",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Select a category with the same transaction type.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CompactCategoryGrid(
                categories = categories,
                currentCategory = currentCategory,
                isSaving = isSaving,
                onSelectCategory = onSelectCategory,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactCategoryGrid(
    categories: List<BudgetCategory>,
    currentCategory: String,
    isSaving: Boolean,
    onSelectCategory: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
    ) {
        categories.forEach { category ->
            CompactCategoryOptionCard(
                category = category,
                isSelected = category.categoryKey == currentCategory,
                enabled = !isSaving,
                onClick = { onSelectCategory(category.categoryKey) },
            )
        }
    }
}

@Composable
private fun CompactCategoryOptionCard(
    category: BudgetCategory,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val accent = categoryAccentColor(category.colorHex, category.categoryKey)

    Surface(
        onClick = onClick,
        enabled = enabled && !isSelected,
        modifier = Modifier.fillMaxWidth(0.48f),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                accent.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = if (isSelected) {
                        accent.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    },
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryIcon(
                            iconKey = category.iconKey,
                            fallbackCategoryKey = category.categoryKey,
                            tint = accent,
                            size = 22.dp,
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = accent,
                    )
                }
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )

            if (category.isArchived) {
                Text(
                    text = "Archived",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ItemDetailOverviewCard(
    title: String,
    amount: String,
    typeLabel: String,
    categoryLabel: String,
    imagePath: String?,
    iconKey: String,
    fallbackCategoryKey: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f),
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(BudgetTheme.radii.md),
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryIcon(
                            iconKey = iconKey,
                            fallbackCategoryKey = fallbackCategoryKey,
                            tint = accent,
                            size = 28.dp,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Entry details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
            ) {
                DetailTag(label = typeLabel, tint = accent)
                DetailTag(label = categoryLabel, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            BudgetValueText(
                text = amount,
                modifier = Modifier.fillMaxWidth(),
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onSurface,
                unitLabel = "IQD",
            )

            if (imagePath != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(BudgetTheme.radii.lg)),
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(BudgetTheme.radii.lg),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(BudgetTheme.spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(accent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            CategoryIcon(
                                iconKey = iconKey,
                                fallbackCategoryKey = fallbackCategoryKey,
                                tint = accent,
                                size = 28.dp,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "No image attached",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "This entry was saved without a receipt or photo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTag(
    label: String,
    tint: Color,
) {
    Surface(
        color = tint.copy(alpha = 0.1f),
        shape = RoundedCornerShape(BudgetTheme.radii.pill),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}

@Composable
private fun DetailInfoTile(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailHistoryCard(
    title: String,
    subtitle: String,
    history: List<com.example.mybudgetapp.ui.viewmodels.ItemWIthDates>,
    accent: Color,
) {
    Card(
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level1),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            history.forEachIndexed { index, item ->
                DetailHistoryRow(
                    date = item.date,
                    amount = item.cost,
                    accent = accent,
                )
                if (index != history.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BudgetTheme.extendedColors.edge)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHistoryRow(
    date: String,
    amount: String,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Recorded value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BudgetValueText(
            text = amount,
            tone = BudgetValueTone.Card,
            color = accent,
            unitLabel = "IQD",
        )
    }
}
