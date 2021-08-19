package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.CommonViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import com.udacity.project4.utils.Constants.LOCATION_TAG
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment() , OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    val commonViewModel: CommonViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    lateinit var locationManager : LocationManager
    private var buttonShowed = false
    private var latitude = 0.0
    private var longitude = 0.0
    private var selectedPOI : PointOfInterest? = null
    private var namePlace : String = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    val zoomLevel = 15f

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        locationManager = requireActivity().applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.buttonConfirm.setOnClickListener {
            onLocationSelected()
        }

        setObservers()

        return binding.root
    }


    //This supress warning is called because if we are here, the location is enabled and application can access to it
    @SuppressLint("MissingPermission")
    private fun setObservers() {
        commonViewModel.locationRequestedAndApproved.observe(viewLifecycleOwner, Observer { value ->
            if (value) {
                commonViewModel.locationRequestedAndApproved.value = false


                val locationListener : LocationListener = object : LocationListener{
                    override fun onLocationChanged(location: Location) {
                        map.isMyLocationEnabled = true

                        commonViewModel.lastLocation = location
                        val currentCoordinates =
                            LatLng(location.latitude, location.longitude)
                        val lastCameraUpdate =
                            CameraUpdateFactory.newLatLngZoom(currentCoordinates, zoomLevel)
                        map.moveCamera(lastCameraUpdate)
                        map.animateCamera(lastCameraUpdate)
                    }

                    //It is called for avoid crashing when we remove GPS position after select a position on a map on Android 9 or lower
                    override fun onProviderDisabled(provider: String) {
                        Log.e(LOCATION_TAG, "provide $provider disabled")
                    }
                }

                locationManager
                    .requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )

            }
        })
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.selectedPOI.value = selectedPOI
        _viewModel.reminderSelectedLocationStr.value = namePlace
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)

        if(commonViewModel.lastLocation == null){
            val rome = LatLng(41.8902, 12.4922)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(rome, zoomLevel)
            map.moveCamera(cameraUpdate)
            map.animateCamera(cameraUpdate)
        }else{
            val reloadedPosition = LatLng(
                commonViewModel.lastLocation!!.latitude,
                commonViewModel.lastLocation!!.longitude
            )
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(reloadedPosition, zoomLevel)
            map.moveCamera(cameraUpdate)
            map.animateCamera(cameraUpdate)
        }

        enableMyLocation()


        map.setOnMapClickListener{ clickedCoordinates ->
                clickOnMap(clickedCoordinates)
        }

        map.setOnPoiClickListener{ pointOfInterest ->
                selectedPOI = pointOfInterest
                clickOnMap(pointOfInterest.latLng, pointOfInterest.name)
        }
    }

    // Customize the styling of the base map using a JSON object defined
    // in a raw resource file.
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
        if (!success) {
            Log.e(LOCATION_TAG, "Style parsing failed.")
        }
          } catch (e: Resources.NotFoundException) {
       Log.e(LOCATION_TAG, "Can't find style. Error: ", e)
   }
    }

    private fun clickOnMap(latLng: LatLng, nameOfPlace: String = ""){

        if(foregroundLocationPermissionApproved(this)) {
            askToTurnOnLocation(this, {
                map.clear()

                val marker = MarkerOptions().position(latLng).title(nameOfPlace)
                map.addMarker(marker)
                latitude = latLng.latitude
                longitude = latLng.longitude
                namePlace = if (nameOfPlace.isNotBlank()) nameOfPlace else {

                    val shortLatitude = latitude.toString().substring(
                        0,
                        latitude.toString().indexOf(".") + 4
                    )
                    val shortLongitude = longitude.toString().substring(
                        0,
                        longitude.toString().indexOf(".") + 4
                    )
                    "(${shortLatitude},$shortLongitude)"
                }


                if (!buttonShowed) {
                    binding.buttonConfirm.visibility = View.VISIBLE
                    buttonShowed = true
                }})
        }else{
            Toast.makeText(requireContext(), R.string.location_permission_not_granted_selection, Toast.LENGTH_LONG). show()
        }
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation(){
        doIfPermissionsAreGiven(this, PermissionType.FOREGROUND_PERMISSION){
            askToTurnOnLocation(this, methodToInvoke = {
                map.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            location?.let { currentLocation ->
                                commonViewModel.lastLocation = currentLocation
                                val currentCoordinates =
                                    LatLng(currentLocation.latitude, currentLocation.longitude)
                               val lastCameraUpdate =
                                    CameraUpdateFactory.newLatLngZoom(currentCoordinates, zoomLevel)
                                map.moveCamera(lastCameraUpdate)
                                map.animateCamera(lastCameraUpdate)
                            }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(permissionsAreGranted(this, grantResults,requestCode)){
            enableMyLocation()
        }else{
            Toast.makeText(requireContext(), R.string.location_permission_not_granted, Toast.LENGTH_LONG).show()
        }

    }
}
