package com.houseofdevelopment.gps.utils


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnPolygonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentCommonMapBinding
import com.houseofdevelopment.gps.geozone.viewmodel.GeoZoneViewModel
import com.houseofdevelopment.gps.homemap.ui.BottomSheetFragment
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import java.util.*
import com.houseofdevelopment.gps.R

/**
 * A simple [Fragment] subclass.
 */
class CommonMapFragment : BaseFragment(), OnMapReadyCallback,
    ResultCallback<LocationSettingsResult>,
    GoogleApiClient.ConnectionCallbacks {

    private lateinit var geoZoneViewModel: GeoZoneViewModel
    private var coming_from: String = ""
    private var _rootView: View? = null
    lateinit var binding: FragmentCommonMapBinding
    private var map: GoogleMap? = null

    var latLngArrayList = ArrayList<LatLng>()
    var polygon: Polygon? = null
    var geoZoneName = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    private var mRequestingLocationUpdates: Boolean = false
    private var bottomSheetFragment: BottomSheetFragment? = null

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        geoZoneViewModel = ViewModelProvider(this).get(GeoZoneViewModel::class.java)
        if (_rootView == null) {
            binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_common_map, container, false
            )
            var data = CommonMapFragmentArgs.fromBundle(arguments!!)
            coming_from = data.comingFrom
            /*viewmodel = ViewModelProviders.of(this).get(HomeMapViewModel::class.java)
            binding.viewmodel = viewmodel*/

            viewsVisibility()
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

    // Draw Geofence circle on GoogleMap
    private var geoFenceLimits: Circle? = null
    private fun drawGeofence(colorCode: String?) {
        val color = Color.parseColor(colorCode)
        binding.pinView.setBackgroundColor(
            Color.argb(
                100,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        )
        binding.pinView.strokeWidth = 3f
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun viewsVisibility() {
        if (coming_from.equals("ShowOnGeoZone", true)) {
            binding.distanceLayout.visibility = View.GONE
            binding.constSaveSelection.visibility = View.GONE
            handleActionBarHidePlusIcon(R.string.geo_zones)
        } else if (coming_from.equals("ShowOnSingleVehicle", true)) {
            binding.distanceLayout.visibility = View.GONE
            binding.constSaveSelection.visibility = View.GONE
            handleActionBarHidePlusIcon(R.string.car_name)
        } else if (coming_from.equals("ShowOnGroupVehicle", true)) {
            binding.distanceLayout.visibility = View.GONE
            binding.constSaveSelection.visibility = View.GONE
            handleActionBarHidePlusIcon(R.string.group_name)
        } else if (coming_from.equals("addGeo", true)) {
            binding.distanceLayout.visibility = View.GONE
            binding.constSaveSelection.visibility = View.VISIBLE
            handleActionBarHidePlusIcon(R.string.geo_zones)
        } else if (coming_from.equals("addGeoGps3", true)) {
            binding.distanceLayout.visibility = View.GONE
            binding.pinView.visibility = View.GONE
            binding.constSaveSelection.visibility = View.VISIBLE
            binding.textCard.visibility = View.VISIBLE
            binding.fab.visibility = View.VISIBLE
            handleActionBarHidePlusIcon(R.string.geo_zones)
        } else if (coming_from.equals("notification", true)) {

            val title = arguments!!.getString("title")
            val time = arguments!!.getString("time")
            val msg = arguments!!.getString("msg")

            handleActionBarHidePlusIcon(R.string.notification_details)

            binding.distanceLayout.visibility = View.GONE
            if (bottomSheetFragment == null) {
                bottomSheetFragment =
                    BottomSheetFragment.newInstance("notification")
                bottomSheetFragment!!.passNotificationData(title, time, msg)
                bottomSheetFragment?.show(fragmentManager!!, bottomSheetFragment?.tag)

            }
        }

        geoZoneViewModel.isGeoZoneAdded.observe(requireActivity()) {
            if (it) {
                Utils.hideProgressBar()
                Toast.makeText(
                    AppBase.instance.applicationContext,
                    AppBase.instance.getString(R.string.zone_added_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
            findNavController().navigateUp()
        }

        binding.btnSaveSelection.setOnClickListener {
            if (coming_from == "addGeoGps3") {
                Utils.showProgressBar(requireContext())

                var zoneVertices = ""
                for (latLng in latLngArrayList) {
                    zoneVertices += latLng.latitude
                    zoneVertices += ","
                    zoneVertices += latLng.longitude
                    zoneVertices += ","
                }
                Log.d(TAG, "onClick: $zoneVertices")
                if (zoneVertices.isNotEmpty()) {
                    zoneVertices = zoneVertices.substring(0, zoneVertices.length - 1)
                    geoZoneViewModel.callApiForAddGeoZoneDataGps3(
                        selectedRGB,
                        arguments?.getString("geoZoneName")!!,
                        zoneVertices
                    )
                    Log.d(TAG, "viewsVisibility: $rgb")
                } else Toast.makeText(context, "Choose Area First!", Toast.LENGTH_SHORT)
                    .show()
            } else { //findNavController().navigateUp()
                val color = Color.parseColor(arguments?.getString("selectedRGB"))

                val rgb = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color))

//            val centerFromPoint: LatLng = map!!.projection.fromScreenLocation(getLocationOnScreen(binding.centerPin))
                val leftFromPoint: LatLng =
                    map!!.projection.fromScreenLocation(getLocationOnScreen(binding.leftPin))
                val centerLatLang: LatLng = map!!.projection.visibleRegion.latLngBounds.center
//            DebugLog.e("CenterPOint=" + centerFromPoint.latitude + "," + centerFromPoint.longitude)
                DebugLog.e("centerLatLang=" + centerLatLang.latitude + "," + centerLatLang.longitude)
                DebugLog.e("leftFromPoint=" + leftFromPoint.latitude + "," + leftFromPoint.longitude)

                val startPoint = Location("locationA")
                startPoint.latitude = centerLatLang.latitude
                startPoint.longitude = centerLatLang.longitude

                val endPoint = Location("locationA")
                endPoint.latitude = leftFromPoint.latitude
                endPoint.longitude = leftFromPoint.longitude

                DebugLog.e("distance=" + startPoint.distanceTo(endPoint))

                geoZoneName = arguments?.getString("geoZoneName")!!
                fillColor = arguments?.getString("selectedRGB")!!
                geoZoneViewModel.callApiForAddGeoZoneData1(
                    arguments?.getString("geoZoneName")!!,
                    rgb,
                    centerLatLang,
                    (startPoint.distanceTo(endPoint)).toInt()
                )
            }
//            geoZoneViewModel.callApiForAddGeoZoneData1(arguments?.getString("geoZoneName")!!, rgb, centerFromPoint, 252986)
        }
    }

    private fun getLocationOnScreen(view: View): Point {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] //+ view.width / 2
        val y = location[1] + view.height / 2
        return Point(x, y)
        // return Point(location[0], location[1]/2)
    }

    private fun loadMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    private val TAG = "CommonMapFragment"
    override fun onMapReady(mMap: GoogleMap) {

        if (isGooglePlayServicesAvailable()) {
            map = mMap
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
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
                (requireActivity() as MainActivity).mapSettings(map!!)
                if (coming_from == "addGeoGps3") {
                    binding.fab.setOnClickListener {
                        map!!.clear()
                        latLngArrayList.clear()
                    }


//                    map!!.setOnMapClickListener {
//                        Toast.makeText(context,it.toString(),Toast.LENGTH_SHORT).show()
//                        map!!.clear()
//                        map!!.addMarker(MarkerOptions().position(it))
//                    }

                    map!!.setOnMapClickListener { latLng -> // Creating a marker
                        binding.textCard.visibility = View.GONE
                        val markerOptions = MarkerOptions()
                        // Setting the position for the marker
                        markerOptions.position(latLng)
                        // Setting the title for the marker.
                        // This will be displayed on taping the marker
                        markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)

                        // Saving selected points.
                        if (latLngArrayList.size > 1) map!!.clear()
                        latLngArrayList.add(latLng)


                        // Clears the previously touched position
                        polygon = map!!.addPolygon(
                            PolygonOptions()
                                .addAll(latLngArrayList)
                        )
                        // Store a data object with the polygon, used here to indicate an arbitrary type.
                        polygon!!.tag = geoZoneName
                        // Style the polygon.
                        stylePolygon(polygon!!)
                        // Animating to the touched position
                        map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                        // Placing a marker on the touched position
                        map!!.addMarker(markerOptions)
                    }

//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));

                    // Set listeners for click events.

//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));

                    // Set listeners for click events.

//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));

                    // Set listeners for click events.
                    map!!.setOnPolygonClickListener(OnPolygonClickListener { polygon -> // Flip the values of the red, green, and blue components of the polygon's color.
                        var color = polygon.strokeColor xor 0x00ffffff
                        polygon.strokeColor = color
                        color = polygon.fillColor xor 0x00ffffff
                        polygon.fillColor = color
                        Toast.makeText(
                            requireContext(),
                            arguments?.getString("geoZoneName") + polygon.tag,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
                if (coming_from.equals("addGeo", true)) {
                    drawGeofence(arguments?.getString("selectedRGB"))
                }
            }
        }
    }

    var fillColor = ""
    var selectedRGB = ""
    private val POLYGON_STROKE_WIDTH_PX = 8
    private val PATTERN_DASH_LENGTH_PX = 20
    private val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())

    // Create a stroke pattern of a gap followed by a dash.
    private val PATTERN_GAP_LENGTH_PX = 20
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
    private val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH)
    var rgb = 0

    private fun stylePolygon(polygon: Polygon) {
        selectedRGB = arguments?.getString("selectedRGB")!!.removeRange(0, 1)
        val selectedColor = Color.parseColor(arguments?.getString("selectedRGB")!!)
        // Apply a stroke pattern to render a dashed line, and define colors.
        val pattern: List<PatternItem?>? = PATTERN_POLYGON_ALPHA
        polygon.strokePattern = pattern
        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        rgb = Color.argb(
            100,
            Color.red(selectedColor),
            Color.green(selectedColor),
            Color.blue(selectedColor)
        )
        polygon.strokeColor = rgb
        polygon.fillColor = rgb
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
        /*fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    DebugLog.d("LocationUpdates ${locationResult.lastLocation}  ${locationResult.locations}")
                    mRequestingLocationUpdates = true
                    *//*viewmodel.myLocation.value = LatLng(
                        *//**//*locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude*//**//*
                        46.6330535, 24.7897052
                    )*//*
                }
            },
            Looper.myLooper()
        )*/
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

        (activity as MainActivity).add_vehicle.setOnClickListener {
            findNavController().navigate(R.id.action_homemapFragment_to_vehicalsListFragment)

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
}


