package com.udacity.project4.util

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import java.util.*

//As reported here: https://www.kotlindevelopment.com/runtime-permissions-espresso-done-right/
val runningO_MR1OrEarlier = Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1
val runningP = Build.VERSION.SDK_INT == Build.VERSION_CODES.P
val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
enum class PermissionOptions{
    NONE,
    ONLY_FOREGROUND,
    FOREGROUND_AND_BACKGROUND
}
enum class LocationsOptions{
    TURN_ON,
    KEEP_OFF
}

fun grantPermissionsIfRequested(permissionOption: PermissionOptions) {

    if (Build.VERSION.SDK_INT >= 23) {
        val textButtonToClick = when{
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningQOrLater && permissionOption == PermissionOptions.FOREGROUND_AND_BACKGROUND  -> {
                "MENTRE USI L'APP"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningQOrLater && permissionOption == PermissionOptions.ONLY_FOREGROUND  -> {
                "SOLO QUESTA VOLTA"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningQOrLater && permissionOption == PermissionOptions.NONE -> {
                "RIFIUTA"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningP && permissionOption != PermissionOptions.ONLY_FOREGROUND-> {
                "Consenti"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningP && permissionOption != PermissionOptions.NONE-> {
                "Nega"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningO_MR1OrEarlier && permissionOption != PermissionOptions.NONE-> {
                "CONSENTI"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && runningO_MR1OrEarlier && permissionOption == PermissionOptions.NONE-> {
                "NEGA"
            }
            runningQOrLater && permissionOption == PermissionOptions.FOREGROUND_AND_BACKGROUND  -> {
                "While using the app"
            }
            runningQOrLater && permissionOption == PermissionOptions.ONLY_FOREGROUND  -> {
                "Only this time"
            }
            runningQOrLater && permissionOption == PermissionOptions.NONE  -> {
                "Deny"
            }
            runningO_MR1OrEarlier && permissionOption == PermissionOptions.NONE  -> {
                "DENY"
            }
            else ->{
                "ALLOW"
            }
        }
        val allowPermissions = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).findObject(UiSelector().text(textButtonToClick))
        if (allowPermissions.exists()) {
            try {
                allowPermissions.click()
            } catch (e: UiObjectNotFoundException) {
                Log.e("Test_TAG", "No permission dialog found.")
            }
        }
    }
}

fun turnOnPositionIfRequested(locationsOption: LocationsOptions) : Boolean{
    if (Build.VERSION.SDK_INT >= 23) {
        val textButtonToClick = when{
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && locationsOption == LocationsOptions.KEEP_OFF -> {
                "NO, GRAZIE"
            }
            locationsOption == LocationsOptions.KEEP_OFF -> {
                "CANCEL"
            }
            else ->{
                "OK"
            }
        }
        val allowPermissions = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).findObject(UiSelector().text(textButtonToClick))
        if (allowPermissions.exists()) {
            try {
                allowPermissions.click()
                return true
            } catch (e: UiObjectNotFoundException) {
                Log.e("Test_TAG", "No permission dialog found.")
            }
        }
        return false
    }
    return true
}

fun resetAllPermissions(){
    if(runningQOrLater) {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm revoke ${androidx.test.InstrumentationRegistry.getTargetContext().packageName} android.permission.ACCESS_BACKGROUND_LOCATION")
    }

    InstrumentationRegistry.getInstrumentation().uiAutomation.
    executeShellCommand("pm revoke ${androidx.test.InstrumentationRegistry.getTargetContext().packageName} android.permission.ACCESS_FINE_LOCATION")
    InstrumentationRegistry.getInstrumentation().uiAutomation.
    executeShellCommand("pm revoke ${androidx.test.InstrumentationRegistry.getTargetContext().packageName} android.permission.ACCESS_COARSE_LOCATION")
}


fun checkIfLocationIsEnabled(context : Context): Boolean{
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var gps_enabled = false
    var network_enabled = false

    try {
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (ex: Exception) {
    }

    return !(!gps_enabled && !network_enabled)
}

