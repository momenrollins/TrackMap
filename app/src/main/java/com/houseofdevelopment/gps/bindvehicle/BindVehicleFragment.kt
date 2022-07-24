package com.houseofdevelopment.gps.bindvehicle

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.bindvehicle.model.BindDriverRequest
import com.houseofdevelopment.gps.databinding.FragmentBindVehicleBinding
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.network.ApiClientForBrainvire
import com.houseofdevelopment.gps.network.model.ErrorWrapper
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.*
import com.houseofdevelopment.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import kotlinx.android.synthetic.main.layout_custom_action_bar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class BindVehicleFragment : BaseFragment() {

    lateinit var binding: FragmentBindVehicleBinding
    lateinit var bindVehicleAdapter: BindVehicleAdapter
    lateinit var viewmodel: VehiclesListViewModel
    private var bindDriverRequest: BindDriverRequest? = null
    private var carDetailList = ArrayList<Item>()
    var currentlatitude: Double = 0.0
    var currentlongitude: Double = 0.0
    private var client: FusedLocationProviderClient? = null

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    companion object {
        private val REQUEST_CHECK_SETTINGS: Int = 3423
        private val PERMISSION_LOCATION_CODE: Int = 3233
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, com.houseofdevelopment.gps.R.layout.fragment_bind_vehicle, container, false)

        handleActionBar(com.houseofdevelopment.gps.R.string.bind_to_vehicle)
        (activity as MainActivity).toolbar.add_vehicle.visibility = View.VISIBLE
        if (Utils.getCarListingData(requireContext()) != null
            && Utils.getCarListingData(requireContext()).items.size > 0
        )
            carDetailList = Utils.getCarListingData(requireContext()).items
        viewmodel = ViewModelProvider(this).get(VehiclesListViewModel::class.java)
        binding.lifecycleOwner = this

        getCurrentLocation()

        addObserver()

        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getCurrentLocation() {

        client = LocationServices.getFusedLocationProviderClient(activity!!)
        if (ActivityCompat.checkSelfPermission(
                context!!,
                permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            client!!.lastLocation.addOnSuccessListener(activity!!) { location ->
                if (location != null) {
                    currentlatitude = location.latitude
                    currentlongitude = location.longitude
                }
            }
        }
        createLocationRequest()
        buildLocationSettingsRequest()
        requestPermission()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), permission.ACCESS_FINE_LOCATION
                )
            ) {
                requestPermissions(
                    arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_CODE
                )
            } else {
                requestPermissions(
                    arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_CODE
                )
            }
        } else {
            checkLocationSettings()
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun addObserver() {
        initRecyclerView()
        (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(this) {
            it.let {
                //bindVehicleAdapter.notifyDataSetChanged()
                val vehicalList = Utils.getCarListingData(requireContext()).items
                for (i in 0 until carDetailList!!.size) {
                    for (j in 0 until vehicalList!!.size) {
                        if (vehicalList[j].id.toString()
                                .equals(carDetailList!![i]!!.id.toString(), true)
                        ) {
                            vehicalList!![j].isExpanded = carDetailList!![i]!!.isExpanded
                            vehicalList!![j].isSelected = carDetailList!![i]!!.isSelected
                        }
                    }
                }
                carDetailList!!.clear()
                carDetailList!!.addAll(vehicalList)
                bindVehicleAdapter.filter.filter(binding.etSearch.text.toString());
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initRecyclerView() {
        val handlerInsertDialog = object : CommonAlertDialog.InsertCodeInterface {
            override fun onbindClick(selected: String, id: Int) {
                // call api for bind driver
                callApiForBindDriver(selected, id, "Code")
            }
        }

        bindVehicleAdapter = BindVehicleAdapter(carDetailList, checkForBiometrics(),
            checkForFaceBiometrics(),
            object : BindVehicleAdapter.BindItemClick {
                override fun onBindOptionClick(opt: String, position: Int, model: Item) {
                    val startPoint = Location("locationA")
                    startPoint.latitude = model.tripToLat?.toDouble() ?: 0.0
                    startPoint.longitude = model.tripToLong?.toDouble() ?: 0.0

                    val endPoint = Location("locationA")
                    endPoint.latitude = currentlatitude
                    endPoint.longitude = currentlongitude
                    when {
                        opt.equals("Code", true) -> {
                            if (startPoint.distanceTo(endPoint) < 500) {
                                model.id?.let {
                                    CommonAlertDialog.showInsertCodeAlerter(
                                        context, handlerInsertDialog, it
                                    )
                                }
                            } else
                                Toast.makeText(
                                    context!!, getString(com.houseofdevelopment.gps.R.string.you_are_far_from_the_vehicle), Toast.LENGTH_SHORT
                                ).show()

                            /*  model.id?.let {
                                    CommonAlertDialog.showInsertCodeAlerter(
                                        context,
                                        handlerInsertDialog,
                                        it
                                    )
                                }*/

                        }
                        opt.equals("Finger", true) -> {
                            val executor = ContextCompat.getMainExecutor(requireContext())
                            val biometricPrompt = BiometricPrompt(requireActivity(), executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationError(
                                        errorCode: Int,
                                        errString: CharSequence
                                    ) {
                                        super.onAuthenticationError(errorCode, errString)
                                        Toast.makeText(
                                            requireContext(), getString(com.houseofdevelopment.gps.R.string.Device_not_supported), Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onAuthenticationSucceeded(
                                        result: BiometricPrompt.AuthenticationResult
                                    ) {
                                        super.onAuthenticationSucceeded(result)
                                        if (startPoint.distanceTo(endPoint) < 500) {
                                            model.id?.let { callApiForBindDriver("", it, "Finger") }
                                        } else
                                            Toast.makeText(
                                                context!!, getString(com.houseofdevelopment.gps.R.string.you_are_far_from_the_vehicle), Toast.LENGTH_SHORT
                                            ).show()
//                                        model.id?.let { callApiForBindDriver("", it, "Finger") }
                                    }

                                    override fun onAuthenticationFailed() {
                                        super.onAuthenticationFailed()

                                    }
                                })
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Finger authentication for \"" + getString(com.houseofdevelopment.gps.R.string.app_name) + "\"")
                                .setSubtitle("Confirm your fingerprint to authenticate.")
                                .setNegativeButtonText(getString(com.houseofdevelopment.gps.R.string.cancel))
                                .build()
                            biometricPrompt.authenticate(promptInfo)
                        }
                        opt.equals("Face", true) -> {
                            //
                            val executor = ContextCompat.getMainExecutor(requireContext())
                            val biometricPrompt = BiometricPrompt(requireActivity(), executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationError(
                                        errorCode: Int,
                                        errString: CharSequence
                                    ) {
                                        super.onAuthenticationError(errorCode, errString)
                                        Toast.makeText(
                                            requireContext(), getString(com.houseofdevelopment.gps.R.string.Device_not_supported), Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onAuthenticationSucceeded(
                                        result: BiometricPrompt.AuthenticationResult
                                    ) {
                                        super.onAuthenticationSucceeded(result)
                                        if (startPoint.distanceTo(endPoint) < 500) {
                                            model.id?.let { callApiForBindDriver("", it, "Face") }
                                        } else
                                            Toast.makeText(
                                                context!!, getString(com.houseofdevelopment.gps.R.string.you_are_far_from_the_vehicle), Toast.LENGTH_SHORT
                                            ).show()
//                                        model.id?.let { callApiForBindDriver("", it, "Face") }
                                    }

                                    override fun onAuthenticationFailed() {
                                        super.onAuthenticationFailed()

                                    }
                                })
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Face authentication for \"" + getString(com.houseofdevelopment.gps.R.string.app_name) + "\"")
                                .setSubtitle("Confirm your face to authenticate.")
                                .setNegativeButtonText(getString(com.houseofdevelopment.gps.R.string.cancel))
                                .setConfirmationRequired(true)
                                .setDeviceCredentialAllowed(false)
                                .build()

                            biometricPrompt.authenticate(promptInfo)
                        }
                    }

                }
            })
        binding.rvBindVehicle.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBindVehicle.adapter = bindVehicleAdapter
        binding.belowLL.visibility = View.GONE

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                /*  var filterList = mutableListOf<MapListDataModel.Item>()
                  for (name in carDetailList!!) {
                      if (name!!.nm.toLowerCase(Locale.US).trim().contains(editable.toString().toLowerCase(Locale.US).trim())) {
                          filterList.add(name)
                      }
                  }

                  bindVehicleAdapter.updateList(filterList)*/
                bindVehicleAdapter.filter.filter(editable.toString());
            }
        })

        (activity as MainActivity).add_vehicle.setOnClickListener {
            CommonAlertDialog.showBindUserAlerter(
                context,
                object : CommonAlertDialog.BindUserInterface {
                    override fun onbinditemClick(selected: String) {
                        when {
                            selected == "code" -> {

                                findNavController().navigate(
                                    BindVehicleFragmentDirections.actionBindVehicleFragmentToAddBindUserFragment(
                                        selected
                                    )
                                )
                            }
                            selected == "fingerprint" -> {
                                val executor = ContextCompat.getMainExecutor(requireContext())
                                val biometricPrompt = BiometricPrompt(requireActivity(), executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationError(
                                            errorCode: Int,
                                            errString: CharSequence
                                        ) {
                                            super.onAuthenticationError(errorCode, errString)
                                            Toast.makeText(
                                                requireContext(),
                                                getString(com.houseofdevelopment.gps.R.string.Device_not_supported),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onAuthenticationSucceeded(
                                            result: BiometricPrompt.AuthenticationResult
                                        ) {
                                            super.onAuthenticationSucceeded(result)
                                            findNavController().navigate(
                                                BindVehicleFragmentDirections.actionBindVehicleFragmentToAddBindUserFragment(
                                                    selected
                                                )
                                            )
                                        }

                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Finger authentication for \"" + getString(com.houseofdevelopment.gps.R.string.app_name) + "\"")
                                    .setSubtitle("Confirm your fingerprint to authenticate.")
                                    .setNegativeButtonText(getString(com.houseofdevelopment.gps.R.string.cancel))
                                    .build()

                                biometricPrompt.authenticate(promptInfo)
                            }
                            selected == "face" -> {
                                val executor = ContextCompat.getMainExecutor(requireContext())
                                val biometricPrompt = BiometricPrompt(requireActivity(), executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationError(
                                            errorCode: Int,
                                            errString: CharSequence
                                        ) {
                                            super.onAuthenticationError(errorCode, errString)
                                            Toast.makeText(
                                                requireContext(), getString(com.houseofdevelopment.gps.R.string.Device_not_supported), Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onAuthenticationSucceeded(
                                            result: BiometricPrompt.AuthenticationResult
                                        ) {
                                            super.onAuthenticationSucceeded(result)
                                            findNavController().navigate(
                                                BindVehicleFragmentDirections.actionBindVehicleFragmentToAddBindUserFragment(
                                                    selected
                                                )
                                            )
                                        }

                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()

                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Face authentication for \"" + getString(com.houseofdevelopment.gps.R.string.app_name) + "\"")
                                    .setSubtitle("Confirm your face to authenticate.")
                                    .setNegativeButtonText(getString(com.houseofdevelopment.gps.R.string.cancel))
                                    .setConfirmationRequired(true)
                                    .build()

                                biometricPrompt.authenticate(promptInfo)
                            }
                        }
                    }
                },
                checkForBiometrics(),
                checkForFaceBiometrics()
            )
        }
    }

    private fun checkForBiometrics(): Boolean {
        var canAuthenticate = true
        /*    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT <= 29) {
                    val keyguardManager: KeyguardManager = context?.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                    val packageManager: PackageManager = requireContext().packageManager
                    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                        canAuthenticate = false
                    }
                    if (!keyguardManager.isKeyguardSecure) {
                        canAuthenticate = false
                    }
                } else {
                    *//*val biometricManager: BiometricManager? =
                        context?.getSystemService(BiometricManager::class.java)
                if (biometricManager == null) canAuthenticate = false
                if (biometricManager != null && biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                    canAuthenticate = false
                }*//*
                when (BiometricManager.from(requireContext()).canAuthenticate(BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS ->
                        canAuthenticate = true
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                        canAuthenticate = false
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        canAuthenticate = false
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                        canAuthenticate = false
                }
            }
        } else {
            canAuthenticate = false
        }*/
        return canAuthenticate
    }

    @SuppressLint("VisibleForTests")
    private fun checkForFaceBiometrics(): Boolean {
        var canAuthenticate = true
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT <= 29) {
                val hasFaceBiometric = requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
                val keyguardManager: KeyguardManager = context?.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                if (!hasFaceBiometric) {
                    canAuthenticate = false
                }
                if (!keyguardManager.isKeyguardSecure) {
                    canAuthenticate = false
                }
            } else {
                when (BiometricManager.from(requireContext()).canAuthenticate(BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS ->
                        canAuthenticate = true
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                        canAuthenticate = false
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                        canAuthenticate = false
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                        canAuthenticate = false
                }
            }
        } else {
            canAuthenticate = false
        }*/
        return canAuthenticate
    }

    private fun callApiForBindDriver(selected: String, id: Int, bindType: String) {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Utils.showProgressBar(requireContext())

        bindDriverRequest = BindDriverRequest()
        bindDriverRequest?.driverId = " "
        bindDriverRequest?.unitId = id.toString()
        bindDriverRequest?.resourceId =
            MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toString()
        bindDriverRequest?.userUuid = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
        bindDriverRequest?.interfaceMode = "mobile"
        bindDriverRequest?.bind_type = bindType
        if (bindType.lowercase(Locale.US).equals("code", true)) bindDriverRequest?.passcode =
            selected

        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            } else {

                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callBindDriver(bindDriverRequest)
                    if (response.body()?.meta?.code == 200) {
                        Utils.hideProgressBar()
                        Toast.makeText(
                            requireContext(),
                            response.body()?.meta?.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
//                           findNavController().navigateUp()
                        //  findNavController().navigate(com.houseofdevelopment.gps.R.id.bindVehicleFragment)
                    } else {
                        val body = response.errorBody()
                        val bodyString = body?.toString()
                        val responseWrapper =
                            Gson().fromJson<ErrorWrapper>(bodyString, ErrorWrapper::class.java)
                        Utils.hideProgressBar()
                        Toast.makeText(
                            requireContext(), responseWrapper.meta?.message, Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
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
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(mLocationSettingsRequest)

        task.addOnSuccessListener { locationSettingsResponse ->
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {

                    /*Show the dialog by calling startResolutionForResult(), and check the result
                    in onActivityResult().*/
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null
                    );
                    /*exception.startResolutionForResult(this,
                        REQUEST_CHECK_SETTINGS)*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        /*val result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            mLocationSettingsRequest
        )
        result.setResultCallback(this)*/
    }

    private fun startLocationUpdates() {
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(), permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            client?.requestLocationUpdates(
                mLocationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.locations.forEach {
                            currentlatitude = it.latitude
                            currentlongitude = it.longitude
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationSettings()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    startLocationUpdates()
                }
                Activity.RESULT_CANCELED -> {
                    DebugLog.i("User chose not to make required location settings changes.")
                }
            }
        }
    }
}
