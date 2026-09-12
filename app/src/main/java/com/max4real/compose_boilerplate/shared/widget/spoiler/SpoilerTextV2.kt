package com.max4real.compose_boilerplate.shared.widget.spoiler

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Immutable
data class SpoilerStyle(
    val particleDensity: Float = 0.42f,
    val maxParticles: Int = 900,
    val minRadius: Dp = 0.35.dp,
    val maxRadius: Dp = 0.95.dp,
    val speed: Float = 1f,
    val drift: Dp = 5.dp,
    val minLifetime: Float = 0.55f,
    val maxLifetime: Float = 1.5f,
    val feather: Dp = 2.dp,
    val horizontalPadding: Dp = 2.dp,
    val verticalPadding: Dp = 0.dp,
    val burstDistance: Dp = 12.dp,
    val dissolveDurationMs: Int = 420
)

@Composable
fun SpoilerTextV2(
    text: String,
    spoilerRanges: List<IntRange>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    isObscured: Boolean = true,
    particleColor: Color = Color.Unspecified,
    spoilerStyle: SpoilerStyle = SpoilerStyle(),
    onSpoilerTap: ((Offset) -> Unit)? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    SpoilerTextV2(
        text = remember(text) { AnnotatedString(text) },
        spoilerRanges = spoilerRanges,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = overflow,
        isObscured = isObscured,
        particleColor = particleColor,
        spoilerStyle = spoilerStyle,
        onSpoilerTap = onSpoilerTap,
        onTextLayout = onTextLayout
    )
}

@Composable
fun SpoilerTextV2(
    text: AnnotatedString,
    spoilerRanges: List<IntRange>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    isObscured: Boolean = true,
    particleColor: Color = Color.Unspecified,
    spoilerStyle: SpoilerStyle = SpoilerStyle(),
    onSpoilerTap: ((Offset) -> Unit)? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val density = LocalDensity.current
    val fallbackColor = LocalContentColor.current
    val resolvedColor = color.takeOrElse { style.color.takeOrElse { fallbackColor } }
    val dustColor = particleColor.takeOrElse { resolvedColor }

    val ranges = remember(text, spoilerRanges) {
        normalizeRanges(spoilerRanges, text.length)
    }
    // Glyphs in a spoiler range are painted transparent, so the text itself never
    // reaches the screen even if the particle overlay fails to draw.
    val displayText = remember(text, ranges, isObscured) {
        if (!isObscured || ranges.isEmpty()) text else text.withHiddenRanges(ranges)
    }
    val pool = remember(spoilerStyle.maxParticles) { SpoilerParticlePool.of(spoilerStyle.maxParticles) }

    var spoilerRects by remember { mutableStateOf(emptyList<Rect>()) }
    var tapOrigin by remember { mutableStateOf<Offset?>(null) }
    val clock = remember { mutableFloatStateOf(0f) }
    val reveal = remember { mutableFloatStateOf(if (isObscured) 0f else 1f) }
    val hasRects = spoilerRects.isNotEmpty()

    LaunchedEffect(isObscured, hasRects, spoilerStyle.dissolveDurationMs) {
        if (!hasRects) return@LaunchedEffect

        var last = withFrameNanos { it }
        if (isObscured) {
            reveal.floatValue = 0f
            while (true) {
                withFrameNanos { now ->
                    clock.floatValue = wrapClock(clock.floatValue + (now - last) / 1_000_000_000f)
                    last = now
                }
            }
        }

        val duration = spoilerStyle.dissolveDurationMs / 1000f
        var elapsed = 0f
        while (elapsed < duration) {
            withFrameNanos { now ->
                val delta = (now - last) / 1_000_000_000f
                last = now
                elapsed += delta
                clock.floatValue = wrapClock(clock.floatValue + delta)
                reveal.floatValue = (elapsed / duration).coerceAtMost(1f)
            }
        }
        reveal.floatValue = 1f
        tapOrigin = null
    }

    val tapModifier = if (onSpoilerTap == null) {
        Modifier
    } else {
        Modifier.pointerInput(spoilerRects) {
            detectTapGestures { position ->
                if (spoilerRects.none { it.contains(position) }) return@detectTapGestures
                tapOrigin = position
                onSpoilerTap(position)
            }
        }
    }

    BasicText(
        text = displayText,
        style = style.merge(TextStyle(color = resolvedColor)),
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = overflow,
        onTextLayout = { layoutResult ->
            onTextLayout(layoutResult)
            spoilerRects = layoutResult.spoilerRects(ranges, spoilerStyle, density)
        },
        modifier = modifier
            .then(tapModifier)
            .drawWithContent {
                drawContent()

                val rects = spoilerRects
                val revealProgress = reveal.floatValue
                if (rects.isEmpty() || revealProgress >= 1f) return@drawWithContent

                rects.forEachIndexed { index, rect ->
                    drawSpoilerDust(
                        pool = pool,
                        rect = rect,
                        color = dustColor,
                        style = spoilerStyle,
                        time = clock.floatValue + index * 0.61f,
                        reveal = revealProgress,
                        origin = tapOrigin ?: rect.center
                    )
                }
            }
    )
}

