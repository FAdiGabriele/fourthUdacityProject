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
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.Constants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.Constants.BACKGROUND_LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.Constants.LOCATION_PERMISSION_INDEX
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.utils.Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.utils.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.utils.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
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
                Log.d(Constants.GEOFENCE_TAG, "Add Geofence with id: ${request.geofences.last().requestId}")
                startGeofencingService(request)
            }
            addOnFailureListener {
                Toast.makeText(this@RemindersActivity, R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.e(Constants.GEOFENCE_TAG, "message: ${it.message}")
                }
            }
        }

    }

    private fun startGeofencingService(request: GeofencingRequest) {
        val intent = Intent()
        intent.putExtra(Constants.GEOFENCING_REQUEST, request)
        GeofenceTransitionsJobIntentService.enqueueWork(this, intent)
    }


    //endregion
}
