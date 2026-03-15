package com.example.mybudgetapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayObject
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.ItemDatesUiState
import com.example.mybudgetapp.ui.viewmodels.ItemDatesViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.DateCard
import com.example.mybudgetapp.ui.widgets.SectionHeading

object ItemDatesScreenNavigationDestination: NavigationDestination {
    override val route = "ItemDates"
    override val titleRes = R.string.spending_on_category_screen
    const val id: Long = 0
    const val imagePath: String = ""
    const val name: String = ""
    val routeWithArgs = "$route/{$id}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDatesScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: ItemDatesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState = viewModel.uiState.collectAsState()

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
                uiState = uiState.value,
            )
        }
    }
}

@Composable
fun ItemDatesBody(
    modifier: Modifier = Modifier,
    uiState: ItemDatesUiState,
) {
    val spacing = BudgetTheme.spacing
    val displayItem = when (uiState.category) {
        "food" -> SpendingCategoryDisplayObject.items[0]
        "others" -> SpendingCategoryDisplayObject.items[2]
        "transportation" -> SpendingCategoryDisplayObject.items[1]
        else -> SpendingCategoryDisplayObject.items[3]
    }
    val accent = when (uiState.category) {
        "food" -> BudgetTheme.extendedColors.food
        "others" -> BudgetTheme.extendedColors.others
        "transportation" -> BudgetTheme.extendedColors.transit
        else -> BudgetTheme.extendedColors.income
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            EntryHeroDetailCard(
                title = uiState.name,
                amount = uiState.amount,
                typeLabel = uiState.typeLabel,
                categoryLabel = uiState.categoryLabel,
                imagePath = uiState.picturePath,
                icon = displayItem.spendingIcon,
                accent = accent,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                EntryMetaCard(
                    modifier = Modifier.weight(1f),
                    label = "Category",
                    value = uiState.categoryLabel,
                    subtitle = "Tagged for tracking",
                )
                EntryMetaCard(
                    modifier = Modifier.weight(1f),
                    label = "Type",
                    value = uiState.typeLabel,
                    subtitle = "How this entry behaves",
                )
            }
        }
        item {
            EntryMetaCard(
                label = "Recorded on",
                value = uiState.date,
                subtitle = "Saved to your timeline",
            )
        }
        item {
            SectionHeading(
                title = "Recorded timeline",
                subtitle = "This is the saved moment for the entry you opened.",
            )
        }
        items(uiState.itemDatesList) {
            DateCard(
                title = it.date,
                totalSpending = it.cost,
            )
        }
    }
}

@Composable
private fun EntryHeroDetailCard(
    title: String,
    amount: String,
    typeLabel: String,
    categoryLabel: String,
    imagePath: String?,
    icon: Int,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level3),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                )
                .padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(BudgetTheme.radii.pill),
                ) {
                    Text(
                        text = categoryLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(BudgetTheme.radii.lg),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BudgetTheme.spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    if (imagePath != null) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(220.dp),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .background(accent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryMetaCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
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
