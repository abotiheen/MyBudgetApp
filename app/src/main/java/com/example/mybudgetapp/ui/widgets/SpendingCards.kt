package com.example.mybudgetapp.ui.widgets

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mybudgetapp.R
import com.example.mybudgetapp.data.SpendingCategoryDisplayData
import com.example.mybudgetapp.ui.theme.BudgetTheme

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

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    title: String,
    totalSpending: String,
    displayItem: SpendingCategoryDisplayData,
    date: String,
    imagePath: String?,
    deleteItem: () -> Unit,
    navigateToItemDates: () -> Unit,
) {
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var isDeleteMenuVisible by rememberSaveable { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .onSizeChanged { itemHeight = with(density) { it.height.toDp() } }
            .clickable { navigateToItemDates() },
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .indication(interactionSource, LocalIndication.current)
                .pointerInput(true) {
                    detectTapGestures(
                        onLongPress = {
                            isContextMenuVisible = true
                            pressOffset = DpOffset(it.x.toDp(), it.y.toDp())
                        },
                        onPress = {
                            val press = PressInteraction.Press(it)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        },
                        onTap = { navigateToItemDates() },
                    )
                },
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f))
                        .border(
                            width = 1.dp,
                            color = BudgetTheme.extendedColors.edge,
                            shape = RoundedCornerShape(20.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (imagePath != null) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = null,
                            placeholder = painterResource(id = displayItem.spendingIcon),
                            error = painterResource(id = displayItem.spendingIcon),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = displayItem.spendingIcon),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            BudgetValueText(
                text = totalSpending,
                tone = BudgetValueTone.Card,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                unitLabel = "IQD",
            )
        }
        DropdownMenu(
            expanded = isContextMenuVisible,
            onDismissRequest = { isContextMenuVisible = false },
            offset = pressOffset.copy(y = pressOffset.y - itemHeight),
            modifier = Modifier.padding(8.dp),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.delete_item),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                onClick = {
                    isDeleteMenuVisible = true
                    isContextMenuVisible = false
                },
            )
        }
    }
    if (isDeleteMenuVisible) {
        DeleteConfirmationDialog(
            onDeleteCancel = { isDeleteMenuVisible = false },
            onDeleteConfirm = {
                deleteItem()
                isDeleteMenuVisible = false
            },
        )
    }
}

@Composable
fun TotalIncomeCard(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    totalSpending: String,
    month: String,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(id = title, month),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.74f),
            )
            BudgetValueText(
                text = totalSpending,
                tone = BudgetValueTone.Prominent,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 2,
                unitLabel = "IQD",
            )
        }
    }
}

@Composable
fun DateCard(
    modifier: Modifier = Modifier,
    title: String,
    totalSpending: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            BudgetValueText(
                text = totalSpending,
                tone = BudgetValueTone.Compact,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                unitLabel = "IQD",
            )
        }
    }
}

@Composable
fun ItemCardForDates(
    modifier: Modifier = Modifier,
    title: String,
    displayItem: SpendingCategoryDisplayData,
    imagePath: String?,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f))
                    .border(
                        width = 1.dp,
                        color = BudgetTheme.extendedColors.edge,
                        shape = RoundedCornerShape(20.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (imagePath != null) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        placeholder = painterResource(id = displayItem.spendingIcon),
                        error = painterResource(id = displayItem.spendingIcon),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = displayItem.spendingIcon),
                        contentDescription = null,
                    )
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
