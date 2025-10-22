package com.example.habitrpg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.habitrpg.data.local.converters.ListConverter
import com.example.habitrpg.utils.DateUtils

// Базовый класс для всех типов задач
sealed class QuestType {
    abstract val displayName: String
}

// Типы задач
object HabitType : QuestType() {
    override val displayName: String = "Привычка"
}

object DailyType : QuestType() {
    override val displayName: String = "Ежедневная"
}

object TaskType : QuestType() {
    override val displayName: String = "Задача"
}

// Дни недели для ежедневных задач
enum class WeekDay(val displayName: String) {
    MONDAY("Пн"),
    TUESDAY("Вт"),
    WEDNESDAY("Ср"),
    THURSDAY("Чт"),
    FRIDAY("Пт"),
    SATURDAY("Сб"),
    SUNDAY("Вс")
}

// Сложность задач
enum class QuestDifficulty(
    val displayName: String,
    val expReward: Int,
    val goldReward: Int,
    val penaltyDamage: Int
) {
    EASY("Легкая", 10, 5, 5),
    MEDIUM("Средняя", 25, 15, 10),
    HARD("Сложная", 50, 30, 20),
    EPIC("Эпическая", 100, 60, 35)
}

@Entity
@TypeConverters(ListConverter::class)
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val questType: String, // "HABIT", "DAILY", "TASK"
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD, EPIC
    val tags: List<String> = emptyList(),
    val expReward: Int = 25,
    val goldReward: Int = 15,
    val penaltyDamage: Int = 10,

    // Общие поля
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // Поля для привычек
    val habitCounter: Int = 0,

    // Поля для ежедневных задач
    val weekDays: List<String> = emptyList(),
    val dailyCompleted: Boolean = false,
    val lastResetDate: Long = System.currentTimeMillis(), // Дата последнего сброса

    // Поля для одноразовых задач
    val deadline: Long? = null,
    val isOverdue: Boolean = false,
    val autoFailed: Boolean = false // Автопровал при дедлайне
) {
    // Вспомогательные методы
    fun getQuestTypeDisplayName(): String {
        return when (questType) {
            "HABIT" -> "Привычка"
            "DAILY" -> "Ежедневная"
            "TASK" -> "Задача"
            else -> questType
        }
    }

    fun getDifficultyDisplayName(): String {
        return when (difficulty) {
            "EASY" -> "Легкая"
            "MEDIUM" -> "Средняя"
            "HARD" -> "Сложная"
            "EPIC" -> "Эпическая"
            else -> difficulty
        }
    }

    // Проверка активности для ежедневных задач с учетом дня недели
    fun isActiveToday(): Boolean {
        if (questType != "DAILY") return true

        val today = DateUtils.getCurrentDayOfWeek()
        return weekDays.contains(today)
    }

    // Проверка необходимости сброса ежедневной задачи
    fun needsReset(): Boolean {
        if (questType != "DAILY") return false
        return DateUtils.isNewDay(lastResetDate)
    }

    // Получение статуса для отображения
    fun getDailyStatus(): String {
        if (questType != "DAILY") return ""

        return when {
            !isActiveToday() -> "сегодня не нужно"
            isCompleted -> "выполнено"
            isFailed -> "провалено"
            else -> "не выполнено"
        }
    }

    // Получение статуса дедлайна для отображения
    fun getDeadlineStatus(): String {
        if (deadline == null) return "без дедлайна"

        if (isCompleted) return "выполнено"
        if (isFailed) return "провалено"

        return when {
            isOverdue -> "просрочено"
            DateUtils.isTaskActiveToday(deadline) -> {
                val daysLeft = DateUtils.getDaysUntilDeadline(deadline)
                when (daysLeft) {
                    1 -> "сегодня последний день"
                    2 -> "завтра последний день"
                    else -> "осталось $daysLeft дней"
                }
            }
            else -> "просрочено"
        }
    }

    // Проверка, можно ли выполнить задачу (не провалена, не завершена и не просрочена)
    fun canBeCompleted(): Boolean {
        if (questType == "TASK" && deadline != null) {
            return !isCompleted && !isFailed && DateUtils.isTaskActiveToday(deadline)
        }
        return !isCompleted && !isFailed
    }

    // Проверка, можно ли провалить задачу (не завершена и активна)
    fun canBeFailed(): Boolean {
        if (questType == "TASK" && deadline != null) {
            return !isCompleted && DateUtils.isTaskActiveToday(deadline)
        }
        return !isCompleted
    }
}