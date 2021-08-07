package com.udacity.project4.utils

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
    const val GEOFENCE_RADIUS_IN_METERS = 50f
    const val CHANNEL_ID = "GeofenceChannel"
}