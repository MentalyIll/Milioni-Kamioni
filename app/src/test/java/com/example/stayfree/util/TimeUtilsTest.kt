package com.example.stayfree.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    private fun timeAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    // --- getEffectiveDate: reset boundary ---

    @Test
    fun `midnight reset - any time of day maps to calendar date`() {
        val now = timeAt(2026, 6, 13, 10, 0)
        assertEquals("2026-06-13", TimeUtils.getEffectiveDate(0, now))
    }

    @Test
    fun `custom reset - before reset time still counts as yesterday`() {
        // Reset at 03:00; at 01:30 we are still in the 2026-06-12 "day".
        val now = timeAt(2026, 6, 13, 1, 30)
        assertEquals("2026-06-12", TimeUtils.getEffectiveDate(180, now))
    }

    @Test
    fun `custom reset - after reset time counts as today`() {
        val now = timeAt(2026, 6, 13, 4, 0)
        assertEquals("2026-06-13", TimeUtils.getEffectiveDate(180, now))
    }

    @Test
    fun `custom reset - exactly at reset time counts as today`() {
        val now = timeAt(2026, 6, 13, 3, 0)
        assertEquals("2026-06-13", TimeUtils.getEffectiveDate(180, now))
    }

    // --- isInScheduleWindow: same-day windows ---

    @Test
    fun `same-day window - inside`() {
        assertTrue(TimeUtils.isInScheduleWindow("MON,TUE", 540, 1020, now = 600, currentDay = "MON"))
    }

    @Test
    fun `same-day window - wrong day`() {
        assertFalse(TimeUtils.isInScheduleWindow("MON", 540, 1020, now = 600, currentDay = "WED"))
    }

    @Test
    fun `same-day window - end is exclusive`() {
        assertFalse(TimeUtils.isInScheduleWindow("MON", 540, 1020, now = 1020, currentDay = "MON"))
    }

    // --- isInScheduleWindow: overnight windows (23:00 - 07:00) ---

    @Test
    fun `overnight window - evening side of the scheduled day`() {
        assertTrue(TimeUtils.isInScheduleWindow("MON", 1380, 420, now = 1400, currentDay = "MON"))
    }

    @Test
    fun `overnight window - morning side belongs to the previous day's schedule`() {
        // Window scheduled for MON 23:00-07:00; Tuesday 02:00 is still inside it.
        assertTrue(TimeUtils.isInScheduleWindow("MON", 1380, 420, now = 120, currentDay = "TUE"))
    }

    @Test
    fun `overnight window - outside on both sides`() {
        assertFalse(TimeUtils.isInScheduleWindow("MON", 1380, 420, now = 720, currentDay = "TUE"))
        assertFalse(TimeUtils.isInScheduleWindow("MON", 1380, 420, now = 720, currentDay = "MON"))
    }

    // --- formatDuration ---

    @Test
    fun `formatDuration formats hours minutes seconds`() {
        assertEquals("2h 5m", TimeUtils.formatDuration(2 * 3_600_000L + 5 * 60_000L))
        assertEquals("5m 30s", TimeUtils.formatDuration(5 * 60_000L + 30_000L))
        assertEquals("45s", TimeUtils.formatDuration(45_000L))
    }
}
