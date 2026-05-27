package com.konnecta.app.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DateUtilsTest {

    // Build a Date at noon on the given calendar date to avoid timezone edge cases.
    private fun dateOf(year: Int, month: Int, day: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun dayOfWeek(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    // ── getUpcomingFriday ────────────────────────────────────────────────────

    @Test
    fun `getUpcomingFriday always returns a Friday`() {
        assertEquals(Calendar.FRIDAY, dayOfWeek(DateUtils.getUpcomingFriday()))
    }

    // ── getFridayForDate ─────────────────────────────────────────────────────
    // Reference week: 2025-01-13 (Mon) … 2025-01-19 (Sun). Friday = 2025-01-17.

    @Test
    fun `getFridayForDate returns same date when input is Friday`() {
        val friday = dateOf(2025, 1, 17)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(friday)))
    }

    @Test
    fun `getFridayForDate returns preceding Friday when input is Saturday`() {
        val saturday = dateOf(2025, 1, 18)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(saturday)))
    }

    @Test
    fun `getFridayForDate returns preceding Friday when input is Sunday`() {
        val sunday = dateOf(2025, 1, 19)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(sunday)))
    }

    @Test
    fun `getFridayForDate advances to next Friday when input is Monday`() {
        val monday = dateOf(2025, 1, 13)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(monday)))
    }

    @Test
    fun `getFridayForDate advances to next Friday when input is Tuesday`() {
        val tuesday = dateOf(2025, 1, 14)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(tuesday)))
    }

    @Test
    fun `getFridayForDate advances to next Friday when input is Wednesday`() {
        val wednesday = dateOf(2025, 1, 15)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(wednesday)))
    }

    @Test
    fun `getFridayForDate advances to next Friday when input is Thursday`() {
        val thursday = dateOf(2025, 1, 16)
        assertEquals("2025-01-17", DateUtils.formatDbDate(DateUtils.getFridayForDate(thursday)))
    }

    // ── isWeekend ────────────────────────────────────────────────────────────

    @Test
    fun `isWeekend returns true for Friday`() {
        assertTrue(DateUtils.isWeekend(dateOf(2025, 1, 17)))
    }

    @Test
    fun `isWeekend returns true for Saturday`() {
        assertTrue(DateUtils.isWeekend(dateOf(2025, 1, 18)))
    }

    @Test
    fun `isWeekend returns true for Sunday`() {
        assertTrue(DateUtils.isWeekend(dateOf(2025, 1, 19)))
    }

    @Test
    fun `isWeekend returns false for Monday`() {
        assertFalse(DateUtils.isWeekend(dateOf(2025, 1, 13)))
    }

    @Test
    fun `isWeekend returns false for Thursday`() {
        assertFalse(DateUtils.isWeekend(dateOf(2025, 1, 16)))
    }

    // ── formatDbDate / parseDbDate ───────────────────────────────────────────

    @Test
    fun `formatDbDate produces yyyy-MM-dd string`() {
        assertEquals("2025-03-14", DateUtils.formatDbDate(dateOf(2025, 3, 14)))
    }

    @Test
    fun `parseDbDate round-trips with formatDbDate`() {
        val original = "2025-06-06"
        assertEquals(original, DateUtils.formatDbDate(DateUtils.parseDbDate(original)))
    }

    @Test
    fun `parseDbDate returns current Date without crashing on invalid input`() {
        val before = System.currentTimeMillis()
        val result = DateUtils.parseDbDate("not-a-date")
        val after = System.currentTimeMillis()
        assertTrue(result.time in before..after)
    }

    // ── addDays ──────────────────────────────────────────────────────────────

    @Test
    fun `addDays adds correct number of days`() {
        assertEquals("2025-01-15", DateUtils.formatDbDate(DateUtils.addDays(dateOf(2025, 1, 10), 5)))
    }

    @Test
    fun `addDays handles month boundary`() {
        assertEquals("2025-02-04", DateUtils.formatDbDate(DateUtils.addDays(dateOf(2025, 1, 30), 5)))
    }

    // ── getNextWeekends ──────────────────────────────────────────────────────

    @Test
    fun `getNextWeekends returns the requested count`() {
        assertEquals(5, DateUtils.getNextWeekends(5).size)
    }

    @Test
    fun `getNextWeekends defaults to 10`() {
        assertEquals(10, DateUtils.getNextWeekends().size)
    }

    @Test
    fun `getNextWeekends all entries are Fridays`() {
        DateUtils.getNextWeekends(8).forEach { dateStr ->
            val date = DateUtils.parseDbDate(dateStr)
            assertEquals("Expected Friday for $dateStr", Calendar.FRIDAY, dayOfWeek(date))
        }
    }

    @Test
    fun `getNextWeekends entries are exactly one week apart`() {
        val weekends = DateUtils.getNextWeekends(4)
        for (i in 1 until weekends.size) {
            val prev = Calendar.getInstance().apply {
                time = DateUtils.parseDbDate(weekends[i - 1])
                add(Calendar.DAY_OF_YEAR, 7)
            }
            assertEquals(weekends[i], DateUtils.formatDbDate(prev.time))
        }
    }

    @Test
    fun `getNextWeekends first entry matches getUpcomingFriday`() {
        val expected = DateUtils.formatDbDate(DateUtils.getUpcomingFriday())
        assertEquals(expected, DateUtils.getNextWeekends(1).first())
    }

    // ── getDayOfMonth / getMonthOfMonth ──────────────────────────────────────

    @Test
    fun `getDayOfMonth returns correct day`() {
        assertEquals(15, DateUtils.getDayOfMonth(dateOf(2025, 7, 15)))
    }

    @Test
    fun `getMonthOfMonth returns 1-based month`() {
        assertEquals(1, DateUtils.getMonthOfMonth(dateOf(2025, 1, 1)))
        assertEquals(12, DateUtils.getMonthOfMonth(dateOf(2025, 12, 1)))
    }
}
