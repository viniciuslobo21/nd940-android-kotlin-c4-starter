package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.*
import kotlin.collections.ArrayList

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    var listRemindersDTO: MutableList<ReminderDTO>? = mutableListOf()
) : ReminderDataSource {

    var reminderDTOServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Error get all reminders")
        }
        listRemindersDTO?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("No reminder found ")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        listRemindersDTO?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminderDTOServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find reminder")
    }

    override suspend fun deleteAllReminders() {
        listRemindersDTO?.clear()
    }
}