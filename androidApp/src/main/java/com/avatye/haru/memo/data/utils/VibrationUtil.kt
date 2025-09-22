package com.avatye.haru.memo.data.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibrationUtil {

    /**
     * 단일 진동: duration(지속 시간)과 amplitude(강도)를 조절할 수 있음
     * @param duration 진동 지속 시간 (밀리초)
     * @param amplitude 강도 (1~255), 255는 최대. DEFAULT_AMPLITUDE 사용 가능
     */

    fun vibrate(context: Context, duration: Long = 30L, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(duration)
        }
    }

    /**
     * 진동 패턴 실행
     * @param pattern 진동-정지 반복 시간 배열 (ms)
     * @param repeat 반복 여부, -1이면 반복 없음
     */
    fun vibratePattern(context: Context, pattern: LongArray, repeat: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, repeat)
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            }
        }
    }
}