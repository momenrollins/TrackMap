package com.trackmap.gps.homemap.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.trackmap.gps.R
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.google.maps.android.clustering.ClusterManager
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.DataValues.tz
import com.trackmap.gps.MainActivity
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentHomeBinding
import com.trackmap.gps.geozone.viewmodel.GeoZoneViewModel
import com.trackmap.gps.homemap.model.GetCarDetailsModel
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.hrmovecarmarkeranimation.AnimationClass.HRMarkerAnimation
import com.trackmap.gps.login.ui.LoginActivity
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.*
import com.trackmap.gps.vehicallist.model.ItemGps3
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : BaseFragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    ResultCallback<LocationSettingsResult> {
    private val START_ZOOM: Float = 14f
    private val REQUEST_CHECK_SETTINGS: Int = 111
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var map: GoogleMap? = null
    private var previousSelectedMarker: Marker? = null
    private var mPreviousSelectedClusterItem: ClusterRender? = null
    private var mClusterManager: ClusterManager<ClusterRender>? = null
    private var mPreviousSelectedClusterItemGPS3: ClusterRenderGPS3? = null
    private var mClusterManagerGPS3: ClusterManager<ClusterRenderGPS3>? = null

    private lateinit var binding: FragmentHomeBinding
    private lateinit var findParkingRenderer: CarOnMapRenderer
    private lateinit var findParkingRendererGPS3: CarOnMapRendererGPS3

    private var _rootView: View? = null
    private var firstTime: Boolean = true
    private var mRequestingLocationUpdates: Boolean = false
    private lateinit var geoZoneViewmodel: GeoZoneViewModel
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mGeoZonesCircle = ArrayList<Circle>()
    private var mGeoZonesPolygon = ArrayList<Polygon>()
    private var mGeoZonesPolyline = ArrayList<Polyline>()
    var polygon: Polygon? = null
    val handler2 = Handler(Looper.getMainLooper())
    var polygonList: ArrayList<Polygon> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        Log.d(TAG, "onCreate: HOME")
        initGoogleAPIClient()
        createLocationRequest()
        buildLocationSettingsRequest()
        StrictMode.setThreadPolicy(policy)
    }

    private var selectedCarId = ArrayList<String>()

    private fun selectedCarDataList() {
        if (MyPreference.getValueString(PrefKey.SELECTED_CAR_LISTING, "")?.isNotEmpty()!!) {
            val data = MyPreference.getValueString(PrefKey.SELECTED_CAR_LISTING, "")
            val splitDatas = data?.split(",")
            selectedCarId.clear()
            for (i in 0 until splitDatas?.size!!) {
                if (splitDatas[i].isNotEmpty())
                    selectedCarId.add(splitDatas[i])
            }
            Log.d(TAG, "selectedCarDataList:size ${selectedCarId.size} ")
        }
    }


    @SuppressLint("WrongConstant")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            _rootView = binding.root
            (requireActivity() as MainActivity).homeMapViewModel.clearData()
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
            checkForArguments(arguments)
        } else {
            if (_rootView?.parent as? ViewGroup != null)
                (_rootView?.parent as? ViewGroup)?.removeView(_rootView)
            if (serverData.contains("s3")) {
                (requireActivity() as MainActivity).homeMapViewModel.carAddressDataGPS3 =
                    MutableLiveData<String>()
            } else {
                (requireActivity() as MainActivity).homeMapViewModel.carAddressData =
                    MutableLiveData<String>()
                (requireActivity() as MainActivity).homeMapViewModel.geoZoneDetail.postValue(
                    ArrayList()
                )
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return _rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geoZoneViewmodel = ViewModelProvider(this)[GeoZoneViewModel::class.java]

        DebugLog.e("Car==" + MyPreference.getValueString(PrefKey.SELECTED_CAR_LISTING, ""))
        selectedCarDataList()
        addObserver()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
        }
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val h = bottomSheet.height.toFloat()
                when {
                    newState == BottomSheetBehavior.STATE_EXPANDED && binding.bottomSheet.invisibleView.visibility == View.VISIBLE -> binding.bottomSheet.imgTopUpperArrow.setImageResource(
                        R.drawable.ic_arrow_down
                    )
                    newState == BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_upper_arrow)
                    }
                    newState == BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_upper_arrow)
                    }
                    else -> {
                        setMapPaddingBotttom(h)
                        binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_upper_arrow)
                    }
                }
                carMapMoving()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                setMapPaddingBotttom(off)
                carMapMoving()
            }

        })
    }

    private fun carMapMoving() {
        if (binding.selectedCar != null) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        binding.selectedCar!!.position.latitude,
                        binding.selectedCar!!.position.longitude
                    )
                )
            )
        }
    }

    private fun setMapPaddingBotttom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        map?.setPadding(0, 0, 0, Math.round(offset * maxMapPaddingBottom))

        when {
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED && binding.bottomSheet.invisibleView.visibility == View.VISIBLE -> {
                map?.uiSettings?.isZoomControlsEnabled = false
            }
            else -> {
                map?.uiSettings?.isZoomControlsEnabled = true
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun checkForArguments(arguments: Bundle?) {
        if (arguments != null) {
            val comingFrom = arguments.getString("comingFrom")
            if (comingFrom != null) {
                when {
                    comingFrom.equals("ShowOnSingleVehicle", true) -> {
                        selectedCarId = arguments.getStringArrayList("carId") as ArrayList<String>
                    }
                    comingFrom == "ShowOnGroupSingleVehicle" -> {
                        selectedCarId = arguments.getStringArrayList("carId") as ArrayList<String>
                    }
                    comingFrom == "ShowOnGroupVehicle" -> {
                        selectedCarId = arguments.getStringArrayList("carId") as ArrayList<String>
                    }
                }
            }
        }
    }

    private fun getPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        if (MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!
                .contains("s3")
        ) {
            setUpClusterGPS3()
        } else {
            setUpCluster()
        }
    }

    private fun requestLocationWithDisclosure() {
        val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alert.setTitle("GPS Disclosure");
        alert.setMessage("${getString(R.string.app_name)} collects location data to enable user to bind with cars, to show your current location on map and to show cars on map.");
        alert.setPositiveButton(android.R.string.yes) { dialog, which ->
            getPermission()
        }
        alert.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(
                requireContext(), getString(R.string.accept_permission), Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
        }
        val dialog = alert.create()
        dialog.show();
    }

    override fun onMapReady(p0: GoogleMap) {
        if (isGooglePlayServicesAvailable()) {
            map = p0
            initMapView()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                getPermission()
            } else {
                (requireActivity() as MainActivity).mapSettings(map!!)

                if (MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!
                        .contains("s3")
                ) {
                    setUpClusterGPS3()

                } else {
                    setUpCluster()
                }
                // checkLocationSettings()
            }
        }
    }


    private fun initMapView() {
        if (map != null) {
            (requireActivity() as MainActivity).mapSettings(map!!)
            map?.isTrafficEnabled = MyPreference.getValueBoolean(PrefKey.IS_TRAFFIC, false)
            map?.setOnMyLocationButtonClickListener {
                checkLocationSettings()
                if (isAdded && isVisible) {
                    if ((requireActivity() as MainActivity).isFakeLocation) {
                        CommonAlertDialog.showAlertWithMessage(
                            requireContext(),
                            "You are using Fake GPS"
                        )
                    }
                }
                false
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the `#addApi` method to request the
     * LocationServices API.
     */
    @SuppressLint("UseRequireInsteadOfGet")
    @Synchronized
    private fun initGoogleAPIClient() {
        mGoogleApiClient = context?.let {
            GoogleApiClient.Builder(it)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build()
        }!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
    }

    override fun onConnected(p0: Bundle?) {
        // checkLocationSettings()
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()

        //Set min Distance of Displacement by 100m
//        mLocationRequest.smallestDisplacement = MIN_DISPLACEMENT
        mLocationRequest.interval = 500
        mLocationRequest.fastestInterval = 500
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * [com.google.android.gms.location.SettingsApi.checkLocationSettings] method, with the results provided through a `PendingResult`.
     */
    private fun checkLocationSettings() {
        val result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            mLocationSettingsRequest
        )
        result.setResultCallback(this)
    }

    /**
     * The callback invoked when
     * [com.google.android.gms.location.SettingsApi.checkLocationSettings] is called. Examines the
     * [com.google.android.gms.location.LocationSettingsResult] object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    override fun onResult(locationSettingsResult: LocationSettingsResult) {
        val status = locationSettingsResult.status
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                if (mGoogleApiClient.isConnected && !mRequestingLocationUpdates) {
                    startLocationUpdates()
                }
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
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

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(object :
            LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                mRequestingLocationUpdates = false
            }
        })
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.locations.forEach {
                            if (isAdded && isVisible) {
                                try {
                                    if (requireActivity() is MainActivity) {
                                        (requireActivity() as MainActivity).isFakeLocation =
                                            it.isFromMockProvider
                                        if (it.isFromMockProvider) {
                                            if (!(requireActivity() as MainActivity).isFakeLocation) {
                                                (requireActivity() as MainActivity).isFakeLocation =
                                                    true
                                                CommonAlertDialog.showAlertWithMessage(
                                                    context!!, "You are using Fake GPS"
                                                )
                                            }
                                        } else {
                                            (requireActivity() as MainActivity).isFakeLocation =
                                                false
                                        }
                                    }
                                } catch (e: Exception) {

                                }
                            }
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onResume() {
        // handler2.post(updateTextTask)
        super.onResume()
        handleActionBar(R.string.map)
        try {
            if (serverData.contains("s3")) {
                if (MyPreference.getValueBoolean(PrefKey.IS_GEO_ZONES, false)) {
                    geoZoneViewmodel.callApiForGetGeoZonesGps3()
                } else {
                    geoZoneViewmodel.geoZoneListGps3.postValue(null)
                }
            } else {
                if (MyPreference.getValueString(Constants.E_ID, "")?.isNotEmpty()!!) {
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForSetLocale(
                        134237528, 256
                    )
                    if (MyPreference.getValueBoolean(PrefKey.IS_GEO_ZONES, false)) {
                        (requireActivity() as MainActivity).homeMapViewModel.callApiForGeoZoneDetailsData()
                    }
                }
            }
            (activity as MainActivity).chk_check.visibility = View.INVISIBLE
            (activity as MainActivity).add_vehicle.visibility = View.VISIBLE
            if (mGoogleApiClient.isConnected && !mRequestingLocationUpdates) {
                startLocationUpdates()
            }

            (activity as MainActivity).add_vehicle.setOnClickListener {
                hideBottomSheetValues()

                Handler(Looper.myLooper()!!).postDelayed({
                    try {
                        findNavController().navigate(R.id.action_homemapFragment_to_vehicalsListFragment)
                    } catch (e: Exception) {
                        Log.e(TAG, "onResume: CATCH ${e.message}")
                    }
                }, 200)


            }
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
                if (it.isSuccessful.not()) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = it.result
                Log.d(TAG, "onResume: ttk home $token")
                if (MyPreference.getValueString(PrefKey.FCM_TOKEN, "") != token) {
                    val username = MyPreference.getValueString(Constants.USER_NAME, "")
                    if (username != null)
                        if (serverData.contains("s3") && username != "") {
                            Log.d(TAG, "onResume: GPS3 TOKEN:: $username")
                            (requireActivity() as MainActivity).homeMapViewModel.sendTokenGps3(
                                username, token
                            )
                        } else if (!serverData.contains("s3") && username != "") {
                            Log.d(TAG, "onResume: GPS TOKEN:: $username")
                            (requireActivity() as MainActivity).homeMapViewModel.callSignInMethod(
                                username
                            )
                        }
                }
                MyPreference.setValueString(PrefKey.FCM_TOKEN, token.toString())
            })
            initMapView()
        } catch (e: Exception) {
            Log.e(TAG, "Catch onResume: ${e.message}")
        }
    }

    fun hideBottomSheetValues() {
        try {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } catch (e: Exception) {
            Log.e(TAG, "hideBottomSheetValues: CATCH ${e.message}")
        }

    }

    override fun onPause() {
        super.onPause()
        //  handler2.removeCallbacks(updateTextTask)
        if (mGoogleApiClient.isConnected) {
            stopLocationUpdates()
        }
    }

    override fun onStop() {
        // handler2.removeCallbacks(updateTextTask)
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    /**
     *  Call api for setting up car details on map
     */
    @SuppressLint("FragmentLiveDataObserve", "UseRequireInsteadOfGet")
    private fun addObserver() {
        (requireActivity() as MainActivity).homeMapViewModel.status.observe(this) {
            Log.d(TAG, "addObserver: status=> $it")
            when (it!!) {
                ApiStatus.LOADING -> progressDialog.show()
                ApiStatus.DONE -> progressDialog.dismiss()
                ApiStatus.ERROR -> {
                    progressDialog.dismiss()
                    (requireActivity() as MainActivity).homeMapViewModel.callsetToken()
//                     showError()
                }
                ApiStatus.NOINTERNET -> {
                    progressDialog.dismiss()
                    CommonAlertDialog.showConnectionAlert(context!!)
                }
                ApiStatus.SUCCESSFUL -> {
                    progressDialog.dismiss()
                }
            }
        }

        if (serverData.contains("s3")) {
            (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(this) {
                Log.d(TAG, "addObserver: carCarDetailsGPS3")
                geoZoneViewmodel.callApiForGetGeoZonesGps3()
                updateSpeedGps3(it)
                val allCarList = Utils.getCarListingDataGps3(context!!)
                addCarOnClusterGPS3(allCarList.items)
            }
            (requireActivity() as MainActivity).homeMapViewModel.isRightKey.observe(this) {
                if (!it) {
                    MyPreference.clearAllData()
                    Toast.makeText(
                        requireContext(), getString(R.string.sign_in_again), Toast.LENGTH_SHORT
                    ).show()
                    val myIntent = Intent(activity, LoginActivity::class.java)
                    myIntent.putExtra("logoutFromApp", true)
                    startActivity(myIntent)
                    requireActivity().finish()
                }
            }
            (requireActivity() as MainActivity).homeMapViewModel.carAddressDataGPS3.observe(this) {
                binding.carAddress = it
                if (binding.selectedCarGPS3 != null) {
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForDriverNameGps3(
                        binding.selectedCarGPS3!!.mCarModel.imei
                    )
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForSensorValueGps3((binding.selectedCarGPS3!!.mCarModel.imei))
                }
            }
            (requireActivity() as MainActivity).homeMapViewModel.carDriverNameDataGPS3.observe(
                this
            ) {
                if (it.status && it.data!!.isNotEmpty())
                    binding.driverName = it.data!![0].driverName
                else
                    binding.driverName = "--"
            }
            (requireActivity() as MainActivity).homeMapViewModel.sensorDetailsGps3.observe(
                viewLifecycleOwner
            ) {
                if (it.status) {
                    binding.bottomSheet.noDataLay.visibility = View.GONE
                    if (it.data!!.isNotEmpty() && binding.bottomSheet.rvSensors.adapter != null && binding.bottomSheet.rvSensors.adapter!!.itemCount > 0) {
                        try {
                            Log.d(TAG, "updateSpeedGPS3: in  ${it.data!!.size}")

                            (binding.bottomSheet.rvSensors.adapter as CarDetailsAdapterGPS3).updateValues(
                                it
                            )
                            for (index in it.data!!) {
                                Log.d(TAG, "updateSpeedGPS3: ${index.name}")
                                if (index.name == "حالة المحرك") {
                                    Log.d(TAG, "updateSpeedGPS3: ${index.value}")

                                    if (index.value.toString().toDouble() > 0) {
                                        binding.bottomSheet.txtEngineStatus.text =
                                            requireContext().getString(R.string.on)
                                        binding.bottomSheet.imgCarEngine.setImageResource(
                                            R.drawable.ic_car_engine_on
                                        )
                                    } else {
                                        binding.bottomSheet.txtEngineStatus.text =
                                            requireContext().getString(R.string.off)
                                        binding.bottomSheet.imgCarEngine.setImageResource(
                                            R.drawable.ic_engine_on_off
                                        )
                                    }
                                }

                                binding.bottomSheet.noDataLay.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "addObserver: CATCH ${e.message}")
                        }

                    } else if (it.data!!.isNotEmpty()) {
                        try {
                            for (index in it.data!!) {
                                Log.d(TAG, "updateSpeedGPS3: ${index.name}")
                                if (index.name == "حالة المحرك") {
                                    Log.d(TAG, "updateSpeedGPS3: ${index.value}")

                                    if (index.value.toString().toDouble() > 0) {
                                        binding.bottomSheet.txtEngineStatus.text =
                                            requireContext().getString(R.string.on)
                                        binding.bottomSheet.imgCarEngine.setImageResource(
                                            R.drawable.ic_car_engine_on
                                        )
                                    } else {
                                        binding.bottomSheet.txtEngineStatus.text =
                                            requireContext().getString(R.string.off)
                                        binding.bottomSheet.imgCarEngine.setImageResource(
                                            R.drawable.ic_engine_on_off
                                        )
                                    }
                                }
                                BindAdapterGPS3.bindHomeTabList(
                                    binding.bottomSheet.rvSensors, it
                                )
                                binding.bottomSheet.noDataLay.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "addObserver: CATCH ${e.message}")
                        }
                    } else {
                        showNoSensFound()
                    }
                } else {
                    showNoSensFound()
                }
            }
            geoZoneViewmodel.geoZoneListGps3.observe(this) {
                Log.d(TAG, "addObserver: geoZoneListGps3")
                val listLatlng: ArrayList<LatLng> = ArrayList()
                if (polygonList.size > 0) {
                    for (poly in polygonList)
                        poly.remove()
                    polygonList.clear()
                }
                if (it != null && MyPreference.getValueBoolean(PrefKey.IS_GEO_ZONES, false)) {
                    for (zone in it.data) {
                        listLatlng.clear()
                        for (latLng in zone.zone_vertices) {
                            val latLng1 = LatLng(
                                latLng.lat.toDouble(), latLng.lng.toDouble()
                            )
                            listLatlng.add(latLng1)
                        }
                        polygon = map?.addPolygon(
                            PolygonOptions().addAll(listLatlng).strokeColor(Color.RED)
                                .fillColor(Color.BLUE)
                        )
                        polygonList.add(polygon!!)
                        stylePolygon(polygon!!, zone.zone_color)
                    }
                }
            }

        } else {
            (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(this) {
                Utils.hideProgressBar()
                it.let {

                    val uri =
                        (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                            it, requireContext()
                        )
                    addCarOnCluster(uri)
                }
            }
            (requireActivity() as MainActivity).homeMapViewModel.carAddressData.observe(this) {
                binding.carAddress = it
                if (binding.selectedCar != null) (requireActivity() as MainActivity).homeMapViewModel.callApiForDriverName(
                    binding.selectedCar!!
                )
                if (binding.selectedCar != null) (requireActivity() as MainActivity).homeMapViewModel.callApiForSensorValue(
                    binding.selectedCar!!
                )
            }
            (requireActivity() as MainActivity).homeMapViewModel.carDriverNameData.observe(this) {
                if (it.trim().isNotEmpty())
                    binding.driverName = it
                else
                    binding.driverName = "--"
            }
            (requireActivity() as MainActivity).homeMapViewModel.carCarDetails.observe(this) {
                Log.d(TAG, "addObserver: ref ${it.item}")
                updateSpeed(it, (requireActivity() as MainActivity).homeMapViewModel.sensorDetails)
            }
            (requireActivity() as MainActivity).homeMapViewModel.sensorDetails.observe(this) {

                Log.d(TAG, "addObserver:binding ${binding.selectedCar} ")

                if (binding.selectedCar != null)
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForGetCarDetails(
                        binding.selectedCar!!
                    )
            }
            (requireActivity() as MainActivity).homeMapViewModel.geoZoneDetail.observe(this) {
//                Log.d(TAG, "addObserver: ${it[0].n}")
                if (map == null) return@observe
                if (it.size > 0) {
                    for (int in 0 until mGeoZonesCircle.size) {
                        mGeoZonesCircle[int].remove()
                    }
                    for (int in 0 until mGeoZonesPolyline.size) {
                        mGeoZonesPolyline[int].remove()
                    }
                    for (int in 0 until mGeoZonesPolygon.size) {
                        mGeoZonesPolygon[int].remove()
                    }
                    mGeoZonesCircle.clear()
                    mGeoZonesPolygon.clear()
                    mGeoZonesPolyline.clear()
                    for (i in 0 until it.size) {
                        val hexColor =
                            java.lang.String.format("#%06X", 0xFFFFFF and it[i].c.toInt())
                        val color = Color.parseColor(hexColor)
                        when (it[i].t) {
                            3 -> {
                                val circleOptions = CircleOptions()
                                    .center(LatLng(it[i].p?.get(0)?.y!!, it[i].p?.get(0)?.x!!))
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
                                    .radius(it[i].w)
                                mGeoZonesCircle.add(map!!.addCircle(circleOptions))
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
                                for (z in 0 until it[i].p?.size!!) {
                                    val point = LatLng(it[i].p?.get(z)?.y!!, it[i].p?.get(z)?.x!!)
                                    options.add(point)
                                }
                                mGeoZonesPolygon.add(map!!.addPolygon(options))
                            }
                            else -> {
                                val options = PolylineOptions().width(5f)
                                    .color(
                                        Color.rgb(
                                            Color.red(color),
                                            Color.green(color),
                                            Color.blue(color)
                                        )
                                    )
                                for (z in 0 until it[i].p?.size!!) {
                                    val point = LatLng(it[i].p?.get(z)?.y!!, it[i].p?.get(z)?.x!!)
                                    options.add(point)
                                }
                                mGeoZonesPolyline.add(map!!.addPolyline(options))
                            }
                        }
                    }
                } else {
                    for (int in 0 until mGeoZonesCircle.size) {
                        mGeoZonesCircle[int].remove()
                    }
                    for (int in 0 until mGeoZonesPolyline.size) {
                        mGeoZonesPolyline[int].remove()
                    }
                    for (int in 0 until mGeoZonesPolygon.size) {
                        mGeoZonesPolygon[int].remove()
                    }
                }
            }

        }
    }

    private val POLYGON_STROKE_WIDTH_PX = 8
    private fun stylePolygon(polygon: Polygon, color: String) {
        val fillColor: Int
        val strokeColor: Int = Color.parseColor(color)
        fillColor = Color.argb(
            100,
            Color.red(strokeColor),
            Color.green(strokeColor),
            Color.blue(strokeColor)
        )

        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!progressDialog.isShowing)
            progressDialog.dismiss()
        binding.unbind()
    }

    @SuppressLint("SetTextI18n")
    private fun addCarOnCluster(uriData: ArrayList<Item>?) {
        val uri = ArrayList<Item>()
        if (uriData == null || (uriData.size == 0) || (selectedCarId.size > 0 && selectedCarId.size != uriData.size)) {
            uri.clear()
            Utils.getCarListingData(requireContext()).items.let { uri.addAll(it) }
        } else {
            uri.clear()
            uri.addAll(uriData)
        }
        if (selectedCarId.size > 0) {
            val unitCarList = ArrayList<Item>()
            for (i in 0 until uri.size) {
                if (selectedCarId.contains(uri[i].id.toString())) {
                    unitCarList.add(uri[i])
                }
            }
            newUpdateView(unitCarList)
            Log.d(TAG, "addCarOnCluster:addCarOnCluster enter ")

            if (binding.selectedCar != null && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE) {
                (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddress(
                    binding.selectedCar!!.position.longitude.toString(),
                    binding.selectedCar!!.position.latitude.toString()
                )
            }
        } else {
            Log.d(TAG, "addCarOnCluster:addCarOnCluster out ")
            newUpdateView(uri)
            for (item in uri){
                selectedCarId.add(item.id.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addCarOnClusterGPS3(uriData: ArrayList<ItemGps3>?) {
        Log.d(TAG, "addCarOnClusterGPS3: ${uriData?.size}")
        val allCarList = ArrayList<ItemGps3>()
        if (uriData == null || (uriData.size == 0) || (selectedCarId.size > 0 && selectedCarId.size != uriData.size)) {
            allCarList.clear()
            Utils.getCarListingDataGps3(requireContext()).items.let { allCarList.addAll(it) }
        } else {
            allCarList.clear()
            allCarList.addAll(uriData)
        }
        if (selectedCarId.size > 0) {
            val unitCarList = ArrayList<ItemGps3>()
            for (i in 0 until allCarList.size) {
                if (selectedCarId.contains(allCarList[i].imei)) {
                    unitCarList.add(allCarList[i])
                }
            }
            newUpdateViewGPS3(unitCarList)
            if (binding.selectedCarGPS3 != null && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE) {
                (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddressGps3(
                    binding.selectedCarGPS3!!.mCarModel.lat,
                    binding.selectedCarGPS3!!.mCarModel.lng
                )
            }
        } else {

            newUpdateViewGPS3(allCarList)
        }
    }

    private fun updateSpeed(
        carDetails: GetCarDetailsModel,
        sensorDetails: MutableLiveData<String>
    ) {
        try {
            if (carDetails.item.pos?.s.toString().equals("0", true)) {
                binding.carSpeed = "0" + " " + getString(R.string.km_h)
                binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speedometer)
            } else {
                binding.carSpeed =
                    carDetails.item.pos?.s.toString() + " " + resources.getString(R.string.km_h)
                binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speed_on_big)
            }
            binding.bottomSheet.txtLastUpdatedTime.text = Utils.getDateFromMillis(
                carDetails.item.lmsg?.t?.toLong()!! * 1000,
                "yyyy-MM-dd hh:mm a"
            )
            if (carDetails.item.sens?.size()!! > 0 && binding.bottomSheet.rvSensors.adapter != null && binding.bottomSheet.rvSensors.adapter!!.itemCount > 0) {
                try {
                    Log.d(TAG, "updateSpeed:sns 1")
                    val mapListwithKeyValue =
                        getKeysSize(JSONObject(carDetails.item.sens.toString()))
                    (binding.bottomSheet.rvSensors.adapter as CarDetailsAdapter).updateValues(
                        mapListwithKeyValue, sensorDetails.value.toString()
                    )
                    binding.bottomSheet.noDataLay.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "updateSpeed: CATCH ${e.message}")
                }
            } else if (carDetails.item.sens?.size()!! > 0) {
                try {
                    Log.d(TAG, "updateSpeed:sns 2")
                    val mapListwithKeyValue =
                        getKeysSize(JSONObject(carDetails.item.sens.toString()))
                    BindAdapter.bindHomeTabList(
                        binding.bottomSheet.rvSensors,
                        mapListwithKeyValue,
                        sensorDetails.value.toString()
                    )

                    binding.bottomSheet.noDataLay.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "updateSpeed: CATCH ${e.message}")
                }
            } else {
                Log.d(TAG, "updateSpeed:sns 3")
                showNoSensFound()
            }
            for (i in 0 until (requireActivity() as MainActivity).homeMapViewModel.carDataList.size) {
                try {
                    if (binding.selectedCar != null && (requireActivity() as MainActivity).homeMapViewModel.carDataList[i].id.toString()
                            .equals(binding.selectedCar!!.mCarModel.id.toString(), true)
                    ) {

                        Log.d(TAG, "updateSpeed:state ")

                        binding.selectedCar!!.mCarModel =
                            (requireActivity() as MainActivity).homeMapViewModel.carDataList[i]

                        if ((requireActivity() as MainActivity).homeMapViewModel.carDataList[i].trip_state.toString()
                                .equals("0", true) ||
                            (requireActivity() as MainActivity).homeMapViewModel.carDataList[i].trip_state?.trim()!!
                                .isEmpty() || carDetails.item.sens?.size() == 0
                        ) {
                            binding.bottomSheet.txtEngineStatus.text = getString(R.string.off)
                            binding.bottomSheet.imgCarEngine.setImageResource(R.drawable.ic_engine_on_off)
                        } else {
                            binding.bottomSheet.txtEngineStatus.text = getString(R.string.on)
                            binding.bottomSheet.imgCarEngine.setImageResource(R.drawable.ic_car_engine_on)
                        }

                        if (((Calendar.getInstance().timeInMillis / 1000) - (binding.selectedCar!!.mCarModel.trip_m?.toLong()
                                ?: 0)) / 60 >= 60
                        ) {
                            binding.bottomSheet.carName.setTextColor(
                                getColor(requireContext(), R.color.color_red)
                            )
                        } else {
                            binding.bottomSheet.carName.setTextColor(
                                getColor(requireContext(), R.color.colorBlack)
                            )
                        }
                        binding.bottomSheet.txtMin.text =
                            Utils.formatDuration(
                                (Calendar.getInstance().timeInMillis / 1000) - (binding.selectedCar!!.mCarModel.trip_m?.toLong()
                                    ?: 0)
                            )

                        DebugLog.e("Car Speed" + binding.selectedCar!!.mCarModel.trip_curr_speed)
                        if (binding.selectedCar!!.mCarModel.trip_curr_speed.toString()
                                .equals("0", true)
                        ) {
                            binding.carSpeed = "0" + " " + getString(R.string.km_h)
                            binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speedometer)
                        } else {
                            binding.carSpeed =
                                binding.selectedCar!!.mCarModel.trip_curr_speed + " " + resources.getString(
                                    R.string.km_h
                                )
                            binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speed_on_big)
                        }
                        break
                    }
                } catch (e: java.lang.Exception) {

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch updateSpeed: ${e.message}")
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateSpeedGps3(
        carDetails: ArrayList<ItemGps3>,
    ) {
        for (item in 0 until carDetails.size) {
            if (binding.selectedCarGPS3 != null && carDetails[item].imei
                    .equals(binding.selectedCarGPS3!!.mCarModel.imei, true)
            ) {
                binding.bottomSheet.txtLastUpdatedTime.text = Utils.getDateFromMillis(
                    getTimeStamp(carDetails[item].dt_server) + tz.getOffset(
                        getTimeStamp(carDetails[item].dt_server)
                    ), "yyyy-MM-dd hh:mm a"
                )
                val timeStamp: Long = getTimeStamp(carDetails[item].dt_server)
                if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                        timeStamp
                    )) / 1000) / 60 >= 60
                ) {
                    binding.bottomSheet.carName.setTextColor(
                        getColor(requireContext(), R.color.color_red)
                    )
                } else {
                    binding.bottomSheet.carName.setTextColor(
                        getColor(requireContext(), R.color.colorBlack)
                    )
                }
                binding.bottomSheet.txtMin.text = Utils.formatDuration(
                    (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                        timeStamp
                    )) / 1000
                )
                Log.d(TAG, "updateSpeedGps3: ${carDetails[item].name} $timeStamp")
                if (carDetails[item].speed == "0") {

                    binding.carSpeed = "0" + " " + getString(R.string.km_h)
                    binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speedometer)

                } else {
                    binding.carSpeed = carDetails[item].speed + " " + resources.getString(
                        R.string.km_h
                    )
                    binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speed_on_big)
                }
                break
            }
            if (binding.selectedCarGPS3 != null) {
                val date =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(binding.selectedCarGPS3!!.mCarModel.dt_server)
                binding.bottomSheet.txtMin.text = Utils.formatDuration(
                    (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((date!!.time) + tz.getOffset(
                        date.time
                    )) / 1000
                )
            }
        }
    }

    @Throws(JSONException::class)
    fun getKeysSize(jsonobj: JSONObject): MutableList<JSONObject> {
        val list = mutableListOf<JSONObject>()
        val hsSensor = mutableSetOf<String>()
        val map = HashMap<String, Any>()
        val keys = jsonobj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            hsSensor.add(key)
            var value = jsonobj.get(key)
            if (value is JSONObject) {
                value = getKeysSize(value)
                list.add(JSONObject(jsonobj.get(key).toString()))
            }
            map[key] = value
        }
        return list
    }

    private fun newUpdateView(carImage: List<Item>) {
        if (map != null && carImage.isNotEmpty()) {
            newAddClusterInManager(carImage)
        }
    }

    private fun newUpdateViewGPS3(carImage: ArrayList<ItemGps3>) {
        if (map != null && carImage.isNotEmpty()) {
            newAddClusterInManagerGPS3(carImage)
        } else {
            (requireActivity() as MainActivity).homeMapViewModel.loading(true)
        }
    }

    private fun newAddClusterInManager(carImage: List<Item?>) {
        var updateLat = 0.0
        var updateLng = 0.0
        val builder = LatLngBounds.Builder()
        var isBuilderNotNull = false
        try {
            Log.d(TAG, "newAddClusterInManager: firstTime $firstTime")
            if (firstTime) {
                Utils.hideProgressBar()
                mClusterManager!!.markerCollection.clear()
                mClusterManager!!.clearItems()
                for (markerOptions in carImage) {
                    if (markerOptions!!.tripToLat != null && markerOptions.tripToLong != null) {
                        firstTime = false
                        isBuilderNotNull = true
                        builder.include(
                            markerOptions.tripToLat?.let {
                                markerOptions.tripToLong?.let { it1 ->
                                    LatLng(
                                        it.toDouble(),
                                        it1.toDouble()
                                    )
                                }
                            }!!
                        )
                        val offsetItem = markerOptions.tripToLat?.let {
                            markerOptions.tripToLong?.let { it1 ->
                                ClusterRender(
                                    it.toDouble(),
                                    it1.toDouble(),
                                    markerOptions
                                )
                            }
                        }
                        updateLat = markerOptions.tripToLat?.toDouble() ?: 0.0
                        updateLng = markerOptions.tripToLong?.toDouble() ?: 0.0
                        mClusterManager!!.addItem(offsetItem)
                    }
                }
                if (carImage.size > 1 && isBuilderNotNull) {
                    val bounds = builder.build()
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 100),
                        1000,
                        null
                    )
                } else if (updateLat > 0 && updateLng > 0) {
                    /*map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(updateLat, updateLng),
                            START_ZOOM
                        )
                    )*/
                    map?.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(updateLat, updateLng))
                                    .zoom(START_ZOOM)
                                    .build()
                            )
                    )
                }
            } else {
                for (markerOptions in carImage) {
                    if (markerOptions!!.tripToLat != null && markerOptions.tripToLong != null) {
                        isBuilderNotNull = true
                        builder.include(markerOptions.tripToLat?.let {
                            markerOptions.tripToLong?.let { it1 ->
                                LatLng(
                                    it.toDouble(),
                                    it1.toDouble()
                                )
                            }
                        }!!)
                        var isAnyUpdate = false
                        var oldLatLong = LatLng(0.0, 0.0)
                        for (item in mClusterManager!!.algorithm.items) {
                            if (item.mCarModel.id.toString()
                                    .equals(markerOptions.id.toString(), true)
                                && item.position != markerOptions.tripToLong?.let {
                                    LatLng(
                                        markerOptions.tripToLat!!.toDouble(),
                                        it.toDouble()
                                    )
                                } && item.mCarModel.trip_curr_speed != markerOptions.trip_curr_speed
                                && item.mCarModel.trip_course != markerOptions.trip_course
                            ) {
                                oldLatLong = item.position
                                markerOptions.tripToLat?.let {
                                    markerOptions.tripToLong?.let { it1 ->
                                        markerOptions.trip_curr_speed?.let { it2 ->
                                            markerOptions.trip_course?.let { it3 ->
                                                item.updateItems(
                                                    it.toDouble(), it1.toDouble(),
                                                    markerOptions
                                                )
                                            }
                                        }
                                    }
                                }
                                mClusterManager!!.updateItem(item)
                                isAnyUpdate = true
                                if (false) {
                                    isAnyUpdate = false
                                    mClusterManager!!.removeItem(item)
                                    mClusterManager!!.addItem(
                                        markerOptions.tripToLat?.let {
                                            markerOptions.tripToLong?.let { it1 ->
                                                ClusterRender(
                                                    it.toDouble(),
                                                    it1.toDouble(),
                                                    markerOptions
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        if (isAnyUpdate) {
                            for (marker in mClusterManager!!.markerCollection.markers) {
                                if (markerOptions.id.toString()
                                        .equals(marker.tag!!.toString(), true)
                                ) {
                                    HRMarkerAnimation(
                                        map!!, 1500
                                    ) {
                                        findParkingRenderer.updateMarker(
                                            marker,
                                            ClusterRender(
                                                markerOptions.tripToLat!!.toDouble(),
                                                markerOptions.tripToLat!!.toDouble(),
                                                markerOptions
                                            )
                                        )
                                        if (binding.selectedCar != null && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE)
                                            setMapPaddingBotttom(binding.bottomSheet.bottomSheet.height.toFloat())
                                    }.animateMarker(
                                        LatLng(
                                            markerOptions.tripToLat!!.toDouble(),
                                            markerOptions.tripToLong!!.toDouble()
                                        ), oldLatLong, marker

                                    )
                                }
                            }
                        }
                    }
                }
            }
            mClusterManager?.cluster()
            if (binding.selectedCar != null && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE) {
                map?.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(
                            map?.cameraPosition.let {
                                CameraPosition.Builder()
                                    .target(binding.selectedCar!!.position)
                                    .zoom(it!!.zoom)
                                    .build()
                            }
                        )
                )
            }
        } catch (e: Exception) {
            DebugLog.e(e.message.toString())
        }
    }

    private fun newAddClusterInManagerGPS3(carImage: ArrayList<ItemGps3>) {
        var updateLat = 0.0
        var updateLng = 0.0
        val builder = LatLngBounds.Builder()
        var isbuilderNotNull = false
        try {
            Log.d(TAG, "newAddClusterInManagerGPS3: firstTime $firstTime")
            if (firstTime) {
                (requireActivity() as MainActivity).homeMapViewModel.loading(false)
                mClusterManagerGPS3!!.markerCollection.clear()
                mClusterManagerGPS3!!.clearItems()
                for (markerOptions in carImage) {
                    if (markerOptions.lat.toDouble() != null && markerOptions.lng.toDouble() != null) {
                        firstTime = false
                        isbuilderNotNull = true
                        builder.include(
                            LatLng(
                                markerOptions.lat.toDouble(),
                                markerOptions.lng.toDouble()
                            )
                        )
                        val offsetItem = ClusterRenderGPS3(
                            markerOptions.lat.toDouble(),
                            markerOptions.lng.toDouble(),
                            markerOptions
                        )
                        updateLat = markerOptions.lat.toDouble()
                        updateLng = markerOptions.lng.toDouble()
                        mClusterManagerGPS3!!.addItem(offsetItem)
                    }
                }
                if (carImage.size > 1 && isbuilderNotNull) {
                    val bounds = builder.build()
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 100),
                        1000,
                        null
                    )
                } else if (updateLat > 0 && updateLng > 0) {
                    /*map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(updateLat, updateLng),
                            START_ZOOM
                        )
                    )*/
                    map?.animateCamera(
                        CameraUpdateFactory
                            .newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(updateLat, updateLng))
                                    .zoom(START_ZOOM)
                                    .build()
                            )
                    )
                }
            } else {
                for (markerOptions in carImage) {
                    isbuilderNotNull = true
                    builder.include(
                        LatLng(
                            markerOptions.lat.toDouble(),
                            markerOptions.lng.toDouble()
                        )
                    )
                    var isAnyUpdate = false
                    var oldLatLong = LatLng(0.0, 0.0)
                    for (item in mClusterManagerGPS3!!.algorithm.items) {
                        if (item.mCarModel.imei
                                .equals(markerOptions.imei, true)
                            && item.position != LatLng(
                                markerOptions.lat.toDouble(),
                                markerOptions.lng.toDouble()
                            ) && item.mCarModel.speed != markerOptions.speed && item.mCarModel.angle != markerOptions.angle

                        ) {
                            oldLatLong = item.position
                            item.updateItems(
                                markerOptions.lat.toDouble(),
                                markerOptions.lng.toDouble(),
                                markerOptions
                            )
                            mClusterManagerGPS3!!.updateItem(item)
                            isAnyUpdate = true
                            if (false) {
                                isAnyUpdate = false
                                mClusterManagerGPS3!!.removeItem(item)
                                mClusterManagerGPS3!!.addItem(
                                    ClusterRenderGPS3(
                                        markerOptions.lat.toDouble(),
                                        markerOptions.lng.toDouble(),
                                        markerOptions
                                    )
                                )
                            }
                        }

                        if (isAnyUpdate) {
                            for (marker in mClusterManagerGPS3!!.markerCollection.markers) {
                                if (markerOptions.imei.equals(marker?.tag.toString(), true)) {
                                    HRMarkerAnimation(
                                        map!!, 1500
                                    ) {
                                        findParkingRendererGPS3.updateMarker(
                                            marker,
                                            ClusterRenderGPS3(
                                                markerOptions.lat.toDouble(),
                                                markerOptions.lng.toDouble(),
                                                markerOptions
                                            )
                                        )
                                        if (binding.selectedCarGPS3 != null && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE)
                                            setMapPaddingBotttom(binding.bottomSheet.bottomSheet.height.toFloat())
                                    }.animateMarker(
                                        LatLng(
                                            markerOptions.lat.toDouble(),
                                            markerOptions.lng.toDouble()
                                        ), oldLatLong, marker
                                    )
                                }
                            }
                        }
                    }
                }
            }
            mClusterManagerGPS3?.cluster()
            if (binding.selectedCarGPS3 != null && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED && binding.bottomSheet.bottomSheet.visibility == View.VISIBLE) {
                map?.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(
                            map?.cameraPosition.let {
                                CameraPosition.Builder()
                                    .target(binding.selectedCarGPS3!!.position)
                                    .zoom(it!!.zoom)
                                    .build()
                            }
                        )
                )
            }
        } catch (e: Exception) {
            DebugLog.e(e.message.toString())
        }
    }


    private fun setUpCluster() {
        mClusterManager = ClusterManager(context, map!!)
        map!!.setOnCameraIdleListener(mClusterManager)
        mClusterManager!!.setOnClusterClickListener(onClusterClickListener)
        mClusterManager!!.setOnClusterItemClickListener(onClusterItemClickListener)
        findParkingRenderer = CarOnMapRenderer(requireActivity(), map!!, mClusterManager!!)
        mClusterManager!!.renderer = findParkingRenderer
        if (!Constants.isFirstTimeApiCall) addCarOnCluster(null)

    }

    private fun setUpClusterGPS3() {
        mClusterManagerGPS3 = ClusterManager(context, map!!)
        map!!.setOnCameraIdleListener(mClusterManagerGPS3)
        mClusterManagerGPS3!!.setOnClusterClickListener(onClusterClickListenerGPS3)
        mClusterManagerGPS3!!.setOnClusterItemClickListener(onClusterItemClickListenerGPS3)
        findParkingRendererGPS3 =
            CarOnMapRendererGPS3(requireActivity(), map!!, mClusterManagerGPS3!!)
        mClusterManagerGPS3!!.renderer = findParkingRendererGPS3
        if (!Constants.isFirstTimeApiCall) addCarOnClusterGPS3(null)

    }

    private var onClusterItemClickListener: ClusterManager.OnClusterItemClickListener<ClusterRender> =
        ClusterManager.OnClusterItemClickListener { model ->
            try {
                binding.selectedCar = model
                val marker = findParkingRenderer.getMarker(model)
                if (marker != null) {
                    map!!.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                model.position.latitude,
                                model.position.longitude
                            ), map!!.cameraPosition.zoom
                        )
                    )
                    if (((Calendar.getInstance().timeInMillis / 1000) - (binding.selectedCar!!.mCarModel.trip_m?.toLong()
                            ?: 0)) / 60 >= 60
                    ) {
                        binding.bottomSheet.carName.setTextColor(
                            getColor(requireContext(), R.color.color_red)
                        )
                    } else {
                        binding.bottomSheet.carName.setTextColor(
                            getColor(requireContext(), R.color.colorBlack)
                        )
                    }
                    previousSelectedMarker = marker
                    mPreviousSelectedClusterItem = model
                    binding.executePendingBindings()
                    bottomSheetViews()
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddress(
                        binding.selectedCar!!.position.longitude.toString(),
                        binding.selectedCar!!.position.latitude.toString()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //  map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(selectedLat, selectedLng)), 1000, null)
            false
        }
    private var onClusterItemClickListenerGPS3: ClusterManager.OnClusterItemClickListener<ClusterRenderGPS3> =
        ClusterManager.OnClusterItemClickListener { model ->
            try {
                binding.selectedCarGPS3 = model
                val marker = findParkingRendererGPS3.getMarker(model)
                if (marker != null) {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                model.position.latitude, model.position.longitude
                            ), map?.cameraPosition!!.zoom
                        )
                    )
                    val timeStamp: Long = getTimeStamp(model.mCarModel.dt_server)
                    if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                            timeStamp
                        )) / 1000) / 60 >= 60
                    ) {
                        binding.bottomSheet.carName.setTextColor(
                            getColor(requireContext(), R.color.color_red)
                        )
                    } else {
                        binding.bottomSheet.carName.setTextColor(
                            getColor(
                                requireContext(), R.color.colorBlack
                            )
                        )
                    }
                    previousSelectedMarker = marker
                    mPreviousSelectedClusterItemGPS3 = model
                    binding.executePendingBindings()
                    bottomSheetViewsGPS3(model)
                    (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddressGps3(
                        binding.selectedCarGPS3!!.mCarModel.lat,
                        binding.selectedCarGPS3!!.mCarModel.lng
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //  map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(selectedLat, selectedLng)), 1000, null)
            false
        }

    private val TAG = "HomeFragment"
    private fun bottomSheetViews() {
//        binding.bottomSheet.bottomSheet.
        try {
            Log.d(TAG, "bottomSheetViews: ${binding.bottomSheet.imgSensorupperArrow}")
            binding.bottomSheet.invisibleView.visibility = View.GONE
            binding.bottomSheet.imgSensorupperArrow.setImageResource(R.drawable.ic_arrow_down)
            binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_upper_arrow)
            binding.bottomSheet.rvSensors.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheet.txtMin.text = Utils.formatDuration(
                (Calendar.getInstance().timeInMillis / 1000) - (binding.selectedCar!!.mCarModel.trip_m?.toLong()
                    ?: 0)
            )
            binding.driverName = "--"
            binding.carSpeed = "0" + " " + getString(R.string.km_h)
            bottomSheetBehavior.isDraggable = false
        } catch (e: Exception) {
            Log.e(TAG, "Catch bottomSheetViews: e1 ${e.message}")
        }
        try {
            binding.bottomSheet.lblSensors.setOnClickListener {
                if (binding.bottomSheet.noDataLay.visibility != View.VISIBLE) {
                    if (binding.bottomSheet.rvSensors.visibility == View.VISIBLE) {
                        binding.bottomSheet.imgSensorupperArrow.setImageResource(
                            R.drawable.ic_arrow_down
                        )
                        binding.bottomSheet.rvSensors.visibility = View.GONE
                    } else {
                        binding.bottomSheet.rvSensors.visibility = View.VISIBLE
                        binding.bottomSheet.imgSensorupperArrow.setImageResource(
                            R.drawable.ic_upper_arrow
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch bottomSheetViews: e2 ${e.message}")
        }
        try {
            binding.bottomSheet.imgTopUpperArrow.setOnClickListener {
                DebugLog.e(bottomSheetBehavior.state.toString() + "")
                when {
                    (binding.bottomSheet.invisibleView.visibility == View.VISIBLE) -> {
                        binding.bottomSheet.invisibleView.visibility = View.GONE
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
                    }
                    (binding.bottomSheet.invisibleView.visibility == View.GONE) -> {
                        binding.bottomSheet.invisibleView.visibility = View.VISIBLE
                        binding.bottomSheet.invisibleView.scrollTo(0, 0)
                        binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_arrow_down)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        val h = binding.bottomSheet.bottomSheet.height.toFloat() + 800
                        setMapPaddingBotttom(h)
                        carMapMoving()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch bottomSheetViews: e3 ${e.message}")
        }
        try {
            binding.bottomSheet.imgGoogleMap.setOnClickListener {
                val urlAddress =
                    "http://maps.google.com/maps?q=" + binding.selectedCar!!.position.latitude.toString() + "," + binding.selectedCar!!.position.longitude.toString() + "(" + binding.selectedCar!!.mCarModel.nm + ")&iwloc=A&hl=es"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                requireActivity().startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch bottomSheetViews: e4 ${e.message}")
        }
        try {
            binding.bottomSheet.btnViewHistory.setOnClickListener {
                hideBottomSheetValues()
                Handler(Looper.getMainLooper()).postDelayed({
                    val bundle = bundleOf(
                        "comingFrom" to "viewHistory",
                        "carName" to binding.selectedCar?.mCarModel?.nm,
                        "carId" to binding.selectedCar?.mCarModel?.id.toString(),
                        "carAddress" to binding.carAddress?.toString()
                    )
                    DebugLog.e("Selected carid" + binding.selectedCar?.mCarModel?.id)
                    findNavController().navigate(
                        R.id.action_homemapFragment_to_vehicleHistoryFragment,
                        bundle
                    )
                }, 200)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Catch bottomSheetViews: e5 ${e.message}")
        }
    }

    // bottonsheet for gps3
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun bottomSheetViewsGPS3(model: ClusterRenderGPS3) {
        try {
            binding.bottomSheet.invisibleView.visibility = View.GONE
            binding.bottomSheet.imgSensorupperArrow.setImageResource(R.drawable.ic_arrow_down)
            binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_upper_arrow)
            binding.bottomSheet.rvSensors.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheet.txtLastUpdatedTime.text = Utils.getDateFromMillis(
                getTimeStamp(model.mCarModel.dt_server) + tz.getOffset(
                    getTimeStamp(model.mCarModel.dt_server)
                ), "yyyy-MM-dd hh:mm a"
            )
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(model.mCarModel.dt_server)
            binding.bottomSheet.txtMin.text = Utils.formatDuration(
                (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((date!!.time) + tz.getOffset(
                    date.time
                )) / 1000
            )
            Log.d("TAG", "bottomSheetViewsGPS3: " + model.mCarModel.speed + "km/h")
            binding.bottomSheet.carName.text = model.mCarModel.name

            bottomSheetBehavior.isDraggable = false
        } catch (e: Exception) {
            Log.e(TAG, "bottomSheetViewsGPS3: e1 ${e.message}")
        }
        try {
            if (binding.selectedCarGPS3?.mCarModel?.speed == "0") {
                binding.carSpeed = "0" + " " + getString(R.string.km_h)
                binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speedometer)
            } else {
                binding.carSpeed =
                    binding.selectedCarGPS3!!.mCarModel.speed + " " + resources.getString(
                        R.string.km_h
                    )
                binding.bottomSheet.imgSpeedometer.setImageResource(R.drawable.ic_speed_on_big)
            }
        } catch (e: Exception) {
            Log.e(TAG, "bottomSheetViewsGPS3: e2 ${e.message}")
        }
        try {
            binding.bottomSheet.lblSensors.setOnClickListener {
                if (binding.bottomSheet.noDataLay.visibility != View.VISIBLE) {
                    if (binding.bottomSheet.rvSensors.visibility == View.VISIBLE) {
                        binding.bottomSheet.imgSensorupperArrow.setImageResource(R.drawable.ic_arrow_down)
                        binding.bottomSheet.rvSensors.visibility = View.GONE
                    } else {
                        binding.bottomSheet.rvSensors.visibility = View.VISIBLE
                        binding.bottomSheet.imgSensorupperArrow.setImageResource(R.drawable.ic_upper_arrow)

                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "bottomSheetViewsGPS3: e3 ${e.message}")
        }
        try {
            binding.bottomSheet.imgTopUpperArrow.setOnClickListener {
                DebugLog.e(bottomSheetBehavior.state.toString() + "")
                when {
                    (binding.bottomSheet.invisibleView.visibility == View.VISIBLE) -> {
                        binding.bottomSheet.invisibleView.visibility = View.GONE
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
                    }
                    (binding.bottomSheet.invisibleView.visibility == View.GONE) -> {
                        binding.bottomSheet.invisibleView.visibility = View.VISIBLE
                        binding.bottomSheet.invisibleView.scrollTo(0, 0)
                        binding.bottomSheet.imgTopUpperArrow.setImageResource(R.drawable.ic_arrow_down)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        val h = binding.bottomSheet.bottomSheet.height.toFloat() + 800
                        setMapPaddingBotttom(h)
                        carMapMoving()
                        //handler2.removeCallbacks(updateTextTask)
                    }
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "bottomSheetViewsGPS3: e4 ${e.message}")
        }
        try {
            binding.bottomSheet.imgGoogleMap.setOnClickListener {
                val urlAddress: String =
                    "http://maps.google.com/maps?q=" + binding.selectedCarGPS3!!.position.latitude.toString() + "," + binding.selectedCarGPS3!!.position.longitude.toString() + "(" + binding.selectedCarGPS3!!.mCarModel.name + ")&iwloc=A&hl=es"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                requireActivity().startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "bottomSheetViewsGPS3: e5 ${e.message}")
        }

        binding.bottomSheet.btnViewHistory.setOnClickListener {
            try {
                hideBottomSheetValues()
                Handler(Looper.myLooper()!!).postDelayed({
                    try {
                        val bundle = bundleOf(
                            "comingFrom" to "viewHistory",
                            "carName" to binding.selectedCarGPS3?.mCarModel?.name,
                            "carId" to binding.selectedCarGPS3?.mCarModel?.imei,
                            "carAddress" to binding.carAddress?.toString()
                        )
                        findNavController().navigate(
                            R.id.action_homemapFragment_to_vehicleHistoryFragment, bundle
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "bottomSheetViewsGPS3: e6 ${e.message}")
                    }
                }, 200)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "bottomSheetViewsGPS3: e7 ${e.message}")
            }
        }
    }

    private fun showNoSensFound() {
        binding.bottomSheet.rvSensors.visibility = View.GONE
        binding.bottomSheet.imgSensorupperArrow.setImageResource(R.drawable.ic_upper_arrow)
        binding.bottomSheet.noDataLay.visibility = View.VISIBLE
        binding.bottomSheet.txtEngineStatus.text = requireContext().getString(R.string.off)
        binding.bottomSheet.imgCarEngine.setImageResource(R.drawable.ic_engine_on_off)
    }

    private var onClusterClickListener =
        ClusterManager.OnClusterClickListener<ClusterRender> {
            val builder = LatLngBounds.builder()
            val collection: Collection<ClusterRender> = it.items as Collection<ClusterRender>
            for (item in collection) {
                builder.include(item.position)
            }
            val bounds = builder.build()
            map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
            true
        }
    private var onClusterClickListenerGPS3 =
        ClusterManager.OnClusterClickListener<ClusterRenderGPS3> {
            val builder = LatLngBounds.builder()
            val collection: Collection<ClusterRenderGPS3> =
                it.items as Collection<ClusterRenderGPS3>
            for (item in collection) {
                builder.include(item.position)
            }
            val bounds = builder.build()
            map?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null
            )
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // checkLocationSettings()
                    (requireActivity() as MainActivity).mapSettings(map!!)
                    map?.isTrafficEnabled =
                        MyPreference.getValueBoolean(
                            PrefKey.IS_TRAFFIC,
                            false
                        )
                }
            }
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}