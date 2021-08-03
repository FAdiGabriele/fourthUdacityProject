package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment() , OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding


    private var buttonShowed = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap
    val zoomLevel = 15f

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        okTODO: add the map setup implementation
//        okTODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        okTODO: put a marker to location that the user selected


//        okTODO: call this function after the user confirms on the selected location
        binding.buttonConfirm.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence


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

        val rome = LatLng(41.8902, 12.4922)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(rome, zoomLevel)
        map.moveCamera(cameraUpdate)
        map.animateCamera(cameraUpdate)
        enableMyLocation()

        map.setOnMapClickListener{ clickedCoordinates ->
            _viewModel.selectedPOI.value = null
            clickOnMap(clickedCoordinates)
        }

        map.setOnPoiClickListener{ pointOfInterest ->
            _viewModel.selectedPOI.value = pointOfInterest
            clickOnMap(pointOfInterest.latLng)
        }
    }


    private fun setMapStyle(map: GoogleMap) {
//        try {
//            // Customize the styling of the base map using a JSON object defined
//            // in a raw resource file.
//            val success = map.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            requireActivity(),
//                            R.raw.map_style
//                    )
//            )
//        if (!success) {
//            Log.e(LOCATION_TAG, "Style parsing failed.")
//        }
//          } catch (e: Resources.NotFoundException) {
//       Log.e(LOCATION_TAG, "Can't find style. Error: ", e)
//   }
    }

    private fun clickOnMap(latLng: LatLng){
        //todo edit String
        map.clear()
        val marker = MarkerOptions().position(latLng).title("Your Point")
        map.addMarker(marker)
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude

        if(!buttonShowed){
            binding.buttonConfirm.visibility = View.VISIBLE
            buttonShowed = true
        }
    }


    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )

            return
        }
        else {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    location?.let{ currentLocation ->
                        val currentCoordinates = LatLng(currentLocation.latitude, currentLocation.longitude)
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentCoordinates, zoomLevel)
                        map.moveCamera(cameraUpdate)
                        map.animateCamera(cameraUpdate)
                    }
                }
        }
    }

}
