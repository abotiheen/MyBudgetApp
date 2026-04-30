package com.example.mybudgetapp.ui.shared.widgets

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mybudgetapp.R

@Composable
fun TotalSpendingText(
    @StringRes spendingOn: Int,
    category: String,
    totalSpending: String,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = spendingOn, category),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BudgetValueText(
            text = totalSpending,
            modifier = Modifier.padding(top = 4.dp),
            tone = BudgetValueTone.Card,
            color = MaterialTheme.colorScheme.onSurface,
            unitLabel = "IQD",
        )
    }
}

@Composable
fun SpendingProgress(
    totalSpending: String,
    totalSpendingOnCategory: String,
    category: String,
    spendingRatio: Float,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeading(
                title = category,
                subtitle = "Share of your spending in this period",
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TotalSpendingText(
                    spendingOn = R.string.total_spending_text,
                    totalSpending = totalSpendingOnCategory,
                    category = category,
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    BudgetValueText(
                        text = totalSpending,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        tone = BudgetValueTone.Compact,
                        color = MaterialTheme.colorScheme.primary,
                        unitLabel = "IQD",
                    )
                }
            }
            LinearProgressIndicator(
                progress = { spendingRatio.coerceIn(0f, 1f) },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
            )
        }
    }
}
