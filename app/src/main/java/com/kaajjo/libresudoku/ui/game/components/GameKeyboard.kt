package com.kaajjo.libresudoku.ui.game.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaajjo.libresudoku.ui.theme.LibreSudokuTheme
import com.kaajjo.libresudoku.ui.util.LightDarkPreview

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardItem(
    modifier: Modifier = Modifier,
    number: Int,
    remainingUses: Int? = null,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit = { },
    selected: Boolean = false
) {
    val mutableInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val color by animateColorAsState(
        targetValue = if(selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent)
    val localView = LocalView.current
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .combinedClickable(
                interactionSource = mutableInteractionSource,
                onClick = {
                    onClick(number)
                },
                onLongClick = {
                    localView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onLongClick(number)
                },
                indication = rememberRipple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = number.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if(remainingUses != null) {
                Text(
                    text = remainingUses.toString(),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DefaultGameKeyboard(
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
    remainingUses: List<Int>? = null,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit,
    size: Int,
    selected: Int = 0
) {
    var numbers by remember { mutableStateOf((1..size).toList()) }
    LaunchedEffect(key1 = size) {
        numbers =(1..size).toList()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { number ->
            val hide = remainingUses != null && (remainingUses.size > number && remainingUses[number - 1] <= 0)
            KeyboardItem(
                modifier = itemModifier
                    .weight(1f)
                    .alpha(if (hide) 0f else 1f),
                number = number,
                onClick = { if(!hide) { onClick(number) } },
                onLongClick = { if(!hide) { onLongClick(number) } },
                remainingUses = if(remainingUses != null && remainingUses.size >= number) {
                    remainingUses[number - 1]
                } else {
                    null
                },
                selected = number == selected
            )
        }
    }
}



@LightDarkPreview
@Composable
private fun KeyboardItemPreview() {
    LibreSudokuTheme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KeyboardItem(
                    number = 1,
                    onClick = { }
                )
                KeyboardItem(
                    number = 1,
                    selected = true,
                    onClick = { }
                )
                KeyboardItem(
                    number = 1,
                    remainingUses = 5,
                    onClick = { }
                )
                KeyboardItem(
                    number = 1,
                    remainingUses = 5,
                    selected = true,
                    onClick = { }
                )
            }
        }
    }
}
