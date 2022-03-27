package com.trackmap.gps.notification.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.trackmap.gps.MainActivity
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentNotificationDetailMapBinding
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.R
class NotificationDetailMapFragment : BaseFragment(), OnMapReadyCallback,
    ResultCallback<LocationSettingsResult>,
    GoogleApiClient.ConnectionCallbacks {

    lateinit var binding: FragmentNotificationDetailMapBinding
    var _rootView: View? = null
    private var map: GoogleMap? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    private var mRequestingLocationUpdates: Boolean = false

    var lat: Double = 0.0
    var lng: Double = 0.0

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (_rootView == null) {

            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_notification_detail_map,
                container,
                false
            )
            handleActionBarHidePlusIcon(R.string.notification_details)

            getBundleData()
            initListeners()
            loadMap(savedInstanceState)
            initGoogleAPIClient()
            createLocationRequest()
            buildLocationSettingsRequest()

            binding.lifecycleOwner = this
            binding.executePendingBindings()
            _rootView = binding.root
        } else {
            if (_rootView?.parent as? ViewGroup != null)
                (_rootView?.parent as? ViewGroup)?.removeView(_rootView)

            return _rootView
        }

        return _rootView
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initListeners() {
        binding.imgGoogleMap.setOnClickListener {

            val urlAddress = "http://maps.google.com/maps?q=$lat,$lng($lat $lng)&iwloc=A&hl=es"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
            context!!.startActivity(intent)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getBundleData() {
        val title = arguments!!.getString("title")
        val time = arguments!!.getString("time")
        val msg = arguments!!.getString("msg")
        lat = arguments!!.getDouble("lat")
        lng = arguments!!.getDouble("long")

        binding.txtMyCar.text = title
        binding.txtDateTime.text = time
        binding.txtNotificationDetails.text = msg

    }

    private fun loadMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(mMap: GoogleMap) {
        if (isGooglePlayServicesAvailable()) {

            map = mMap
            (requireActivity() as MainActivity).mapSettings(map!!)
            map?.setOnMyLocationButtonClickListener {
                checkLocationSettings()
                false
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                }

            } else {
                // (requireActivity() as MainActivity).mapSettings(map!!)
            }
            setupMapLocation()
        }
    }

    private fun setupMapLocation() {
        val mylocation = LatLng(lat, lng)
        map!!.addMarker(MarkerOptions().position(mylocation))
        map!!.uiSettings.isMyLocationButtonEnabled = true
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 15F))
    }

    override fun onResult(locationSettingsResult: LocationSettingsResult) {
        val status = locationSettingsResult.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                DebugLog.i("All location settings are satisfied.")
                if (mGoogleApiClient.isConnected && !mRequestingLocationUpdates) {
                    startLocationUpdates()
                }
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                DebugLog.i(
                    "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings "
                )

                try {
                    /*Show the dialog by calling startResolutionForResult(), and check the result
                    in onActivityResult().*/


                    //status.startResolutionForResult(activity!!, REQUEST_CHECK_SETTINGS)
                    startIntentSenderForResult(
                        status.resolution!!.intentSender, REQUEST_CHECK_SETTINGS,
                        null, 0, 0, 0, null
                    )

                } catch (e: IntentSender.SendIntentException) {
                    DebugLog.i("PendingIntent unable to execute request.")
                }

            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> DebugLog.i(
                "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created."
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (mGoogleApiClient.isConnected && !mRequestingLocationUpdates) {
                        startLocationUpdates()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    DebugLog.i("User chose not to make required location settings changes.")
                }
            }
        }

    }

    override fun onConnected(p0: Bundle?) {
        // checkLocationSettings()
    }

    override fun onConnectionSuspended(p0: Int) {}

    @Synchronized
    private fun initGoogleAPIClient() {
        mGoogleApiClient = context?.let {
            GoogleApiClient.Builder(it)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build()
        }!!
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        //Set min Distance of Displacement by 100m
        mLocationRequest.smallestDisplacement = 100f

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        DebugLog.d("startLocationUpdates")
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    DebugLog.d("LocationUpdates ${locationResult.lastLocation}  ${locationResult.locations}")
                    mRequestingLocationUpdates = true
                    /*viewmodel.myLocation.value = LatLng(
                        *//*locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude*//*
                        46.6330535, 24.7897052
                    )*/
                }
            },
            Looper.myLooper()!!
        )
    }

    private fun checkLocationSettings() {
        val result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            mLocationSettingsRequest
        )
        result.setResultCallback(this)
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        //handleActionBarHidePlusIcon(R.string.tracks, true)
        if (mGoogleApiClient.isConnected && !mRequestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        if (mGoogleApiClient.isConnected) {
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(object :
            LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mRequestingLocationUpdates = false
            }
        })
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // checkLocationSettings()
                    (requireActivity() as MainActivity).mapSettings(map!!)
                }
            }
        }
    }

}