package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var mLastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var currentMarker: Marker? = null
    private var currentPOI: PointOfInterest? = null

    companion object {
        private val REQUEST_LOCATION_PERMISSION = 1
        private const val DEFAULT_ZOOM = 15
        private val defaultLocation = LatLng(-18.921499703661087, -48.209044831600515)
        private const val KEY_LOCATION = "location"

    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.saveButton.setOnClickListener {
            _viewModel.latitude.postValue(currentMarker?.position?.latitude)
            _viewModel.longitude.postValue(currentMarker?.position?.longitude)
            _viewModel.selectedPOI.postValue(currentPOI)
            _viewModel.reminderSelectedLocationStr.postValue(currentPOI?.name)
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }


        return binding.root
    }

    private fun onLocationSelected() {

        mMap.setOnPoiClickListener { pointOfInterest ->
            val currentLocation = pointOfInterest.latLng
            binding.saveButton.visibility = View.VISIBLE
            if (currentMarker != null) {
                currentMarker?.remove()
            }
            currentPOI = pointOfInterest
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title(pointOfInterest.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (_viewModel.selectedPOI.value != null) {
            currentPOI = _viewModel.selectedPOI.value
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(_viewModel.selectedPOI.value!!.latLng)
                    .title(_viewModel.selectedPOI.value!!.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        enableMyLocation()
        onLocationSelected()
    }

    private fun updateUI() {
        try {
            if (isPermissionGranted()) {
                mMap.uiSettings?.isMyLocationButtonEnabled = true
                mMap.uiSettings?.isMapToolbarEnabled = false
                mMap.isMyLocationEnabled = true
            } else {
                mMap.uiSettings?.isMyLocationButtonEnabled = false
                mMap.uiSettings?.isMapToolbarEnabled = false
                mMap.isMyLocationEnabled = false

                mLastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        try {
            if (isPermissionGranted()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result!!
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation!!.latitude,
                                    mLastKnownLocation!!.longitude
                                ), DEFAULT_ZOOM.toFloat()
                            )
                        )
                    } else {
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            mMap.setMyLocationEnabled(true)
            updateUI()
            getDeviceLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
}