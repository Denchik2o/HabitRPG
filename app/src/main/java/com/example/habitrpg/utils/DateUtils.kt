package com.example.habitrpg.utils

import java.util.*

object DateUtils {

    // Получение начала текущего дня (00:00:00)
    fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Получение конца текущего дня (23:59:59.999)
    fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Получение конца указанного дня (23:59:59.999)
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Проверка, является ли день новым (сравниваем с последним сбросом)
    fun isNewDay(lastResetTime: Long): Boolean {
        return lastResetTime < getStartOfDay()
    }

    // Получение текущего дня недели
    fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Пн"
            Calendar.TUESDAY -> "Вт"
            Calendar.WEDNESDAY -> "Ср"
            Calendar.THURSDAY -> "Чт"
            Calendar.FRIDAY -> "Пт"
            Calendar.SATURDAY -> "Сб"
            Calendar.SUNDAY -> "Вс"
            else -> "Пн"
        }
    }

    // Проверка просрочки дедлайна (учитываем весь день дедлайна)
    fun isDeadlinePassed(deadline: Long): Boolean {
        // Если дедлайн установлен на конкретную дату (без времени),
        // считаем что задача активна до конца этого дня
        val endOfDeadlineDay = getEndOfDay(deadline)
        return System.currentTimeMillis() > endOfDeadlineDay
    }

    // Проверка, активна ли задача сегодня (для отображения статуса)
    fun isTaskActiveToday(deadline: Long?): Boolean {
        if (deadline == null) return true

        val now = System.currentTimeMillis()
        val endOfDeadlineDay = getEndOfDay(deadline)
        return now <= endOfDeadlineDay
    }

    // Получение оставшегося времени до дедлайна (в днях)
    fun getDaysUntilDeadline(deadline: Long): Int {
        val now = System.currentTimeMillis()
        val endOfDeadlineDay = getEndOfDay(deadline)

        if (now > endOfDeadlineDay) return 0

        val diff = endOfDeadlineDay - now
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1 // +1 потому что сегодняшний день тоже считается
    }
}