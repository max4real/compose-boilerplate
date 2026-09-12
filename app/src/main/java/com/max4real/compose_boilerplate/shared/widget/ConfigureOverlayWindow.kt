package com.max4real.compose_boilerplate.shared.widget

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
fun ConfigureOverlayWindow(
    blurRadiusPx: Int
) {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window

    SideEffect {
        window?.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setDimAmount(0f)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            @Suppress("DEPRECATION")
            statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            navigationBarColor = android.graphics.Color.TRANSPARENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
                isStatusBarContrastEnforced = false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                val windowAttributes = attributes
                windowAttributes.blurBehindRadius = blurRadiusPx
                attributes = windowAttributes
            }
        }

        // Window was just resized to MATCH_PARENT above; without this, WindowInsets
        // composition locals can keep reporting stale (zero) insets from the dialog's
        // original small/centered size, causing content to clip under the system bars.
        view.requestApplyInsets()
    }

    DisposableEffect(window) {
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            }
        }
    }
}
