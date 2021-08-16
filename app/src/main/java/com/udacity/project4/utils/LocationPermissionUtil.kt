package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R


private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

private fun askToTurnOnLocation(activity : Activity, resolve : Boolean = true){
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    val settingsClient = LocationServices.getSettingsClient(activity)

    //variable that checks location settings
    val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve){
            try {
                exception.startResolutionForResult(activity,
                        Constants.REQUEST_TURN_DEVICE_LOCATION_ON)
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(Constants.LOCATION_TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            Snackbar.make(
                    activity.findViewById(R.id.activity_layout),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                askToTurnOnLocation(activity)
            }.show()
        }
    }
    locationSettingsResponseTask.addOnSuccessListener {
        Log.d(Constants.LOCATION_TAG, "Location activated")
    }
}

@TargetApi(29)
private fun foregroundAndBackgroundLocationPermissionApproved(activity : Activity): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION))
    val backgroundPermissionApproved =
            if (runningQOrLater) {

                //it is required only from Android Q
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                                activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
    return foregroundLocationApproved && backgroundPermissionApproved
}

@TargetApi(29 )
private fun requestForegroundAndBackgroundLocationPermissions(activity : Activity) {
    if (foregroundAndBackgroundLocationPermissionApproved(activity))
        return
    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        else -> Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    }
    Log.d(Constants.LOCATION_TAG, "Request foreground only location permission")

//        val dialog = Dialog(this)
    ActivityCompat.requestPermissions(
            activity,
            permissionsArray,
            resultCode
    )
}