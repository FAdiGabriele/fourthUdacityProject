package com.udacity.project4.util

import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import java.util.*

//As reported here: https://www.kotlindevelopment.com/runtime-permissions-espresso-done-right/
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
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.FOREGROUND_AND_BACKGROUND  -> {
                "MENTRE USI L'APP"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.ONLY_FOREGROUND  -> {
                "SOLO QUESTA VOLTA"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.NONE -> {
                "RIFIUTA"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && Build.VERSION.SDK_INT < 29 && permissionOption != PermissionOptions.NONE-> {
                "CONSENTI"
            }
            Locale.getDefault().displayLanguage == Locale.ITALIAN.displayLanguage && Build.VERSION.SDK_INT < 29 && permissionOption == PermissionOptions.NONE-> {
                "NEGA"
            }
            Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.FOREGROUND_AND_BACKGROUND  -> {
                "While using the app"
            }
            Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.ONLY_FOREGROUND  -> {
                "Only this time"
            }
            Build.VERSION.SDK_INT >= 29 && permissionOption == PermissionOptions.NONE  -> {
                "Deny"
            }
            Build.VERSION.SDK_INT < 29 && permissionOption == PermissionOptions.NONE  -> {
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

fun turnOnPositionIfRequested(locationsOption: LocationsOptions){
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
            } catch (e: UiObjectNotFoundException) {
                Log.e("Test_TAG", "No permission dialog found.")
            }
        }
    }
}

