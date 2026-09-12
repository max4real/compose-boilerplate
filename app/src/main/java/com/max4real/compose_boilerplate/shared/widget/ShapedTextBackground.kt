package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextLayoutResult

/**
 * Connects a list of top-to-bottom line rects into one continuous outline — walks down the right
 * edges (stepping in and out where consecutive lines change width), then back up the left edges —
 * instead of drawing a rectangle per line. Rounded by `PathEffect.cornerPathEffect` at the draw
 * site rather than by hand-computed curves, so the steps between lines round off too.
 *
 * Ported from exyte/ShapedBackgroundAndroid's `createBackgroundPath` and shared by the chat link
 * chip and the story text plate. **Rects must be contiguous** — each line's bottom is the next
 * line's top — or the outline will cross itself; [textLineRects] builds them that way.
 */
fun buildShapedBackgroundPath(rects: List<Rect>): Path {
    val path = Path()
    if (rects.isEmpty()) return path
    if (rects.size == 1) {
        path.addRect(rects.first())
        return path
    }

    rects.forEachIndexed { index, r ->
        val next = rects.getOrNull(index + 1) ?: r
        if (index == 0) {
            path.moveTo(r.left, r.top)
            path.lineTo(r.right, r.top)
            val bottom = when {
                next.right > r.right -> next.top
                next.right < r.right -> r.bottom
                else -> r.bottom
            }
            path.lineTo(r.right, bottom)
        } else {
            val prev = rects[index - 1]
            val top = when {
                r.right > prev.right -> r.top
                r.right < prev.right -> prev.bottom
                else -> prev.bottom
            }
            val bottom = when {
                r.right > next.right -> r.bottom
                r.right < next.right -> next.top
                else -> r.bottom
            }
            path.lineTo(r.right, top)
            path.lineTo(r.right, bottom)
        }
    }
    for (index in rects.lastIndex downTo 0) {
        val r = rects[index]
        val prev = rects.getOrNull(index - 1) ?: r
        val next = rects.getOrNull(index + 1) ?: r
        val top = when {
            r.left > prev.left -> prev.bottom
            r.left < prev.left -> r.top
            else -> r.top
        }
        val bottom = when {
            r.left < next.left -> r.bottom
            r.left > next.left -> next.top
            else -> r.bottom
        }
        path.lineTo(r.left, bottom)
        path.lineTo(r.left, top)
    }
    path.close()
    return path
}

/**
 * One rect per laid-out line, in the coordinates of a box that pads the text by
 * [horizontalPadding] / [verticalPadding] on every side.
 *
 * The padding is added **outwards on the block's ends only** — the first line's top and the last
 * line's bottom — so consecutive lines still meet exactly. Padding every line would overlap them
 * and the outline would fold back through itself.
 */
fun textLineRects(
    layout: TextLayoutResult,
    horizontalPadding: Float,
    verticalPadding: Float,
): List<Rect> = (0 until layout.lineCount).map { line ->
    Rect(
        left = layout.getLineLeft(line),
        top = layout.getLineTop(line) + if (line == 0) 0f else verticalPadding,
        right = layout.getLineRight(line) + horizontalPadding * 2f,
        bottom = layout.getLineBottom(line) +
                if (line == layout.lineCount - 1) verticalPadding * 2f else verticalPadding
    )
}
