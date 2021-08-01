package com.udacity.project4.locationreminders.data

import android.app.Application
import androidx.room.Room
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DefaultReminderRepository(
        private val reminderDataSource : ReminderDataSource,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : OperationOfDefaultRepository {


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return reminderDataSource.getReminders()
    }

    override suspend fun getReminderById(reminderId: String): Result<ReminderDTO> {
        return reminderDataSource.getReminder(reminderId)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        return reminderDataSource.saveReminder(reminder)
    }

    override suspend fun deleteAllReminders() {
        return reminderDataSource.deleteAllReminders()
    }
}