package com.udacity.project4.locationreminders

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class CommonViewModel(private val dataSource: ReminderDataSource) : ViewModel() {

    var lastLocation : Location? = null
    val locationRequestedAndApproved = MutableLiveData<Boolean>()


    fun removeReminderData (reminderData: ReminderDataItem){
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.turnInReminderDTO())
        }

    }

}