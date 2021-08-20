package com.udacity.project4.locationreminders

import android.app.Application
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class CommonViewModel(val app: Application, private val dataSource: ReminderDataSource) : BaseViewModel(app) {

    var lastLocation : Location? = null
    val locationRequestedAndApproved = MutableLiveData<Boolean>()


    fun removeReminderData (reminderData: ReminderDataItem){
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.turnInReminderDTO())
        }

    }

}