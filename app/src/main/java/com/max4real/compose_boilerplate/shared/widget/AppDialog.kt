package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.ui.theme.CustomColor
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme

/**
 * Rounded surface + padded column shared by all app dialogs.
 * Exposed separately from [AppDialogFrame] so dialog content can be previewed
 * without the Dialog window wrapper.
 */
@Composable
fun AppDialogSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    contentPadding: Dp = 22.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = theme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/** [AppDialogSurface] wrapped in a [Dialog] window. */
@Composable
fun AppDialogFrame(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    contentPadding: Dp = 22.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AppDialogSurface(
            modifier = modifier,
            cornerRadius = cornerRadius,
            contentPadding = contentPadding,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

@Composable
fun AppDialogTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Text(
        text = text,
        fontFamily = OpenAISans,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        color = theme.mainText,
        modifier = modifier
    )
}

@Composable
fun AppDialogMessage(
    text: String,
    modifier: Modifier = Modifier,
    lineHeight: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    Text(
        text = text,
        fontFamily = OpenAISans,
        fontSize = 14.sp,
        lineHeight = lineHeight,
        color = theme.textSecondary,
        modifier = modifier
    )
}

/**
 * Cancel / Confirm row using plain TextButtons, with a leading spacer that
 * pushes both buttons to the right (the PinMessageDialog button style).
 * Buttons size to their text instead of sharing a fixed weight, so longer
 * confirm labels (e.g. "Continue") don't get cropped.
 */
@Composable
fun AppDialogTextActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String,
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    confirmTextColor: Color = CustomColor.accentBlue,
    buttonHeight: Dp = 38.dp,
    buttonCornerRadius: Dp = 14.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            modifier = Modifier.height(buttonHeight),
            onClick = onCancel,
            shape = RoundedCornerShape(buttonCornerRadius),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Text(
                text = cancelText,
                fontFamily = OpenAISans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = theme.textSecondary,
                maxLines = 1
            )
        }
        TextButton(
            modifier = Modifier.height(buttonHeight),
            onClick = onConfirm,
            shape = RoundedCornerShape(buttonCornerRadius),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            Text(
                text = confirmText,
                fontFamily = OpenAISans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = confirmTextColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Selectable row with a checkbox, animated highlight background, and label —
 * the PinMessageDialog checkbox style, reused by any dialog with a toggle.
 */
@Composable
fun AppDialogCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) CustomColor.accentBlue.copy(alpha = 0.08f) else Color.Transparent,
        label = "bgColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = CustomColor.accentBlue,
                uncheckedColor = theme.textSecondary
            ),
            modifier = Modifier.size(20.dp)
        )
        12.WidthBox()
        Text(
            text = text,
            fontFamily = OpenAISans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (checked) CustomColor.accentBlue else theme.mainText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
