package com.udacity.project4.utils

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import java.util.concurrent.TimeUnit

object Constants {

    const val AUTENTICATION_CODE = 1

    //tag constants
    const val TAG = "Ex4LogMessage"
    const val FIREBASE_TAG = "Ex4LogFirebaseMessage"
    const val LOCATION_TAG = "Ex4LogLocationMessage"


    //location constants
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    const val REQUEST_LOCATION_PERMISSION = 1
    const val LOCATION_PERMISSION_INDEX = 0
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    //geofence constants
    const val ACTION_GEOFENCE_EVENT = "RemindersActivity.project4.action.ACTION_GEOFENCE_EVENT"
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    const val GEOFENCE_RADIUS_IN_METERS = 10f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}


//todo: move them to util
/**
 * Stores latitude and longitude information along with a hint to help user find the location.
 */
data class LandmarkDataObject(val id: String,  val latLong: LatLng)

    /**
     * Returns the error string for a geofencing error code.
     */
    fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
            )
            else -> resources.getString(R.string.error_adding_geofence)
    }
}