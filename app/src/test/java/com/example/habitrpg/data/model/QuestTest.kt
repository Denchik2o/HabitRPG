package com.example.habitrpg.data.model

import com.example.habitrpg.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class QuestTest {

    private fun createBasicQuest(
        questType: String = "TASK",
        difficulty: String = "MEDIUM",
        isCompleted: Boolean = false,
        isFailed: Boolean = false,
        weekDays: List<String> = emptyList(),
        deadline: Long? = null
    ): Quest {
        return Quest(
            title = "Test Quest",
            description = "Test Description",
            questType = questType,
            difficulty = difficulty,
            tags = emptyList(),
            expReward = 25,
            goldReward = 15,
            penaltyDamage = 10,
            isCompleted = isCompleted,
            isFailed = isFailed,
            createdAt = System.currentTimeMillis(),
            habitCounter = 0,
            weekDays = weekDays,
            dailyCompleted = false,
            lastResetDate = System.currentTimeMillis(),
            deadline = deadline,
            isOverdue = false,
            autoFailed = false
        )
    }

    @Test
    fun `quest should return correct display names for types`() {
        val habitQuest = createBasicQuest(questType = "HABIT")
        val dailyQuest = createBasicQuest(questType = "DAILY")
        val taskQuest = createBasicQuest(questType = "TASK")
        val unknownQuest = createBasicQuest(questType = "UNKNOWN")

        assertEquals("Привычка", habitQuest.getQuestTypeDisplayName())
        assertEquals("Ежедневная", dailyQuest.getQuestTypeDisplayName())
        assertEquals("Задача", taskQuest.getQuestTypeDisplayName())
        assertEquals("UNKNOWN", unknownQuest.getQuestTypeDisplayName())
    }

    @Test
    fun `quest should return correct display names for difficulties`() {
        val easyQuest = createBasicQuest(difficulty = "EASY")
        val mediumQuest = createBasicQuest(difficulty = "MEDIUM")
        val hardQuest = createBasicQuest(difficulty = "HARD")
        val epicQuest = createBasicQuest(difficulty = "EPIC")
        val unknownQuest = createBasicQuest(difficulty = "UNKNOWN")

        assertEquals("Легкая", easyQuest.getDifficultyDisplayName())
        assertEquals("Средняя", mediumQuest.getDifficultyDisplayName())
        assertEquals("Сложная", hardQuest.getDifficultyDisplayName())
        assertEquals("Эпическая", epicQuest.getDifficultyDisplayName())
        assertEquals("UNKNOWN", unknownQuest.getDifficultyDisplayName())
    }

    @Test
    fun `daily quest should check if active today`() {
        val today = DateUtils.getCurrentDayOfWeek()
        val dailyQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = listOf(today)
        )

        assertTrue(dailyQuest.isActiveToday())
    }

    @Test
    fun `daily quest should check if not active today`() {
        val today = DateUtils.getCurrentDayOfWeek()
        val otherDays = WeekDay.values().map { it.displayName }.filter { it != today }
        val dailyQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = otherDays
        )

        assertFalse(dailyQuest.isActiveToday())
    }

    @Test
    fun `non-daily quest should always be active`() {
        val habitQuest = createBasicQuest(questType = "HABIT")
        val taskQuest = createBasicQuest(questType = "TASK")

        assertTrue(habitQuest.isActiveToday())
        assertTrue(taskQuest.isActiveToday())
    }

    @Test
    fun `quest can be completed when not completed and not failed`() {
        val quest = createBasicQuest(isCompleted = false, isFailed = false)
        assertTrue(quest.canBeCompleted())
    }

    @Test
    fun `quest cannot be completed when already completed`() {
        val quest = createBasicQuest(isCompleted = true, isFailed = false)
        assertFalse(quest.canBeCompleted())
    }

    @Test
    fun `quest cannot be completed when failed`() {
        val quest = createBasicQuest(isCompleted = false, isFailed = true)
        assertFalse(quest.canBeCompleted())
    }

    @Test
    fun `task with deadline can be completed only when deadline not passed`() {
        val futureDeadline = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // Tomorrow
        val quest = createBasicQuest(
            questType = "TASK",
            deadline = futureDeadline,
            isCompleted = false,
            isFailed = false
        )

        assertTrue(quest.canBeCompleted())
    }

    @Test
    fun `daily quest status should return correct status`() {
        val today = DateUtils.getCurrentDayOfWeek()
        val activeQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = listOf(today),
            isCompleted = false,
            isFailed = false
        )

        val completedQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = listOf(today),
            isCompleted = true,
            isFailed = false
        )

        val failedQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = listOf(today),
            isCompleted = false,
            isFailed = true
        )

        assertEquals("не выполнено", activeQuest.getDailyStatus())
        assertEquals("выполнено", completedQuest.getDailyStatus())
        assertEquals("провалено", failedQuest.getDailyStatus())
    }

    @Test
    fun `daily quest status should return today not needed when not active`() {
        val today = DateUtils.getCurrentDayOfWeek()
        val otherDays = WeekDay.values().map { it.displayName }.filter { it != today }
        val inactiveQuest = createBasicQuest(
            questType = "DAILY",
            weekDays = otherDays,
            isCompleted = false,
            isFailed = false
        )

        assertEquals("сегодня не нужно", inactiveQuest.getDailyStatus())
    }

    @Test
    fun `quest with deadline should return correct deadline status`() {
        val futureDeadline = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000 // 2 days from now
        val quest = createBasicQuest(
            questType = "TASK",
            deadline = futureDeadline,
            isCompleted = false,
            isFailed = false
        )

        val status = quest.getDeadlineStatus()
        assertTrue(status.contains("осталось") || status.contains("дней"))
    }

    @Test
    fun `completed task with deadline should show completed status`() {
        val futureDeadline = System.currentTimeMillis() + 24 * 60 * 60 * 1000
        val quest = createBasicQuest(
            questType = "TASK",
            deadline = futureDeadline,
            isCompleted = true,
            isFailed = false
        )

        assertEquals("выполнено", quest.getDeadlineStatus())
    }

    @Test
    fun `failed task with deadline should show failed status`() {
        val futureDeadline = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        val quest = createBasicQuest(
            questType = "TASK",
            deadline = futureDeadline,
            isCompleted = false,
            isFailed = false
        )

        assertEquals("просрочено", quest.getDeadlineStatus())
    }

    @Test
    fun `habit counter should be accessible`() {
        val habitQuest = createBasicQuest(questType = "HABIT")
        assertEquals(0, habitQuest.habitCounter)
    }

    @Test
    fun `quest needs reset for daily when last reset was yesterday`() {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        val dailyQuest = createBasicQuest(questType = "DAILY").copy(
            lastResetDate = yesterday
        )

        assertTrue(dailyQuest.needsReset())
    }

    @Test
    fun `quest should not need reset for non-daily types`() {
        val habitQuest = createBasicQuest(questType = "HABIT")
        val taskQuest = createBasicQuest(questType = "TASK")

        assertFalse(habitQuest.needsReset())
        assertFalse(taskQuest.needsReset())
    }
}