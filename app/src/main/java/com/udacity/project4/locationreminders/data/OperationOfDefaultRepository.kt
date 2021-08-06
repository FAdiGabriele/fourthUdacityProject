package com.udacity.project4.locationreminders.data

import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

interface OperationOfDefaultRepository {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun getReminderById(reminderId: String): Result<ReminderDTO>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun deleteAllReminders()
}