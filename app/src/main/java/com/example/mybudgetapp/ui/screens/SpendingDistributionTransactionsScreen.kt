package com.example.mybudgetapp.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.navigation.NavigationDestination
import com.example.mybudgetapp.ui.theme.BudgetTheme
import com.example.mybudgetapp.ui.viewmodels.AppViewModelProvider
import com.example.mybudgetapp.ui.viewmodels.DistributionTransactionUi
import com.example.mybudgetapp.ui.viewmodels.SpendingDistributionTransactionsUiState
import com.example.mybudgetapp.ui.viewmodels.SpendingDistributionTransactionsViewModel
import com.example.mybudgetapp.ui.widgets.BudgetBackdrop
import com.example.mybudgetapp.ui.widgets.BudgetTopAppBar
import com.example.mybudgetapp.ui.widgets.BudgetValueText
import com.example.mybudgetapp.ui.widgets.BudgetValueTone
import com.example.mybudgetapp.ui.widgets.CategoryIcon
import com.example.mybudgetapp.ui.widgets.categoryAccentColor
import com.example.mybudgetapp.ui.widgets.categoryPlaceholderPainter

object SpendingDistributionTransactionsDestination : NavigationDestination {
    override val route = "SpendingDistributionTransactions"
    override val titleRes = R.string.spending_on_category_screen
    const val startDate = "startDate"
    const val endDate = "endDate"
    const val categoryKeys = "categoryKeys"
    val routeWithArgs = "$route/{$startDate}/{$endDate}/{$categoryKeys}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingDistributionTransactionsScreen(
    navigateBack: () -> Unit,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SpendingDistributionTransactionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            BudgetTopAppBar(
                canNavigateBack = true,
                navigateBack = navigateBack,
                scrollBehavior = scrollBehavior,
                title = uiState.title,
            )
        },
    ) { paddingValues ->
        BudgetBackdrop(modifier = Modifier.padding(paddingValues)) {
            SpendingDistributionTransactionsContent(
                uiState = uiState,
                navigateToItemDates = navigateToItemDates,
            )
        }
    }
}

@Composable
private fun SpendingDistributionTransactionsContent(
    uiState: SpendingDistributionTransactionsUiState,
    navigateToItemDates: (String, String, String, Int, Int) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = BudgetTheme.spacing.lg,
            end = BudgetTheme.spacing.lg,
            top = BudgetTheme.spacing.lg,
            bottom = 40.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
    ) {
        item {
            DistributionTransactionsHero(uiState)
        }
        if (uiState.transactions.isEmpty()) {
            item {
                DetailEmptyStateCard(
                    title = "No transactions found",
                    message = "This category has no entries in the selected range.",
                    accent = MaterialTheme.colorScheme.primary,
                    iconKey = "trends",
                    fallbackCategoryKey = "others",
                )
            }
        } else {
            items(uiState.transactions, key = { it.id }) { transaction ->
                DistributionTransactionRow(
                    transaction = transaction,
                    onOpen = {
                        navigateToItemDates(
                            transaction.title,
                            transaction.categoryKey,
                            transaction.type,
                            transaction.year,
                            transaction.month,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DistributionTransactionsHero(uiState: SpendingDistributionTransactionsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgetTheme.radii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(BudgetTheme.elevations.level2),
    ) {
        Column(
            modifier = Modifier.padding(BudgetTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.sm),
        ) {
            Text(
                text = uiState.periodLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            BudgetValueText(
                text = uiState.totalLabel,
                tone = BudgetValueTone.Hero,
                color = MaterialTheme.colorScheme.onSurface,
                unitLabel = "IQD",
            )
            Text(
                text = "${uiState.transactions.size} transactions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DistributionTransactionRow(
    transaction: DistributionTransactionUi,
    onOpen: () -> Unit,
) {
    val accent = categoryAccentColor(transaction.categoryColorHex, transaction.categoryKey)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpen,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(BudgetTheme.radii.lg),
        shadowElevation = BudgetTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(BudgetTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(BudgetTheme.radii.md),
            ) {
                if (transaction.imagePath != null) {
                    AsyncImage(
                        model = transaction.imagePath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = categoryPlaceholderPainter(transaction.categoryIconKey, transaction.categoryKey),
                        error = categoryPlaceholderPainter(transaction.categoryIconKey, transaction.categoryKey),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(BudgetTheme.radii.md)),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CategoryIcon(
                            iconKey = transaction.categoryIconKey,
                            fallbackCategoryKey = transaction.categoryKey,
                            tint = accent,
                            size = 24.dp,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${transaction.categoryLabel} • ${transaction.displayDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BudgetValueText(
                    text = transaction.amount,
                    tone = BudgetValueTone.Compact,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    unitLabel = "IQD",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
