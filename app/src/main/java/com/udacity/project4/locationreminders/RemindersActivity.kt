package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.Constants.LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.utils.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.udacity.project4.utils.createChannel
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRemindersBinding
    private val commonViewModel: CommonViewModel by inject()
    //check device version
    private lateinit var geofencingClient : GeofencingClient
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setObservers()
        createChannel(this )
        geofencingClient = LocationServices.getGeofencingClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )


        }else{
            askToTurnOnLocation()
        }



    }


    private fun setObservers() {
        commonViewModel.geoFencerObserver.observe(this, Observer {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                addGeofence(it)
            } else {
                requestForegroundAndBackgroundLocationPermissions()
                Log.e(Constants.LOCATION_TAG, "GeoFenceRequest refused")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //region ask location


    private fun askToTurnOnLocation(resolve : Boolean = true){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)

        //variable that checks location settings
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(Constants.LOCATION_TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    askToTurnOnLocation()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnSuccessListener {
            Toast.makeText(this, "Location activated", Toast.LENGTH_SHORT).show()
        }
    }


    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {

                //it is required only from Android Q
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(Constants.LOCATION_TAG, "Request foreground only location permission")

//        val dialog = Dialog(this)
        ActivityCompat.requestPermissions(
            this,
            permissionsArray,
            resultCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_TURN_DEVICE_LOCATION_ON -> askToTurnOnLocation(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", applicationContext.packageName, null)
                    })
                }.show()

        }
    }

    //endregion

    //region geofences
    fun addGeofence(request : GeofencingRequest){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(this@RemindersActivity, R.string.geofence_entered,
                    Toast.LENGTH_SHORT)
                    .show()
                Log.e("Add Geofence", request.geofences[request.geofences.size-1].requestId)

            }
            addOnFailureListener {
                Toast.makeText(this@RemindersActivity, R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.e(Constants.TAG, "message: ${it.message}")
                }
            }
        }

    }

    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                //todo fix strings
                Log.d(Constants.LOCATION_TAG, "getString(R.string.geofences_removed)")
                Toast.makeText(applicationContext, "R.string.geofences_removed", Toast.LENGTH_SHORT)
                        .show()
            }
            addOnFailureListener {
                Log.d(Constants.LOCATION_TAG, "getString(R.string.geofences_not_removed)")
            }
        }
    }
    //endregion
}
