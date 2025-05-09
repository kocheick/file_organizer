package com.shevapro.filesorter.model

import java.util.Date

data class UITaskRecord(
    val extension: String,
    val source: String,
    val destination: String,
    val isActive: Boolean = false,
    val isScheduled: Boolean = false,
    val scheduleTime: Date? = null,
    val scheduleType: ScheduleType = ScheduleType.ONCE,
    val errorMessage:String? = null,
    val id: Int = 0
) {
    fun toTaskRecord(): TaskRecord = TaskRecord(extension, source, destination, isActive, isScheduled, scheduleTime, scheduleType, id)

    companion object {
        val EMPTY_OBJECT = UITaskRecord("", "", "", id = 0)
    }
}