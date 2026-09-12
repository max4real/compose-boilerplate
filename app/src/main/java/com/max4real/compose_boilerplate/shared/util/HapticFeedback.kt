package com.max4real.compose_boilerplate.shared.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


object CustomHaptic {

    fun doubleVeryLightImpact(context: Context) {
        val timings = longArrayOf(0, 16, 32, 14)
        val amplitudes = intArrayOf(0, 48, 0, 66)

        vibrateWaveform(context, timings, amplitudes)
    }

    fun doubleLightImpact(context: Context) {
        val timings = longArrayOf(0, 40, 50, 20)
        val amplitudes = intArrayOf(0, 600, 0, 200)

        vibrateWaveform(context, timings, amplitudes)
    }

    fun veryLightImpact(context: Context) {
        vibrate(
            context = context,
            durationMs = 10,
            amplitude = 35,
            predefinedEffect = PredefinedEffect.Tick
        )
    }

    fun lightImpact(context: Context) {
        vibrate(
            context = context,
            durationMs = 20,
            amplitude = 40,
            predefinedEffect = PredefinedEffect.Tick
        )
    }

    fun mediumImpact(context: Context) {
        vibrate(
            context = context,
            durationMs = 35,
            amplitude = 90,
            predefinedEffect = PredefinedEffect.Click
        )
    }

    fun heavyImpact(context: Context) {
        vibrate(
            context = context,
            durationMs = 50,
            amplitude = 160,
            predefinedEffect = PredefinedEffect.HeavyClick
        )
    }

    fun selectionClick(context: Context) {
        vibrate(
            context = context,
            durationMs = 10,
            amplitude = 30,
            predefinedEffect = PredefinedEffect.Tick
        )
    }

    private fun vibrate(
        context: Context,
        durationMs: Long,
        amplitude: Int,
        predefinedEffect: PredefinedEffect
    ) {
        val vibrator = getVibrator(context) ?: return

        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(predefinedEffect.effectId)
                )
                return
            }

            val supportedAmplitude = if (vibrator.hasAmplitudeControl()) {
                amplitude.coerceIn(1, 255)
            } else {
                VibrationEffect.DEFAULT_AMPLITUDE
            }

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    supportedAmplitude
                )
            )
        } catch (_: RuntimeException) {
            // Some OEM ROMs reject specific haptic effects. Keep the tap action safe.
        }
    }

    private fun vibrateWaveform(
        context: Context,
        timings: LongArray,
        amplitudes: IntArray
    ) {
        val vibrator = getVibrator(context) ?: return

        if (!vibrator.hasVibrator()) return

        try {
            val supportedAmplitudes = if (vibrator.hasAmplitudeControl()) {
                amplitudes
            } else {
                amplitudes.map { amplitude ->
                    if (amplitude == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE
                }.toIntArray()
            }

            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    timings,
                    supportedAmplitudes,
                    -1
                )
            )
        } catch (_: RuntimeException) {
            // Ignore unsupported haptic requests on devices with partial vibration support.
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager

            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private enum class PredefinedEffect(val effectId: Int) {
        Click(0),
        HeavyClick(5),
        Tick(2)
    }
}
