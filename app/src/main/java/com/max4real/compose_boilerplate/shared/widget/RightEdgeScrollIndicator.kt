package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.roundToInt

private val IndicatorThickness = 3.dp
private val IndicatorEdgeMargin = 2.dp
private val IndicatorMinLength = 28.dp
private val IndicatorShape = RoundedCornerShape(percent = 50)
private const val IndicatorHideDelayMs = 800L

// Tracks closely rather than trailing the content — this is an indicator, so it should read as
// attached to the scroll, not as an object being animated toward it. Stiff and critically damped:
// enough to round off the per-item stepping in the raw fraction, not enough to lag a fling.
private val IndicatorPositionSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

/**
 * iOS-style scroll position indicator: a thin, proportional-length bar on the right edge that
 * fades in while scrolling and out shortly after. Purely an indicator — deliberately **not**
 * draggable, and it takes no touch input at all, so it can never steal a gesture from the content
 * underneath.
 *
 * Proportional-and-thin rather than a fixed-size drag handle on purpose: a lazy list cannot know
 * its own total content height, so any position derived here is an estimate off item indices. A
 * thin bar whose own length already says "roughly this much content" absorbs that error
 * invisibly, whereas a fixed-size handle turns every approximation error into a visible stall or
 * jump.
 */
@Composable
fun RightEdgeScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    // Sampled only at rest and frozen for the duration of a scroll: mid-scroll the visible-item
    // count flickers by one as partially-visible rows enter and leave, and it feeds both the bar's
    // length and its position denominator — live, that flicker shows up as the bar twitching.
    var restingVisibleCount by remember { mutableIntStateOf(1) }
    LaunchedEffect(listState) {
        snapshotFlow {
            if (listState.isScrollInProgress) null
            else listState.layoutInfo.visibleItemsInfo.size.takeIf { it > 0 }
        }.collect { count -> if (count != null) restingVisibleCount = count }
    }

    val position by remember {
        derivedStateOf {
            val firstItemSize = listState.layoutInfo.visibleItemsInfo
                .firstOrNull()?.size?.takeIf { it > 0 } ?: 1
            val withinItemFraction =
                (listState.firstVisibleItemScrollOffset.toFloat() / firstItemSize.toFloat())
                    .coerceIn(0f, 1f)
            positionFraction(
                canScrollForward = listState.canScrollForward,
                canScrollBackward = listState.canScrollBackward,
                continuous = listState.firstVisibleItemIndex + withinItemFraction,
                total = listState.layoutInfo.totalItemsCount,
                visible = restingVisibleCount
            )
        }
    }
    val length by remember {
        derivedStateOf {
            lengthFraction(
                total = listState.layoutInfo.totalItemsCount,
                visible = restingVisibleCount
            )
        }
    }

    ScrollIndicatorBar(
        positionFraction = position,
        lengthFraction = length,
        isScrollInProgress = listState.isScrollInProgress,
        modifier = modifier
    )
}

/**
 * Grid variant — measured in grid *rows*, since a grid mixing full-span headers with N-column
 * content rows advances flat item index at a different rate per row type.
 *
 * [totalRows] is the exact total row count. Pass it when the caller can compute it: there is no
 * total-rows API on `LazyGridLayoutInfo`, and it cannot be inferred from item count alone here
 * (a full-span header occupies a whole row while counting as one item, so an items-per-row
 * estimate under-counts rows by exactly the header count and the bar reaches the bottom early).
 */
@Composable
fun RightEdgeScrollIndicator(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    totalRows: Int? = null
) {
    var restingRowSpan by remember { mutableIntStateOf(1) }
    var restingItemsPerRow by remember { mutableIntStateOf(1) }
    LaunchedEffect(gridState) {
        snapshotFlow {
            if (gridState.isScrollInProgress) null
            else gridState.layoutInfo.visibleItemsInfo
                .takeIf { it.isNotEmpty() }
                ?.groupBy { it.row }
                // Max items in a row is the live column count exactly — full-span header rows
                // hold a single item, so max naturally reads a content row instead.
                ?.let { byRow -> byRow.size to byRow.values.maxOf { cells -> cells.size } }
        }.collect { sample ->
            if (sample != null) {
                restingRowSpan = sample.first
                restingItemsPerRow = sample.second
            }
        }
    }

    val rowCount by remember {
        derivedStateOf {
            totalRows ?: ceil(
                gridState.layoutInfo.totalItemsCount / restingItemsPerRow.toFloat()
            ).toInt()
        }
    }
    val position by remember {
        derivedStateOf {
            val firstItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull()
            val firstRowHeight = firstItem?.size?.height?.takeIf { it > 0 } ?: 1
            val withinRowFraction =
                (gridState.firstVisibleItemScrollOffset.toFloat() / firstRowHeight.toFloat())
                    .coerceIn(0f, 1f)
            positionFraction(
                canScrollForward = gridState.canScrollForward,
                canScrollBackward = gridState.canScrollBackward,
                continuous = (firstItem?.row ?: 0) + withinRowFraction,
                total = rowCount,
                visible = restingRowSpan
            )
        }
    }
    val length by remember {
        derivedStateOf { lengthFraction(total = rowCount, visible = restingRowSpan) }
    }

    ScrollIndicatorBar(
        positionFraction = position,
        lengthFraction = length,
        isScrollInProgress = gridState.isScrollInProgress,
        modifier = modifier
    )
}

private fun positionFraction(
    canScrollForward: Boolean,
    canScrollBackward: Boolean,
    continuous: Float,
    total: Int,
    visible: Int
): Float = when {
    // Ground truth for the ends, which index math alone can't reliably land on.
    !canScrollForward -> 1f
    !canScrollBackward -> 0f
    // The furthest the first visible item can get is `total - visible`, not `total - 1`: scrolled
    // fully down, the last `visible` items are the ones on screen.
    else -> (continuous / (total - visible).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

private fun lengthFraction(total: Int, visible: Int): Float =
    if (total <= 0) 1f else (visible.toFloat() / total.toFloat()).coerceIn(0f, 1f)

@Composable
private fun ScrollIndicatorBar(
    positionFraction: Float,
    lengthFraction: Float,
    isScrollInProgress: Boolean,
    modifier: Modifier = Modifier
) {
    // Content fits on one screen — nothing to indicate.
    if (lengthFraction >= 1f) return

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isScrollInProgress) {
        if (isScrollInProgress) {
            isVisible = true
        } else {
            delay(IndicatorHideDelayMs)
            isVisible = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = if (isVisible) 100 else 300),
        label = "indicatorAlpha"
    )
    val animatedPosition by animateFloatAsState(
        targetValue = positionFraction,
        animationSpec = IndicatorPositionSpring,
        label = "indicatorPosition"
    )

    if (alpha <= 0f) return

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(IndicatorThickness + IndicatorEdgeMargin * 2)
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        val minLengthPx = with(density) { IndicatorMinLength.toPx() }
        val barHeightPx = (lengthFraction * trackHeightPx)
            .coerceIn(minLengthPx.coerceAtMost(trackHeightPx), trackHeightPx)
        val offsetPx = animatedPosition * (trackHeightPx - barHeightPx)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, offsetPx.roundToInt()) }
                .padding(end = IndicatorEdgeMargin)
                .width(IndicatorThickness)
                .height(with(density) { barHeightPx.toDp() })
                .alpha(alpha)
                .clip(IndicatorShape)
                .background(theme.chipSelectedBackground)
        )
    }
}
