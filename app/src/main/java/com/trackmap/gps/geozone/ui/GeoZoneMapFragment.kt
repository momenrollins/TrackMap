package com.trackmap.gps.geozone.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.trackmap.gps.MainActivity
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentGeoZoneMapBinding
import com.trackmap.gps.geozone.model.GeoZonData
import com.trackmap.gps.geozone.model.GeoZoneModelItemGps3
import com.trackmap.gps.geozone.viewmodel.GeoZoneViewModel
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.DebugLog
import kotlin.collections.ArrayList


class GeoZoneMapFragment : BaseFragment(), OnMapReadyCallback,
    ResultCallback<LocationSettingsResult>, GoogleApiClient.ConnectionCallbacks {

    private lateinit var binding: FragmentGeoZoneMapBinding

    private var map: GoogleMap? = null
    lateinit var viewmodel: GeoZoneViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private var serverData: String = ""
    private var mRequestingLocationUpdates: Boolean = false
    private var geoZonData = GeoZonData()
    var polygon: Polygon? = null
    private val POLYGON_STROKE_WIDTH_PX = 8

    var lats = 0.0
    var lngs = 0.0
    var zoneName = ""
    private var geozoneItem: GeoZoneModelItemGps3? = null
    private var listHLatlng: ArrayList<LatLng> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!

        if (serverData.contains("s3")) {
            geozoneItem = arguments?.getSerializable("model") as GeoZoneModelItemGps3
            var latLng: LatLng

            for (item in geozoneItem!!.zone_vertices.indices) {
                latLng = LatLng(
                    geozoneItem!!.zone_vertices[item].lat.toDouble(),
                    geozoneItem!!.zone_vertices[item].lng.toDouble()
                )
                builder.include(latLng)
                listHLatlng.add(latLng)
                lats += geozoneItem!!.zone_vertices[item].lat.toDouble()
                lngs += geozoneItem!!.zone_vertices[item].lng.toDouble()
            }
            zoneName = geozoneItem!!.zone_name
        } else {
            geoZonData = arguments?.getSerializable("model") as GeoZonData
            viewmodel = ViewModelProvider(this).get(GeoZoneViewModel::class.java)
            viewmodel.callApiForGeoZoneDetailsData(geoZonData.id)
            viewmodel.geoZoneDetail.observe(this) {
                if (map != null) {
                    drawGeofence()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            com.trackmap.gps.R.layout.fragment_geo_zone_map, container, false
        )
        loadMap(savedInstanceState)
        initGoogleAPIClient()
        createLocationRequest()
        buildLocationSettingsRequest()
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        return binding.root
    }


    private fun loadMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    val builder = LatLngBounds.builder()

    override fun onMapReady(mMap: GoogleMap) {
        if (isGooglePlayServicesAvailable()) {
            map = mMap
            if (map != null && serverData.contains("s3")) {
                var polygon = mMap!!.addPolygon(
                    PolygonOptions()
                        .addAll(listHLatlng)
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE)
                )
                (requireActivity() as MainActivity).mapSettings(map!!)
                map?.setOnMyLocationButtonClickListener {
                    checkLocationSettings()
                    false
                }
                for (item in listHLatlng){
                    builder.include(item)
                }
                val bounds = builder.build()
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200), 1000, null)
                /*map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lats / listHLatlng.size,
                            lngs / listHLatlng.size
                        ), 2.7f
                    )
                )*/
                // Store a data object with the polygon, used here to indicate an arbitrary type.
                polygon.tag = zoneName
                // Style the polygon.
                stylePolygon(polygon)
            }
        } else {
            Toast.makeText(
                context,
                geozoneItem!!.zone_vertices[0].lat.toString(),
                Toast.LENGTH_SHORT
            ).show()

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
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
                (requireActivity() as MainActivity).mapSettings(map!!)


                //drawGeofence()
            }
        }
    }

    private fun stylePolygon(polygon: Polygon) {
        var strokeColor = 0
        var fillColor = 0
            strokeColor = Color.parseColor(geozoneItem!!.zone_color)
            fillColor = Color.parseColor(geozoneItem!!.zone_color)
            fillColor = Color.argb(100, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor))



        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }


    // Draw Geofence circle on GoogleMap
    private var geoFenceLimits: Circle? = null
    private fun drawGeofence() {

        val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and geoZonData.colorCode)
        val color = Color.parseColor(hexColor)

        if (geoFenceLimits != null) geoFenceLimits!!.remove()
        when (viewmodel.geoZoneDetail.value?.t) {
            3 -> {
                val circleOptions = CircleOptions()
                    .center(geoZonData.centerLatLong)
                    .strokeColor(
                        Color.rgb(
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                    .strokeWidth(5f)
                    .fillColor(
                        Color.argb(
                            60,
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                    .radius(geoZonData.radius)
                geoFenceLimits = map!!.addCircle(circleOptions)
                val builder = LatLngBounds.builder()

                builder.include(geoZonData.minLatLong)
                builder.include(geoZonData.centerLatLong)
                builder.include(geoZonData.maxLatLong)
                val bounds = builder.build()
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
            }
            2 -> {
                val options = PolygonOptions().strokeWidth(5f)
                    .strokeColor(
                        Color.rgb(
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                    .fillColor(
                        Color.argb(
                            60,
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                val builder = LatLngBounds.builder()
                for (z in 0 until viewmodel.geoZoneDetail.value?.p?.size!!) {
                    val point: LatLng = LatLng(
                        viewmodel.geoZoneDetail.value?.p?.get(z)?.y!!,
                        viewmodel.geoZoneDetail.value?.p?.get(z)?.x!!
                    )
                    options.add(point)
                    builder.include(point)
                }
                val bounds = builder.build()
                map!!.addPolygon(options)
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
            }
            else -> {
                val options = PolylineOptions().width(5f)
                    .color(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)))
                val builder = LatLngBounds.builder()
                for (z in 0 until viewmodel.geoZoneDetail.value?.p?.size!!) {
                    val point: LatLng = LatLng(
                        viewmodel.geoZoneDetail.value?.p?.get(z)?.y!!,
                        viewmodel.geoZoneDetail.value?.p?.get(z)?.x!!
                    )
                    options.add(point)
                    builder.include(point)
                }
                val bounds = builder.build()
                map!!.addPolyline(options)
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
            }
        }
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
                    startIntentSenderForResult(
                        status.resolution!!.intentSender,
                        REQUEST_CHECK_SETTINGS,
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
        checkLocationSettings()
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
        mLocationRequest = LocationRequest.create()
        //Set min Distance of Displacement by 100m
//        mLocationRequest.smallestDisplacement = HomeMapFragment.MIN_DISPLACEMENT

        mLocationRequest.interval = 500
        mLocationRequest.fastestInterval = 500
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
                }
            },
            Looper.getMainLooper()
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
        handleActionBarHidePlusIcon( com.trackmap.gps.R.string.geo_zone)
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

    /*override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }*/
}