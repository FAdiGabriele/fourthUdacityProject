package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient : GeofencingClient
    lateinit var reminderDataItem : ReminderDataItem

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)

        setObservers()
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

            if(_viewModel.validateEnteredData(reminderDataItem)) {
                if (backgroundLocationPermissionApproved(this)
                    && foregroundLocationPermissionApproved(this)) {
                    _viewModel.createGeoFenceRequest(reminderDataItem)
                } else {
                    requestForegroundAndBackgroundLocationPermissions(this)
                    Log.e(Constants.LOCATION_TAG, "GeoFenceRequest refused")
                }
            }
        }
    }

        override fun onDestroy() {
            super.onDestroy()
            //make sure to clear the view model after destroy, as it's a single view model.
            _viewModel.onClear()
            //it avoid context leak when snackbar is showed but we return back to ListFragment
            confirmSnackBar?.dismiss()
        }

    private fun setObservers() {
        _viewModel.geoFenceReady.observe(viewLifecycleOwner, Observer {
            if (it) {
                _viewModel.geoFenceReady.value = false

                    addGeofence(_viewModel.geoFenceToAdd!!)

            }
        })
    }

    //region geofences

    //this suppres exist because in the manifest there is already the permission required, but a bug of Android Studio asks to insert another one
    @SuppressLint("MissingPermission")
    fun addGeofence(request: GeofencingRequest){
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(
                    requireActivity(), R.string.geofence_entered,
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d(
                    Constants.GEOFENCE_TAG,
                    "Add Geofence with id: ${request.geofences.last().requestId}"
                )
                startGeofencingService(request)
            }
            addOnFailureListener {
                Toast.makeText(
                    requireActivity(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT
                ).show()
                if ((it.message != null)) {
                    Log.e(Constants.GEOFENCE_TAG, "message: ${it.message}")
                }
            }
        }
    }

    private fun startGeofencingService(request: GeofencingRequest) {
        val intent = Intent()
        intent.putExtra(Constants.GEOFENCING_REQUEST, request)
        GeofenceTransitionsJobIntentService.enqueueWork(requireActivity(), intent)
        _viewModel.saveReminder(reminderData = reminderDataItem)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(permissionsAreGranted(this, grantResults, requestCode)){
            _viewModel.geoFenceReady.value = true
        }
    }

    //endregion



}
