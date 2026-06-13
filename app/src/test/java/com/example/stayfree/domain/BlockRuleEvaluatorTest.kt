package com.example.stayfree.domain

import com.example.stayfree.data.local.entity.BlockRuleEntity
import com.example.stayfree.data.local.entity.ScheduleEntity
import com.example.stayfree.domain.model.BlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BlockRuleEvaluatorTest {

    private fun rule(
        id: Long,
        type: BlockType,
        dailyLimitMs: Long? = null,
        sessionLimitMs: Long? = null,
        pkg: String = "com.example.target"
    ) = BlockRuleEntity(
        id = id,
        packageName = pkg,
        blockType = type.name,
        dailyLimitMs = dailyLimitMs,
        sessionLimitMs = sessionLimitMs
    )

    private fun schedule(ruleId: Long, days: String, start: Int, end: Int) =
        ScheduleEntity(id = ruleId * 100, blockRuleId = ruleId, daysOfWeek = days, startTimeMinutes = start, endTimeMinutes = end)

    @Test
    fun `BLOCK_NOW always blocks`() {
        val decision = BlockRuleEvaluator.evaluate(
            rules = listOf(rule(1, BlockType.BLOCK_NOW)),
            schedules = emptyMap(),
            usageTodayMs = 0, sessionMs = 0, nowMinutes = 600, day = "MON"
        )
        assertEquals(BlockType.BLOCK_NOW.name, decision?.reason)
    }

    @Test
    fun `SCHEDULED blocks inside the window and not outside`() {
        val r = rule(1, BlockType.SCHEDULED)
        val schedules = mapOf(1L to listOf(schedule(1, "MON,TUE", 540, 1020)))

        assertNotNull(BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 600, day = "MON"))
        assertNull(BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 600, day = "FRI"))
        assertNull(BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 1100, day = "MON"))
    }

    @Test
    fun `SLEEP sentinel rule blocks during an overnight window`() {
        val r = rule(1, BlockType.SLEEP, pkg = BlockRuleEvaluator.SLEEP_MODE_PACKAGE)
        val schedules = mapOf(1L to listOf(schedule(1, "MON,TUE,WED,THU,FRI,SAT,SUN", 1380, 420)))

        // 23:30 same day
        assertEquals(
            BlockType.SLEEP.name,
            BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 1410, day = "MON")?.reason
        )
        // 02:00 next morning
        assertNotNull(BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 120, day = "TUE"))
        // mid-day: not blocked
        assertNull(BlockRuleEvaluator.evaluate(listOf(r), schedules, 0, 0, nowMinutes = 720, day = "TUE"))
    }

    @Test
    fun `DAILY_LIMIT counts persisted usage plus the current session`() {
        val r = rule(1, BlockType.DAILY_LIMIT, dailyLimitMs = 60 * 60_000L) // 1h

        // 50 min persisted + 5 min session: below limit
        assertNull(BlockRuleEvaluator.evaluate(listOf(r), emptyMap(), 50 * 60_000L, 5 * 60_000L, 600, "MON"))
        // 50 min persisted + 10 min session: limit reached
        assertNotNull(BlockRuleEvaluator.evaluate(listOf(r), emptyMap(), 50 * 60_000L, 10 * 60_000L, 600, "MON"))
    }

    @Test
    fun `DAILY_LIMIT with null limit is skipped`() {
        val r = rule(1, BlockType.DAILY_LIMIT, dailyLimitMs = null)
        assertNull(BlockRuleEvaluator.evaluate(listOf(r), emptyMap(), Long.MAX_VALUE / 2, 0, 600, "MON"))
    }

    @Test
    fun `SESSION blocks once the continuous session exceeds the limit`() {
        val r = rule(1, BlockType.SESSION, sessionLimitMs = 15 * 60_000L)

        assertNull(BlockRuleEvaluator.evaluate(listOf(r), emptyMap(), 0, 14 * 60_000L, 600, "MON"))
        assertNotNull(BlockRuleEvaluator.evaluate(listOf(r), emptyMap(), 0, 15 * 60_000L, 600, "MON"))
    }

    @Test
    fun `multi-rule regression - an unmet DAILY_LIMIT must not short-circuit later rules`() {
        // The old service implementation returned `used >= limit` directly from
        // the DAILY_LIMIT branch, so a later BLOCK_NOW rule was never evaluated.
        val rules = listOf(
            rule(1, BlockType.DAILY_LIMIT, dailyLimitMs = 60 * 60_000L),
            rule(2, BlockType.BLOCK_NOW)
        )
        val decision = BlockRuleEvaluator.evaluate(rules, emptyMap(), 0, 0, 600, "MON")
        assertEquals(BlockType.BLOCK_NOW.name, decision?.reason)
        assertEquals(2L, decision?.ruleId)
    }

    @Test
    fun `no rules - no decision`() {
        assertNull(BlockRuleEvaluator.evaluate(emptyList(), emptyMap(), 0, 0, 600, "MON"))
    }
}
