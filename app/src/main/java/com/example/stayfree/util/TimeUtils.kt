package com.example.stayfree.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Returns the "effective date" string based on the user-configured daily reset time.
     * If the current time is before today's reset, we're still in "yesterday's day".
     */
    fun getEffectiveDate(resetTimeMinutes: Int): String {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, resetTimeMinutes / 60)
        calendar.set(Calendar.MINUTE, resetTimeMinutes % 60)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayResetMs = calendar.timeInMillis
        return if (now < todayResetMs) {
            dateFormat.format(Date(todayResetMs - 86_400_000L))
        } else {
            dateFormat.format(Date(now))
        }
    }

    fun getTodayString(): String = dateFormat.format(Date())

    fun getDateString(epochMs: Long): String = dateFormat.format(Date(epochMs))

    fun getDateStringDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    /** Format milliseconds to human-readable "Xh Ym" or "Ym Zs" */
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /** Returns minutes from midnight for the current time */
    fun currentTimeMinutes(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    /** Returns the current day-of-week abbreviation (MON, TUE, ...) */
    fun currentDayAbbreviation(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"
        }
    }

    /**
     * Checks whether the current time falls within a schedule window.
     * Handles overnight schedules (e.g. 23:00 – 07:00).
     */
    fun isInScheduleWindow(daysOfWeek: String, startMinutes: Int, endMinutes: Int): Boolean {
        val days = daysOfWeek.split(",").map { it.trim() }
        val currentDay = currentDayAbbreviation()
        val now = currentTimeMinutes()

        if (startMinutes <= endMinutes) {
            // Same day window
            return currentDay in days && now in startMinutes until endMinutes
        } else {
            // Overnight window — e.g. 23:00 (1380) to 07:00 (420)
            val prevDay = getPreviousDayAbbreviation(currentDay)
            return (currentDay in days && now >= startMinutes) ||
                    (prevDay in days && now < endMinutes)
        }
    }

    private fun getPreviousDayAbbreviation(day: String): String {
        val order = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val idx = order.indexOf(day)
        return order[(idx - 1 + 7) % 7]
    }
}
