package com.example.stayfree.domain.model

data class Schedule(
    val id: Long,
    val blockRuleId: Long,
    val daysOfWeek: List<DayOfWeek>,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int
)

enum class DayOfWeek(val abbreviation: String) {
    MON("MON"), TUE("TUE"), WED("WED"), THU("THU"),
    FRI("FRI"), SAT("SAT"), SUN("SUN");

    companion object {
        fun fromAbbreviation(abbr: String) = entries.first { it.abbreviation == abbr }
        fun fromCalendarDay(calDay: Int): DayOfWeek = when (calDay) {
            java.util.Calendar.MONDAY -> MON
            java.util.Calendar.TUESDAY -> TUE
            java.util.Calendar.WEDNESDAY -> WED
            java.util.Calendar.THURSDAY -> THU
            java.util.Calendar.FRIDAY -> FRI
            java.util.Calendar.SATURDAY -> SAT
            java.util.Calendar.SUNDAY -> SUN
            else -> MON
        }
    }
}
