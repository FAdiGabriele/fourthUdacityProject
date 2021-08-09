package com.udacity.project4.locationreminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants
import kotlinx.coroutines.launch

class CommonViewModel(private val dataSource: ReminderDataSource) : ViewModel() {

    private val  _geoFencerObserver = MutableLiveData<GeofencingRequest>()
    val geoFencerObserver : LiveData<GeofencingRequest>
        get() = _geoFencerObserver

    fun createGeoFenceRequest(reminderData: ReminderDataItem) {

        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(reminderData.latitude!!,
                reminderData.longitude!!,
                Constants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        Log.v(Constants.GEOFENCE_TAG, "Created Geofence with id ${geofence.requestId}")

        _geoFencerObserver.value = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    fun removeReminderData (reminderData: ReminderDataItem){
        viewModelScope.launch {
            dataSource.deleteReminder(reminderData.turnInReminderDTO())
        }

    }


}