// Regex match ranges are inclusive, so callers can pass MatchResult.range straight through.
private fun normalizeRanges(ranges: List<IntRange>, textLength: Int): List<IntRange> {
    if (ranges.isEmpty() || textLength == 0) return emptyList()

    val sorted = ranges
        .mapNotNull { range ->
            val start = range.first.coerceIn(0, textLength - 1)
            val end = range.last.coerceIn(0, textLength - 1)
            if (end < start) null else start..end
        }
        .sortedBy { it.first }

    val merged = mutableListOf<IntRange>()
    sorted.forEach { range ->
        val previous = merged.lastOrNull()
        if (previous != null && range.first <= previous.last + 1) {
            merged[merged.lastIndex] = previous.first..maxOf(previous.last, range.last)
        } else {
            merged += range
        }
    }
    return merged
}

private fun AnnotatedString.withHiddenRanges(ranges: List<IntRange>): AnnotatedString {
    return buildAnnotatedString {
        append(this@withHiddenRanges)
        ranges.forEach { range ->
            addStyle(SpanStyle(color = Color.Transparent), range.first, range.last + 1)
        }
    }
}

// One rect per visual line, clamped to what actually survives maxLines + ellipsis.
private fun TextLayoutResult.spoilerRects(
    ranges: List<IntRange>,
    style: SpoilerStyle,
    density: Density
): List<Rect> {
    if (ranges.isEmpty()) return emptyList()

    val horizontalPadding = with(density) { style.horizontalPadding.toPx() }
    val verticalPadding = with(density) { style.verticalPadding.toPx() }
    val rects = mutableListOf<Rect>()

    ranges.forEach { range ->
        val endExclusive = range.last + 1
        var cursor = range.first
        while (cursor < endExclusive) {
            val line = getLineForOffset(cursor)
            if (line >= lineCount) break

            val visibleEnd = getLineEnd(line, visibleEnd = true)
            val lineEnd = min(endExclusive, min(getLineEnd(line), visibleEnd))
            if (lineEnd > cursor) {
                val bounds = getPathForRange(cursor, lineEnd).getBounds()
                if (bounds.width > 0f && bounds.height > 0f) {
                    rects += Rect(
                        left = bounds.left - horizontalPadding,
                        top = bounds.top - verticalPadding,
                        right = bounds.right + horizontalPadding,
                        bottom = bounds.bottom + verticalPadding
                    )
                }
            }

            val nextCursor = getLineEnd(line)
            if (nextCursor <= cursor) break
            cursor = nextCursor
        }
    }

    return rects
}

private fun wrapClock(value: Float): Float = if (value > 3600f) value - 3600f else value

internal class SpoilerParticlePool private constructor(size: Int, seed: Int) {
    val radiusFactor = FloatArray(size)
    val alpha = FloatArray(size)
    val lifetimeFactor = FloatArray(size)
    val phase = FloatArray(size)
    val directionX = FloatArray(size)
    val directionY = FloatArray(size)
    val count: Int get() = radiusFactor.size

