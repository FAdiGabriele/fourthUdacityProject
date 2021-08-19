package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R


private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
enum class PermissionType {
    FOREGROUND_PERMISSION,
    FOREGROUND_AND_BACKGROUND_PERMISSION
}
var confirmSnackBar : Snackbar? = null


fun askToTurnOnLocation(fragment : Fragment, methodToInvoke: () -> Unit  = {}, resolve : Boolean = true){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(fragment.requireActivity())

        //variable that checks location settings
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        fragment.requireActivity(),
                        Constants.REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        Constants.LOCATION_TAG,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    fragment.requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    askToTurnOnLocation(fragment)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            Log.d(Constants.LOCATION_TAG, "Location activated")
            methodToInvoke.invoke()
        }
    }

fun permissionsAreGranted(fragment : Fragment,grantResults: IntArray, requestCode: Int) : Boolean{

    when(requestCode){
        Constants.REQUEST_FOREGROUND_PERMISSIONS_REQUEST_CODE ->{
            return grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED
        }
        Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE ->{
            if(grantResults[Constants.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED){
               return true
            }else{
                confirmSnackBar = Snackbar.make(
                    fragment.requireView(),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )

                   confirmSnackBar!!.setAction(R.string.settings) {
                        fragment.requireActivity().startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", fragment.requireActivity().applicationContext.packageName, null)
                        })
                    }.show()
            }
        }
    }

    return false
}

fun doIfPermissionsAreGiven(fragment : Fragment, permissions: PermissionType, methodToInvoke : () -> Unit){
    when(permissions){
        PermissionType.FOREGROUND_AND_BACKGROUND_PERMISSION-> {
            if(runningQOrLater){
                if(backgroundLocationPermissionApproved(fragment)){
                    methodToInvoke.invoke()
                }else{
                    requestForegroundAndBackgroundLocationPermissions(fragment)
                }
            }else{
                error("INVALID VERSION")
            }
        }
        PermissionType.FOREGROUND_PERMISSION -> {
            if(foregroundLocationPermissionApproved(fragment)){
                methodToInvoke.invoke()
            }else{
                requestForegroundLocationPermissions(fragment)
            }
        }
    }
}

@TargetApi(29)
fun backgroundLocationPermissionApproved(fragment : Fragment): Boolean {
    return if (runningQOrLater) {
        //it is required only from Android Q
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    fragment.requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    } else {
        true
    }
}


fun foregroundLocationPermissionApproved(fragment : Fragment): Boolean {
    return (ActivityCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
}

fun requestForegroundLocationPermissions(fragment : Fragment) {
    Log.e(Constants.LOCATION_TAG, "requestForegroundLocationPermissions")
    if (foregroundLocationPermissionApproved(fragment))
        return
    val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val resultCode =  Constants.REQUEST_FOREGROUND_PERMISSIONS_REQUEST_CODE

    fragment.requestPermissions(
        permissionsArray,
        resultCode)

    Log.e(Constants.LOCATION_TAG, "wait for foreground")
}

@TargetApi(29 )
fun requestForegroundAndBackgroundLocationPermissions(fragment : Fragment) {
    Log.e(Constants.LOCATION_TAG, "requestBackgroundLocationPermissions")
    if (backgroundLocationPermissionApproved(fragment) && foregroundLocationPermissionApproved(fragment))
        return
    val permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val resultCode =  Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE

    fragment.requestPermissions(
        permissionsArray,
        resultCode)

    Log.e(Constants.LOCATION_TAG, "wait for background" )
}