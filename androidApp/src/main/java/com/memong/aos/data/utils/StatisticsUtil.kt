package com.memong.aos.data.utils

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object StatisticsUtil {

    @SuppressLint("HardwareIds")
    fun trackStatistics(context: Context) {
        val calendar = Calendar.getInstance()

        // 일 단위 (DAU)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
        val lastDau = PreferenceUtil.get(PreferenceUtil.KEY_LAST_DAU_DATE, "")
        if (lastDau != today) {
            EventUtil.sendEvent(
                context,
                EventUtil.CATEGORY_STATISTICS,
                EventUtil.ACTION_DAU
            )
            PreferenceUtil.set(PreferenceUtil.KEY_LAST_DAU_DATE, today)
        }

        // 주 단위 (WAU) - 연도 + 주차
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val currentWeekKey = "$year-$weekOfYear"
        val lastWau = PreferenceUtil.get(PreferenceUtil.KEY_LAST_WAU_WEEK, "")
        if (lastWau != currentWeekKey) {
            EventUtil.sendEvent(
                context,
                EventUtil.CATEGORY_STATISTICS,
                EventUtil.ACTION_WAU
            )
            PreferenceUtil.set(PreferenceUtil.KEY_LAST_WAU_WEEK, currentWeekKey)
        }

        // 월 단위 (MAU) - 연도 + 월
        val monthKey = "$year-${calendar.get(Calendar.MONTH) + 1}"
        val lastMau = PreferenceUtil.get(PreferenceUtil.KEY_LAST_MAU_MONTH, "")
        if (lastMau != monthKey) {
            EventUtil.sendEvent(
                context,
                EventUtil.CATEGORY_STATISTICS,
                EventUtil.ACTION_MAU
            )
            PreferenceUtil.set(PreferenceUtil.KEY_LAST_MAU_MONTH, monthKey)
        }
    }
}