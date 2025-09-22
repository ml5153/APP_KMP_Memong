package com.avatye.haru.memo.data.utils

import android.content.Context
import android.widget.Toast
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class Util {
    companion object {
        fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        fun toastShort(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun toastLong(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun formatDate(timestamp: Long): String {
            val locale = Locale.getDefault()
            val sdf = SimpleDateFormat("yy.MM.dd", locale)
            return sdf.format(Date(timestamp))
        }

        fun parseToInstant(dateStr: String?): Instant? {
            if (dateStr.isNullOrBlank()) return null

            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val localDateTime = LocalDateTime.parse(dateStr, formatter)
                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
            } catch (e: Exception) {
                null
            }
        }
    }


    // 첫번째 text

}