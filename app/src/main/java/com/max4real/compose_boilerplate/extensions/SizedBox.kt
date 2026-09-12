package com.max4real.compose_boilerplate.extensions

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun Int.WidthBox() {
    Spacer(modifier = Modifier.width(this.dp))
}

@Composable
fun Int.HeightBox() {
    Spacer(modifier = Modifier.height(this.dp))
}

@Composable
fun Double.WidthBox() {
    Spacer(modifier = Modifier.width(this.dp))
}

@Composable
fun Double.HeightBox() {
    Spacer(modifier = Modifier.height(this.dp))
}


fun Modifier.ignorePointerWhen(
    ignorePointer: Boolean
): Modifier {
    return if (!ignorePointer) {
        this
    } else {
        pointerInput(Unit) {
            awaitEachGesture {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    event.changes.forEach { it.consume() }
                    if (event.changes.none { it.pressed }) break
                }
            }
        }
    }
}
