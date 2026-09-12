package com.max4real.compose_boilerplate.shared.widget.spoiler

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

// Plain Text unless something is hidden, so untouched text keeps its exact metrics.
@Composable
internal fun SpoilerAwareText(
    text: AnnotatedString,
    spoilerRanges: List<IntRange>,
    isObscured: Boolean,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    if (spoilerRanges.isEmpty()) {
        Text(
            text = text,
            color = color,
            style = style,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
            onTextLayout = onTextLayout
        )
    } else {
        SpoilerTextV2(
            text = text,
            spoilerRanges = spoilerRanges,
            color = color,
            style = style,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = TextOverflow.Ellipsis,
            isObscured = isObscured,
            modifier = modifier,
            onTextLayout = onTextLayout
        )
    }
}
