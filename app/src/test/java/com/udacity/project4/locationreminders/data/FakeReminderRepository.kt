package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Used for testing the real viewmodels
class FakeReminderRepository : ReminderDataSource {

    private val fakeReminder = ReminderDTO("fake1", "description1", "place1", 1.0, 1.0, "1")
    private val fakeReminder2 = ReminderDTO("fake2", "description2", "place2", 1.0, 1.0, "2")

    val reminderList = ArrayList<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        if (shouldReturnError) {
//            return Result.Error("test Exception")
//        }
        return Result.Success(reminderList.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //        if (shouldReturnError) {
//            return Result.Error("test Exception")
//        }
        val result = reminderList.filter { it.id == id }
        return if(result.isNotEmpty())
            Result.Success(result[0])
        else
            Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
       reminderList.clear()
    }

    override suspend fun deleteReminder(reminder: ReminderDTO) {
        reminderList.remove(reminder)
    }

    fun addSomeFakeData(){
        reminderList.add(fakeReminder)
        reminderList.add(fakeReminder2)
    }

}