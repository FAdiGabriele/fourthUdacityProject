package com.udacity.project4.locationreminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants

class CommonViewModel(application: Application) : AndroidViewModel(application) {

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

        _geoFencerObserver.value = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    fun saveGeoFenceRequest() {
        TODO("Not yet implemented")
    }
}