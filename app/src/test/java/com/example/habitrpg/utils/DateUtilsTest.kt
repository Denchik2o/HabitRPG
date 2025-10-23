package com.example.habitrpg.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateUtilsTest {

    @Test
    fun `getStartOfDay should return beginning of current day`() {
        val startOfDay = DateUtils.getStartOfDay()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startOfDay
        }

        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getEndOfDay should return end of current day`() {
        val endOfDay = DateUtils.getEndOfDay()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = endOfDay
        }

        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `isNewDay should return true for old timestamp`() {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000

        val isNew = DateUtils.isNewDay(yesterday)

        assertTrue(isNew)
    }

    @Test
    fun `isNewDay should return false for today's timestamp`() {
        val today = DateUtils.getStartOfDay()

        val isNew = DateUtils.isNewDay(today)

        assertFalse(isNew)
    }

    @Test
    fun `isDeadlinePassed should correctly identify passed deadlines`() {
        val pastDeadline = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        val futureDeadline = System.currentTimeMillis() + 24 * 60 * 60 * 1000

        assertTrue(DateUtils.isDeadlinePassed(pastDeadline))
        assertFalse(DateUtils.isDeadlinePassed(futureDeadline))
    }

    @Test
    fun `getDaysUntilDeadline should calculate correct days`() {
        val twoDaysFromNow = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000

        val daysLeft = DateUtils.getDaysUntilDeadline(twoDaysFromNow)

        assertTrue(daysLeft >= 1 && daysLeft <= 3)
    }
}