package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.Constants
import com.udacity.project4.utils.Constants.TAG
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding



    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)

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


//              okTODO:add a geofencing request
//             okTODO: save the reminder to the local db

            val reminderDataItem = ReminderDataItem(title,description,location,latitude,longitude)

            addGeofence(_viewModel.createGeoFenceRequest(reminderDataItem))



            _viewModel.validateAndSaveReminder(reminderDataItem)
        }
    }

        override fun onDestroy() {
            super.onDestroy()
            //make sure to clear the view model after destroy, as it's a single view model.
            _viewModel.onClear()
        }

        fun addGeofence(request : GeofencingRequest){
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val geofencingClient = LocationServices.getGeofencingClient(this@SaveReminderFragment.requireActivity())
            geofencingClient.addGeofences(request, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Toast.makeText(this@SaveReminderFragment.requireActivity(), R.string.geofence_entered,
                            Toast.LENGTH_SHORT)
                            .show()
                    Log.e("Add Geofence", request.geofences[request.geofences.size-1].requestId)

                }
                addOnFailureListener {
                    Toast.makeText(this@SaveReminderFragment.requireActivity(), R.string.geofences_not_added,
                            Toast.LENGTH_SHORT).show()
                    if ((it.message != null)) {
                        Log.w(TAG, it.message!!)
                    }
                }
            }

        }
}
