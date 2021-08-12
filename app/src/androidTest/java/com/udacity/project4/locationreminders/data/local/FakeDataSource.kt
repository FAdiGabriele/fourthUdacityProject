package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Used for testing the real repository
class FakeDataSource(val reminderList: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminderList?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("List of reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminderList?.let {  list ->

            val filteredList = list.filter { singleReminder ->
                singleReminder.id == id
            }

            return if(filteredList.isNotEmpty()){
                Result.Success(filteredList[0])
            }else{
                Result.Error("Reminder not found")
            }
        } ?: return Result.Error("List of reminders not found")
    }

    override suspend fun deleteAllReminders() {
        reminderList?.clear()
    }

    override suspend fun deleteReminder(reminder: ReminderDTO) {
        reminderList?.remove(reminder)
    }


}