    init {
        val random = Random(seed)
        repeat(size) { index ->
            val angle = random.nextFloat() * TWO_PI
            radiusFactor[index] = random.nextFloat()
            alpha[index] = 0.45f + random.nextFloat() * 0.55f
            lifetimeFactor[index] = random.nextFloat()
            phase[index] = random.nextFloat()
            directionX[index] = cos(angle)
            directionY[index] = sin(angle)
        }
    }

    // Deterministic, so every spoiler on screen can share one pool.
    companion object {
        private val cache = HashMap<Int, SpoilerParticlePool>()

        fun of(size: Int): SpoilerParticlePool = synchronized(cache) {
            cache.getOrPut(size) { SpoilerParticlePool(size = size, seed = 9137) }
        }
    }
}

private fun DrawScope.drawSpoilerDust(
    pool: SpoilerParticlePool,
    rect: Rect,
    color: Color,
    style: SpoilerStyle,
    time: Float,
    reveal: Float,
    origin: Offset
) {
    val widthDp = rect.width / density
    val heightDp = rect.height / density
    val target = (widthDp * heightDp * style.particleDensity).toInt()
    val particles = target.coerceIn(MIN_PARTICLES, pool.count)
    if (particles <= 0) return

    val minRadius = style.minRadius.toPx()
    val maxRadius = style.maxRadius.toPx()
    val drift = style.drift.toPx()
    val feather = style.feather.toPx().coerceAtLeast(0.01f)
    val burst = style.burstDistance.toPx()
    val spread = hypot(rect.width, rect.height).coerceAtLeast(1f)
    val padding = burst * reveal + maxRadius

    clipRect(
        left = rect.left - padding,
        top = rect.top - padding,
        right = rect.right + padding,
        bottom = rect.bottom + padding
    ) {
        val lifetimeSpan = (style.maxLifetime - style.minLifetime).coerceAtLeast(0.01f)

        for (index in 0 until particles) {
            val life = style.minLifetime + pool.lifetimeFactor[index] * lifetimeSpan
            val cursor = time * style.speed / life + pool.phase[index]
            val cycle = floor(cursor)
            val age = cursor - cycle

            val envelope = sin(age * PI.toFloat())
            var alpha = envelope * envelope * pool.alpha[index]
            if (alpha <= MIN_ALPHA) continue

            val spawnX = hash(index * 12.9898f + cycle * 78.233f)
            val spawnY = hash(index * 39.3468f + cycle * 11.135f)
            var x = rect.left + spawnX * rect.width + pool.directionX[index] * age * drift
            var y = rect.top + spawnY * rect.height + pool.directionY[index] * age * drift

            val edgeDistance = min(
                min(x - rect.left, rect.right - x),
                min(y - rect.top, rect.bottom - y)
            )
            alpha *= (edgeDistance / feather).coerceIn(0f, 1f)
            if (alpha <= MIN_ALPHA) continue

            var radius = lerp(minRadius, maxRadius, pool.radiusFactor[index]) * lerp(0.7f, 1.2f, envelope)

            if (reveal > 0f) {
                val distance = hypot(x - origin.x, y - origin.y) / spread
                val progress = ((reveal - distance * 0.35f) / 0.65f).coerceIn(0f, 1f)
                if (progress >= 1f) continue

                val dx = x - origin.x
                val dy = y - origin.y
                val length = hypot(dx, dy).coerceAtLeast(0.001f)
                x += dx / length * burst * progress
                y += dy / length * burst * progress
                radius *= 1f + progress * 0.8f
                alpha *= 1f - progress
            }

            drawCircle(
                color = color.copy(alpha = color.alpha * alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

private fun hash(value: Float): Float {
    val scaled = sin(value) * 43758.547f
    return scaled - floor(scaled)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

private const val TWO_PI = 6.2831855f
private const val MIN_PARTICLES = 24
private const val MIN_ALPHA = 0.02f
