package com.example.stayfree.domain

import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import com.example.stayfree.domain.model.BlockType

data class BlockDecision(
    val reason: String,
    val ruleId: Long
)

/**
 * Pure rule evaluation — no clock, no Android, no IO. The service gathers the
 * inputs (rules, schedules, persisted usage, current session) and this decides
 * whether the foreground package must be blocked right now.
 */
object BlockRuleEvaluator {

    const val SLEEP_MODE_PACKAGE = "__sleep_mode__"

    /**
     * @param rules         active rules for the foreground package, plus any global
     *                      sleep-mode sentinel rules
     * @param schedules     schedules per rule id (only needed for SCHEDULED/SLEEP)
     * @param usageTodayMs  persisted foreground time for the package today
     * @param sessionMs     length of the current continuous foreground session
     * @param nowMinutes    minutes from midnight
     * @param day           current day abbreviation (MON..SUN)
     */
    fun evaluate(
        rules: List<BlockRuleEntity>,
        schedules: Map<Long, List<ScheduleEntity>>,
        usageTodayMs: Long,
        sessionMs: Long,
        nowMinutes: Int,
        day: String
    ): BlockDecision? {
        for (rule in rules) {
            when (rule.blockType) {
                BlockType.BLOCK_NOW.name -> return BlockDecision(rule.blockType, rule.id)

                BlockType.SCHEDULED.name, BlockType.SLEEP.name -> {
                    val windows = schedules[rule.id].orEmpty()
                    if (windows.any { isInWindow(it.daysOfWeek, it.startTimeMinutes, it.endTimeMinutes, nowMinutes, day) }) {
                        return BlockDecision(rule.blockType, rule.id)
                    }
                }

                BlockType.DAILY_LIMIT.name -> {
                    val limit = rule.dailyLimitMs ?: continue
                    if (usageTodayMs + sessionMs >= limit) {
                        return BlockDecision(rule.blockType, rule.id)
                    }
                }

                BlockType.SESSION.name -> {
                    val limit = rule.sessionLimitMs ?: continue
                    if (sessionMs >= limit) {
                        return BlockDecision(rule.blockType, rule.id)
                    }
                }
            }
        }
        return null
    }

    /** Window check with injected time; handles overnight windows (23:00–07:00). */
    fun isInWindow(
        daysOfWeek: String,
        startMinutes: Int,
        endMinutes: Int,
        nowMinutes: Int,
        day: String
    ): Boolean {
        val days = daysOfWeek.split(",").map { it.trim() }
        return if (startMinutes <= endMinutes) {
            day in days && nowMinutes in startMinutes until endMinutes
        } else {
            (day in days && nowMinutes >= startMinutes) ||
                    (previousDay(day) in days && nowMinutes < endMinutes)
        }
    }

    private fun previousDay(day: String): String {
        val order = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val idx = order.indexOf(day)
        return order[(idx - 1 + 7) % 7]
    }
}
