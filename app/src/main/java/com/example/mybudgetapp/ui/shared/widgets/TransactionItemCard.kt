package com.example.mybudgetapp.ui.shared.widgets

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mybudgetapp.R
import com.example.mybudgetapp.ui.theme.BudgetTheme

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    title: String,
    totalSpending: String,
    iconKey: String,
    fallbackCategoryKey: String = "",
    accentColor: Color,
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
                        .background(accentColor.copy(alpha = 0.14f))
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
                            placeholder = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                            error = categoryPlaceholderPainter(iconKey, fallbackCategoryKey),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CategoryIcon(
                            iconKey = iconKey,
                            fallbackCategoryKey = fallbackCategoryKey,
                            tint = accentColor,
                            size = 28.dp,
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
