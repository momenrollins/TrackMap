package com.trackmap.gps.history.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import com.trackmap.gps.R
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.trackmap.gps.MainActivity
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentVehicleHistoryNewBinding
import com.trackmap.gps.vehicallist.model.ItemGps3
import com.trackmap.gps.history.viewmodel.HistoryListViewModel
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.homemap.ui.ClusterRender
import com.trackmap.gps.homemap.ui.ClusterRenderGPS3
import com.trackmap.gps.hrmovecarmarkeranimation.AnimationClass.HRMarkerAnimation
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.CommonAlertDialog
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.utils.Utils.getDates
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.maps.model.LatLng
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.DataValues.tz
import com.trackmap.gps.history.adapter.HistoryCarStatusAdapterGps3
import com.trackmap.gps.history.adapter.HistoryCarStatusAdapter
import com.trackmap.gps.history.adapter.HistoryDateTimeAdapter
import com.trackmap.gps.history.model.*
import com.trackmap.gps.track.viewmodel.TrackViewModel
import org.json.JSONObject
import java.lang.Exception


import kotlin.collections.ArrayList

import java.text.DateFormat


/**
 * A simple [Fragment] subclass.
 */
class VehicleHistoryNewFragment : BaseFragment(), OnMapReadyCallback,
    ResultCallback<LocationSettingsResult>,
    GoogleApiClient.ConnectionCallbacks {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var recyclerViewState: Parcelable? = null

    private var isAddreadyAdded: Boolean = true
    private var marker: Marker? = null
    lateinit var binding: FragmentVehicleHistoryNewBinding
    lateinit var dateTimeAdapter: HistoryDateTimeAdapter
    lateinit var carStatusAdapter: HistoryCarStatusAdapter
    lateinit var carStatusAdapterGps3: HistoryCarStatusAdapterGps3
    private var historyList = mutableListOf<TripDetails.FirstObj>()
    private var map: GoogleMap? = null
    private var inputFormat = "yyyy-MM-dd HH:mm:ss"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private var mRequestingLocationUpdates: Boolean = false
    private var carDetail: Item? = null
    private var carDetailGps3: ItemGps3? = null

    //    private var tripDetailList: MutableList<TripDetails.FirstObj>? = null
    private lateinit var viewmodel: HistoryListViewModel
    private lateinit var trackviewmodel: TrackViewModel
    private var fromInterval: Long = 0
    private var toInterval: Long = 0
    private var carId = ""
    private var tripData: String? = ""
    private var carLat: Double = 0.0
    private var carLng: Double = 0.0
    private var isPinviewVisible = true
    private var bounds: LatLngBounds? = null
    private var isFirstTime = true
    private var pinLocation = LatLng(0.0, 0.0)
    private var listHLatlng: ArrayList<LatLng> = ArrayList()
    private var pathHLatlng: ArrayList<LatLng> = ArrayList()

    private var selectedDateString = ""
    private val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))

    @SuppressLint("SimpleDateFormat")
    val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)
    var mDate = currentDate

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    @SuppressLint("SwitchIntDef")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_vehicle_history_new, container, false
        )
        MyPreference.setValueBoolean(PrefKey.isSelected, true)

        loadMap()
        binding.lifecycleOwner = viewLifecycleOwner
        viewmodel = ViewModelProvider(this).get(HistoryListViewModel::class.java)
        trackviewmodel = ViewModelProvider(this).get(TrackViewModel::class.java)

        addObserver()
        handleActionBarHidePlusIcon(R.string.history)
        if (serverData.contains("s3"))
            getArgumentDataGps3()
        else
            getArgumentData()
        initializeDateAdapters()
        initGoogleAPIClient()
        createLocationRequest()
        buildLocationSettingsRequest()
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.constCardetails)
        bottomSheetBehavior.peekHeight = 250
        binding.constCardetails.setOnClickListener {
            Log.d(TAG, "onCreateView:bs ${bottomSheetBehavior.state}")
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // handle onSlide
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                setMapPaddingBotttom(off)
                carMapMoving()
            }

            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val h = bottomSheet.height.toFloat()
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.mapView.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.mapView.setPadding(0, 0, 0, 10)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    else -> {
                        setMapPaddingBotttom(h)
                    }
                }
                carMapMoving()
            }
        })
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        if (serverData.contains("s3"))
            initializeCarAdaptersGps3()
        else initializeCarAdapters()

        return binding.root
    }

    private fun initializeCarAdaptersGps3() {
        val handlerGps3 = object : HistoryCarStatusAdapterGps3.HistoryTripItemListener {
            override fun onCarImageClicked(
                routeRoot: RouteRootGps3,
                tripList: TripDetailsGps3,
                carClick: Boolean
            ) {
                showPathGps3(routeRoot, tripList, carClick)
            }
        }
        carStatusAdapterGps3 = HistoryCarStatusAdapterGps3(requireContext(), handlerGps3)
        binding.rvCarHistory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCarHistory.adapter = carStatusAdapterGps3
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getArgumentDataGps3() {
        if (arguments != null) {
            val comingFrom = arguments?.getString("comingFrom")
            if (comingFrom?.equals("viewHistory")!!) {
                carId = arguments?.getString("carId")!!
                DebugLog.e("CarId==" + carId)
                binding.carName.text = arguments!!.getString("carName")
                binding.txtAddress.text = arguments!!.getString("carAddress")
                carDetailGps3 =
                    Utils.getCarListingDataGps3(requireContext()).items.filter { a -> a.imei == carId }[0]
                setCarDetailDataGps3(carDetailGps3!!)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCarDetailDataGps3(car: ItemGps3) {
        val dateTimeStamp: Long = getTimeStamp(car.dt_server)
        if (car.speed == "0") {
            binding.carDistance.visibility = View.GONE
        } else {
            binding.carDistance.visibility = View.VISIBLE
            binding.carDistance.text = "${car.dist} km"
        }
        carLat = car.lat.toDouble()
        carLng = car.lng.toDouble()

        binding.txtMin1.text = Utils.formatDuration(
            (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((dateTimeStamp) + tz.getOffset(
                dateTimeStamp
            )) / 1000
        )
        if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((dateTimeStamp) + tz.getOffset(
                dateTimeStamp
            )) / 1000) / 60 >= 60
        ) {
            binding.carName.setTextColor(getColor(requireContext(), R.color.color_red))
        } else {
            binding.carName.setTextColor(getColor(requireContext(), R.color.colorBlack))
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val time: Long

        if (car.speed.toDouble() > 0)
            time = getTimeStamp(car.dt_last_move) + tz.getOffset(
                dateTimeStamp
            )
        else
            time = getTimeStamp(car.dt_last_stop) + tz.getOffset(
                dateTimeStamp
            )
        val dd = Date(time)
        val date1 = sdf.format(dd)
        val date2 = sdf.format(Date())

        if (date1.equals(date2)) {
            val sdDateTime = SimpleDateFormat("hh:mm a", Locale.US)
            val time = sdDateTime.format(Date(time))
            binding.carTime1.text = time
        } else {
            val sdDate = SimpleDateFormat("dd MMM", Locale.US)
            binding.carTime1.text = sdDate.format(dd).toString()
        }
        binding.carTime2.text = Utils.formatDurationFullValues(
            (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis - time) / 1000
        )
        (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddressGps3(
            carLat.toString(), carLng.toString()
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    private fun carMapMoving() {
        if (bounds != null) {
            map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds!!, 100), 1000, null)
            map?.moveCamera(CameraUpdateFactory.newLatLng(pinLocation))
        } else {
            map?.moveCamera(CameraUpdateFactory.newLatLng(pinLocation))
            if (map?.cameraPosition?.zoom != 14f) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 14f))
            }
        }
    }

    private fun setMapPaddingBotttom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        map?.setPadding(0, 0, 0, Math.round(offset * maxMapPaddingBottom))
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                map?.uiSettings?.isZoomControlsEnabled = false
            }
            else -> {
                map?.uiSettings?.isZoomControlsEnabled = true
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getArgumentData() {
        if (arguments != null) {
            val comingFrom = arguments?.getString("comingFrom")
            if (comingFrom?.equals("viewHistory")!!) {
                carId = arguments?.getString("carId")!!
                DebugLog.e("CarId==" + carId)
                binding.carName.text = arguments!!.getString("carName")
                binding.txtAddress.text = arguments!!.getString("carAddress")
                carDetail =
                    Utils.getCarListingData(requireContext()).items.filter { a -> a.id.toString() == carId }[0]
                DebugLog.e("getArgumentData==" + carDetail!!.nm)
                setCarDetailData(carDetail!!)
            }
        }
    }

    //this foe car time details
    @SuppressLint("SetTextI18n")
    private fun setCarDetailData(car: Item) {
        Glide.with(this)
            .load("http://www.avltracmap.com" + car.uri)
            .placeholder(R.drawable.default_car)
            .into(binding.carImage)

        if (car.trip_curr_speed.equals("0")) {
            binding.carDistance.visibility = View.GONE
        } else {
            binding.carDistance.visibility = View.VISIBLE
        }

        val dist = ((car.trip_distance?.toDouble() ?: 0.0) / 1000)
        val roundOff = (df.format(dist).toDouble())

        binding.carDistance.text = "$roundOff km"

        carLat = car.tripFromLat?.toDouble() ?: 0.0
        carLng = car.tripFromLong?.toDouble() ?: 0.0

        // Last Update time calculation
        //get current time
        binding.txtMin1.text = Utils.formatDuration(
            (Calendar.getInstance().timeInMillis / 1000) - (car.trip_m?.toLong()
                ?: 0)
        )
        if (((Calendar.getInstance().timeInMillis / 1000) - (car.trip_m?.toLong()
                ?: 0)) / 60 >= 60
        ) {
            binding.carName.setTextColor(getColor(requireContext(), R.color.color_red))
        } else {
            binding.carName.setTextColor(getColor(requireContext(), R.color.colorBlack))
        }
        binding.carTime2.text = Utils.formatDurationFullValues(
            (Calendar.getInstance().timeInMillis / 1000) - (car.tripFromT?.toLong()
                ?: 0)
        )

        val timeInterval = car.tripToT?.toLong()?.let {
            car.trip_m?.toLong()?.minus(
                it.minus(car.tripFromT!!.toLong())
            )
        }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val dd = Date(timeInterval!! * 1000L)
        val date1 = sdf.format(dd)
        val date2 = sdf.format(Date())

        if (date1.equals(date2)) {
            // Log.d("Date status", "greater")
            val sdDateTime = SimpleDateFormat("hh:mm a", Locale.US)
            val time = sdDateTime.format(Date(timeInterval * 1000L))
            binding.carTime1.text = time
        } else {
            // Log.d("Date status", "less")
            val sdDate = SimpleDateFormat("dd MMM", Locale.US)
            binding.carTime1.text = sdDate.format(dd).toString()
        }

        //    lastPinData = car.trp
        if (isAddreadyAdded && historyList.size > 1 && car.trip_curr_speed?.toInt() ?: 0 > 0 && Utils.getDateFromMillis(
                toInterval * 1000,
                "dd-MM-yyyy"
            ).equals(Utils.getDateFromMillis(System.currentTimeMillis(), "dd-MM-yyyy"))
        ) {


            if (historyList[historyList.size - 1].tripEventType == "p") {
                historyList[historyList.size - 1].toList.to_t = car.tripFromT?.toDouble() ?: 0.0
                historyList[historyList.size - 1].toList.to_x = car.tripFromLong?.toDouble() ?: 0.0
                historyList[historyList.size - 1].toList.to_y = car.tripFromLat?.toDouble() ?: 0.0
            }
            DebugLog.e("Trip Data Added " + historyList[historyList.size - 1].tripEventType)
            DebugLog.e("Trip Data historyList ==" + historyList.size)
            val lastTripData = TripDetails().FirstObj()

            val from = TripDetails().From()
            val to = TripDetails().To()

            from.from_t = car.tripFromT?.toDouble() ?: 0.0
            from.from_x = car.tripFromLong?.toDouble() ?: 0.0
            from.from_y = car.tripFromLat?.toDouble() ?: 0.0

            to.to_x = car.tripToLong?.toDouble() ?: 0.0
            to.to_y = car.tripToLat?.toDouble() ?: 0.0

            to.to_t = (Calendar.getInstance().timeInMillis / 1000).toDouble()

            lastTripData.fromList = from
            lastTripData.toList = to
            lastTripData.history_distance = car.trip_distance?.toDouble() ?: 0.0
            lastTripData.history_curr_speed = car.trip_curr_speed?.toInt() ?: 0
            lastTripData.history_max_speed = car.trip_max_speed?.toInt()
            lastTripData.history_m = car.trip_m?.toInt() ?: 0
            lastTripData.tripEventType = "trip"

            historyList.add(lastTripData)
            isAddreadyAdded = false
            DebugLog.e("Trip Data historyList " + historyList.size)
            setUpCarAdapterClass(historyList)

        }
        (requireActivity() as MainActivity).homeMapViewModel.callApiForCarAddress(
            carLng.toString(), carLat.toString()
        )

    }

    var sDate = ""
    var eDate = ""
    var startDate = ""
    var endDate = ""
    var selString = ""

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initializeDateAdapters() {

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        // Item click listener event
        val handler = object :
            HistoryDateTimeAdapter.DateItemClickHandler {
            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDateItemClickListener(value: Date, position: Int) {
                selString = formatter.format(value)
                val formatDate2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val tz = TimeZone.getDefault()

                selectedDateString = selString
                val startTime = "00:00:00"
                val endTime = "23:59:59"

                sDate = "$selString $startTime"
                eDate = "$selString $endTime"
                val format: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

                val date1: Date = format.parse(sDate)!!
                startDate = Utils.getDateFromMillis(
                    ((getTimeStamp(formatDate2.format(date1))) - tz.getOffset(
                        getTimeStamp(formatDate2.format(date1))
                    )), "yyyy-MM-dd HH:mm:ss"
                ).toString()

                val date2: Date = format.parse(eDate)!!
                endDate = Utils.getDateFromMillis(
                    ((getTimeStamp(formatDate2.format(date2))) - tz.getOffset(
                        getTimeStamp(formatDate2.format(date2))
                    )), "yyyy-MM-dd HH:mm:ss"
                ).toString()

                fromInterval = Utils.getMillisFromDate("$selString $startTime", inputFormat)
                toInterval = Utils.getMillisFromDate("$selString $endTime", inputFormat)
                getHistoryDetails(startDate, endDate!!)
            }
        }
        val dates: ArrayList<Date> = getDates("2019-11-22", Date())
        dateTimeAdapter = HistoryDateTimeAdapter(context!!, dates, handler)
        binding.rvDate.layoutManager = LinearLayoutManager(
            context!!,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        (binding.rvDate.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rvDate.adapter = dateTimeAdapter
        dateTimeAdapter.firstIndexClick()

    }

    private fun initializeCarAdapters() {
        // Item click listener event
        val handler = object : HistoryCarStatusAdapter.HistoryTripListener {
            override fun onCarImageClicked(
                position: Int,
                fromT: Double,
                toT: Double,
                isEmpity: Boolean
            ) {

                if (isEmpity) {
                    trackviewmodel.callApiForTrip(carId, fromT.toLong(), toT.toLong())
                    trackviewmodel.liveTripData.observe(viewLifecycleOwner) {
                        Log.d(TAG, "onCarImageClicked:${it.length()} ")

                        if (it != null && it.length() > 0) {
                            pathHLatlng.clear()
                            Log.d(TAG, "onCarImageClicked:${it.length()} ")

                            for (int in 0 until it.length()) {
                                val tripsObj = JSONObject(it.get(int).toString())
                                val tripsObjTrips = tripsObj.getJSONObject("pos")
                                val y = tripsObjTrips.getString("y")
                                val x = tripsObjTrips.getString("x")
                                pathHLatlng.add(LatLng(y.toDouble(), x.toDouble()))
                                Log.d(TAG, "onCarImageClickeds:${pathHLatlng.size} ")

                                if (int == it.length() - 1) {
                                    Log.d(TAG, "onCarImageClickedssss:${pathHLatlng.size} ")
                                    showPath(position, true, isEmpity)
                                }
                            }
                        }
                    }

                } else showPath(position, true, isEmpity)



                isPinviewVisible = false
            }

            override fun onParkImageClicked(position: Int) {
                showPath(position, false, false)
                isPinviewVisible = false
            }
        }
        carStatusAdapter = HistoryCarStatusAdapter(requireContext(), handler)
        binding.rvCarHistory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCarHistory.adapter = carStatusAdapter
    }

    private fun getHistoryDetails(from: String, to: String) {

        if (serverData.contains("s3")) {
//            viewmodel.callApiForHistoryDetailsGps3(carId, from, to)
            viewmodel.callApiForRouteGps3(carId, from, to, 5f)

        } else viewmodel.callApiForHistoryDetails(carId, fromInterval, toInterval)
    }

    private fun addObserver() {
        // call api for set car on map and check which server login

        if (serverData.contains("s3")) {

            // location call for one only
            (requireActivity() as MainActivity).homeMapViewModel.carAddressDataGPS3.observe(

                viewLifecycleOwner
            ) {
                binding.txtAddress.text = it
            }

            (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(
                viewLifecycleOwner
            ) {
                newUpdateViewGps3(it)
            }
//            if (isAddreadyAdded)
            viewmodel.getRoutesGps3.observe(viewLifecycleOwner) {
                if (it.drives?.size == 0 || it.drives == null) {
                    viewmodel.loading(false)
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rvCarHistory.visibility = View.GONE
                    carStatusAdapterGps3.clearAll()
                    binding.constTimeKm.visibility = View.GONE
                } else {
                    binding.txtNoData.visibility = View.GONE
                    binding.constTimeKm.visibility = View.VISIBLE
                    binding.rvCarHistory.visibility = View.VISIBLE
                    createRecyclerGps3(it)
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            isAddreadyAdded = false
        } else {
            (requireActivity() as MainActivity).homeMapViewModel.carAddressData.observe(

                viewLifecycleOwner
            ) {
                binding.txtAddress.text = it
            }

            (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(
                viewLifecycleOwner
            ) {
                it.let {
                    val uri =
                        (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                            it,
                            requireContext()
                        )
                    newUpdateView(uri)
                }
            }
            viewmodel._tripData.observe(viewLifecycleOwner) {
                tripData = it
                Utils.hideProgressBar()
                if (!tripData.isNullOrEmpty()) {
                    if (tripData.equals("0")) {
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.rvCarHistory.visibility = View.GONE
                        viewmodel.loading(false)

                        carStatusAdapter.clearAll()
                        binding.constTimeKm.visibility = View.GONE
                        //Toast.makeText(context, "Trip data in empty", Toast.LENGTH_SHORT).show()
                    } else {
                        Handler(Looper.myLooper()!!).postDelayed({
                            viewmodel.callApiForTripDetails(fromInterval, toInterval)
                        }, 1000)


                        binding.txtNoData.visibility = View.GONE
                        binding.constTimeKm.visibility = View.VISIBLE
                        binding.rvCarHistory.visibility = View.VISIBLE
                    }
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            viewmodel.getTripData.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    it.let {
                        carStatusAdapter.clearAll()
                        setHistoryList(it as MutableList<TripDetails.FirstObj>)
                    }
                } else {
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.rvCarHistory.visibility = View.GONE
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        viewmodel.status.observe(viewLifecycleOwner) {
            when (it!!) {
                ApiStatus.LOADING -> progressDialog.show()
                ApiStatus.DONE -> progressDialog.dismiss()
                ApiStatus.ERROR -> {
                    progressDialog.dismiss()
                    showError()
                }
                ApiStatus.NOINTERNET -> {
                    progressDialog.dismiss()
                    CommonAlertDialog.showConnectionAlert(requireContext())
                }
                ApiStatus.SUCCESSFUL -> {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun newUpdateView(
        carImage: ArrayList<Item>
    ) {
        if (map != null && carImage.isNotEmpty()) {
            DebugLog.e("newUpdateView=carId=$carId")
            for (i in 0 until (requireActivity() as MainActivity).homeMapViewModel.carDataList.size) {
                if ((requireActivity() as MainActivity).homeMapViewModel.carDataList[i].id.toString()
                        .equals(carId, true)
                ) {
                    newAddClusterInManager((requireActivity() as MainActivity).homeMapViewModel.carDataList[i])
                    setCarDetailData((requireActivity() as MainActivity).homeMapViewModel.carDataList[i])
                    break
                }
            }
        }
    }

    private fun newUpdateViewGps3(
        carImage: ArrayList<ItemGps3>
    ) {
        if (map != null && carImage.isNotEmpty()) {
            for (i in 0 until carImage.size) {
                if (carImage[i].imei
                        .equals(carId, true)
                ) {
                    // add marker and refresh data
                    if (MyPreference.getValueBoolean(
                            PrefKey.isSelected,
                            true
                        )
                    ) newAddClusterInManagerGps3(carImage[i])
                    setCarDetailDataGps3(carImage[i])
                    break
                }
            }
        } else viewmodel.loading(true)
    }

    private fun newAddClusterInManager(
        markerOptions: Item
    ) {
        DebugLog.e("Carname==" + markerOptions.nm)
        if (isPinviewVisible) {
            val updateLat: Double
            val updateLng: Double
            if (isFirstTime) {

                updateLat = markerOptions.tripToLat?.toDouble() ?: 0.0
                updateLng = markerOptions.tripToLong?.toDouble() ?: 0.0

                markerOptions.tripToLat?.let {
                    markerOptions.tripToLong?.let { it1 ->
                        ClusterRender(
                            it.toDouble(), it1.toDouble(), markerOptions
                        )
                    }
                }?.let {
                    createCustomMarker(
                        it
                    )
                }

                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(updateLat, updateLng),
                        14.5f
                    )
                )
                pinLocation = LatLng(updateLat, updateLng)
                isFirstTime = false
            } else if (marker != null) {

                updateLat = markerOptions.tripToLat?.toDouble() ?: 0.0
                updateLng = markerOptions.tripToLong?.toDouble() ?: 0.0
                if (marker?.position != markerOptions.tripToLat?.let {

                        markerOptions.tripToLong?.let { it1 ->
                            LatLng(
                                it.toDouble(),
                                it1.toDouble()
                            )
                        }
                    }) {
                    HRMarkerAnimation(
                        map!!, 1500
                    ) {
                        markerOptions.tripToLat?.let { it1 ->
                            markerOptions.tripToLong?.let { it2 ->
                                ClusterRender(
                                    it1.toDouble(), it2.toDouble(), markerOptions
                                )
                            }
                        }?.let { it2 ->
                            createCustomMarker(
                                it2
                            )
                        }
                        setMapPaddingBotttom(binding.constCardetails.height.toFloat())
                    }.animateMarker(
                        LatLng(
                            markerOptions.tripToLat!!.toDouble(),
                            markerOptions.tripToLong!!.toDouble()
                        ), marker?.position, marker

                    )
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                markerOptions.tripToLat!!.toDouble(),
                                markerOptions.tripToLong!!.toDouble()
                            ),
                            14.5f
                        )
                    )

                }
                pinLocation = LatLng(updateLat, updateLng)
            }
        }
    }


    private fun newAddClusterInManagerGps3(
        markerOptions: ItemGps3
    ) {
        try {
            if (isPinviewVisible) {
                val updateLat: Double
                val updateLng: Double
                if (isFirstTime) {

                    updateLat = markerOptions.lat.toDouble()
                    updateLng = markerOptions.lng.toDouble()

                    ClusterRenderGPS3(
                        markerOptions.lat.toDouble(),
                        markerOptions.lng.toDouble(),
                        markerOptions
                    )
                    createCustomMarkerGps3(
                        ClusterRenderGPS3(
                            markerOptions.lat.toDouble(),
                            markerOptions.lng.toDouble(),
                            markerOptions
                        )
                    )

                    pinLocation = LatLng(updateLat, updateLng)

                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(updateLat, updateLng),
                            14.5f
                        )
                    )
                    isFirstTime = false
                } else if (marker != null) {


                    updateLat = markerOptions.lat.toDouble()
                    updateLng = markerOptions.lng.toDouble()
                    val latLng =
                        LatLng(markerOptions.lat.toDouble(), markerOptions.lng.toDouble())

                    if (marker?.position != latLng) {

                        HRMarkerAnimation(
                            map!!, 1500
                        ) {
                            ClusterRenderGPS3(
                                markerOptions.lat.toDouble(),
                                markerOptions.lng.toDouble(),
                                markerOptions
                            )
                            createCustomMarkerGps3(
                                ClusterRenderGPS3(
                                    markerOptions.lat.toDouble(),
                                    markerOptions.lng.toDouble(),
                                    markerOptions
                                )
                            )
                            setMapPaddingBotttom(binding.constCardetails.height.toFloat())
                        }.animateMarker(
                            LatLng(
                                markerOptions.lat.toDouble(),
                                markerOptions.lng.toDouble()

                            ), marker?.position, marker


                        )
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    markerOptions.lat.toDouble(),
                                    markerOptions.lng.toDouble()

                                ),
                                14.5f
                            )
                        )
                    }
                    pinLocation = LatLng(updateLat, updateLng)
                    // moveToCurrentLocation(pinLocation)

                }
            }
        } catch (e: Exception) {
            DebugLog.e(e.message!!)
        }
    }

    @SuppressLint("ResourceType", "InflateParams")
    private fun createCustomMarker(clusterItem: ClusterRender) {
        try {
            val inflater: LayoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
            val textView: TextView = markerView?.findViewById<View>(R.id.text_label) as TextView
            val imageView: AppCompatImageView =
                markerView.findViewById<View>(R.id.image) as AppCompatImageView
            val imageArrow: AppCompatImageView =
                markerView.findViewById<View>(R.id.image_arrow) as AppCompatImageView
            val rotatedView: ConstraintLayout =
                markerView.findViewById<View>(R.id.rotatedView) as ConstraintLayout

            if (((Calendar.getInstance().timeInMillis / 1000) -
                        (clusterItem.mCarModel.trip_m?.toLong() ?: 0)) / 60 >= 60
            ) {
                textView.setTextColor(getColor(requireContext(), R.color.color_red))

            } else {
                textView.setTextColor(getColor(requireContext(), R.color.color_dark_blue))
            }
            textView.text = clusterItem.mCarModel.nm
            if (MyPreference.getValueBoolean(PrefKey.IS_ADD_BG, false)) {
                if (((Calendar.getInstance().timeInMillis / 1000) - (clusterItem.mCarModel.trip_m?.toLong()
                        ?: 0)) / 60 >= 60
                ) {
                    textView.setBackgroundResource(R.drawable.card_bg_rounded_red)
                } else {
                    textView.setBackgroundResource(R.drawable.card_bg_rounded)
                }
                textView.setPadding(10, 8, 10, 8)

            } else {
                textView.setBackgroundColor(
                    getColor(requireContext(), android.R.color.transparent)
                )
            }
            imageView.setImageResource(R.drawable.default_car)
            rotatedView.rotation = clusterItem.mCarModel.trip_course?.toFloat() ?: 0f
            val speed: Int = clusterItem.mCarModel.trip_curr_speed?.toInt() ?: 0
            if (speed > 0) {
                imageArrow.visibility = View.VISIBLE
            } else {
                imageArrow.visibility = View.GONE
            }
            // marker.setIcon(null)
            Glide.with(requireContext()).asBitmap()
                .placeholder(R.drawable.default_car)
                .error(R.drawable.default_car)
                .load("http://www.avltracmap.com" + clusterItem.snippet).fitCenter()

                .into(object : CustomTarget<Bitmap?>(30, 40) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {

                        DebugLog.e("createCustomMarker" + clusterItem.mCarModel.nm)
                        if (marker != null) {
                            Log.d(TAG, "onResourceReady: d1")
                            try {
                                marker?.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageView,
                                            resource,
                                            markerView
                                        )!!
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "catch onResourceReady: e1 ${e.message}")
                            }
                        } else {
                            Log.d(TAG, "onResourceReady: d2")
                            try {
                                marker = map?.addMarker(
                                    MarkerOptions()
                                        .position(clusterItem.position)
                                        .flat(true)
                                        .icon(
                                            BitmapDescriptorFactory.fromBitmap(
                                                getMarkerBitmapFromView(
                                                    imageView,
                                                    resource,
                                                    markerView
                                                )!!
                                            )
                                        )
                                        .title("")
                                        .anchor(0.5f, 0.5f).snippet("")
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "catch onResourceReady: e1 ${e.message}")
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

        } catch (e: Exception) {
            Log.e(TAG, "catch onResourceReady: e2 ${e.message}")
        }
    }

    @SuppressLint(
        "ResourceType", "SetTextI18n", "SimpleDateFormat", "InflateParams",
        "NotifyDataSetChanged"
    )
    private fun createCustomMarkerGps3(
        clusterItemGps3: ClusterRenderGPS3
    ) {
        val inflater: LayoutInflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
        val imageView: AppCompatImageView =
            markerView.findViewById<View>(R.id.image) as AppCompatImageView
        val imageArrow: AppCompatImageView =
            markerView.findViewById<View>(R.id.image_arrow) as AppCompatImageView
        val rotatedView: ConstraintLayout =
            markerView.findViewById<View>(R.id.rotatedView) as ConstraintLayout

        imageView.setImageResource(R.drawable.default_car)
        rotatedView.rotation = clusterItemGps3.mCarModel.angle.toFloat()
        val speed: Int = clusterItemGps3.mCarModel.speed.toInt()
        if (speed > 0) {
            imageArrow.visibility = View.VISIBLE
        } else {
            imageArrow.visibility = View.GONE
        }
        // marker.setIcon(null)
        Glide.with(requireContext()).asBitmap()
            .placeholder(R.drawable.default_car)
            .error(R.drawable.default_car)
            .load(R.drawable.default_car).fitCenter()
            .into(object : CustomTarget<Bitmap?>(30, 40) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    if (marker != null) {

                        Log.d(TAG, "onResourceReady: d1")
                        try {
                            marker?.setIcon(
                                BitmapDescriptorFactory.fromBitmap(
                                    getMarkerBitmapFromView(
                                        imageView, resource, markerView!!
                                    )!!
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "catch onResourceReady: e1 ${e.message}")
                        }

                    } else {
                        Log.d(TAG, "onResourceReady: d2")
                        try {
                            marker = map?.addMarker(
                                MarkerOptions()
                                    .position(clusterItemGps3.position)
                                    .flat(true)
                                    .icon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            getMarkerBitmapFromView(
                                                imageView, resource, markerView!!
                                            )!!
                                        )
                                    )
                                    .title("")
                                    .anchor(0.5f, 0.5f).snippet("")
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "catch onResourceReady: e2 ${e.message}")
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    var routeRootList: ArrayList<TripDetailsGps3> = ArrayList()
    lateinit var stop: StopGps3
    lateinit var drive: DriveGps3

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun createRecyclerGps3(routeRoot: RouteRootGps3) {
        viewmodel.loading(true)
        Log.d(
            TAG,
            "createRecyclerGps3: size ${routeRoot.drives!!.size} - ${routeRoot.stops!!.size}"
        )
        if (routeRoot.stops!!.size == routeRoot.drives!!.size) {
            if (getTimeStamp(routeRoot.stops!!.first().dt_start) <
                getTimeStamp(routeRoot.drives!!.first().dt_start)
            )
                fillStopFirst(routeRoot, false)
            else
                fillDriveFirst(routeRoot, false)
        } else if (routeRoot.stops!!.size > routeRoot.drives!!.size) {
            fillStopFirst(routeRoot, true)
        } else {
            fillDriveFirst(routeRoot, true)
        }
        Log.d(TAG, "createRecyclerGps3: rrl ${routeRootList.size}")
        binding.txtHours.text =
            Utils.formatDurationFullValues(routeRoot.drives_duration_time.toLong())
        binding.txtParkTime.text =
            Utils.formatDurationFullValues(routeRoot.stops_duration_time.toLong())
        binding.txtKilometer.text = "${df.format(routeRoot.route_length)} km"
        viewmodel.loading(false)
        carStatusAdapterGps3.addAll(routeRootList, routeRoot)
    }

    @SuppressLint("SimpleDateFormat")
    private fun fillStopFirst(routeRoot: RouteRootGps3, addAfter: Boolean) {
        routeRootList = ArrayList()

        for (ind in routeRoot.drives!!.indices) {
            stop = routeRoot.stops!![ind]
            drive = routeRoot.drives!![ind]
            routeRootList.add(
                TripDetailsGps3(
                    "stop",
                    stop.lat, stop.lng,
                    stop.speed, stop.dt_start,
                    stop.dt_end, stop.duration_time,
                    0.0, 0
                )
            )
            routeRootList.add(
                TripDetailsGps3(
                    "drive", "0.0", "0.0",
                    drive.top_speed, drive.dt_start,
                    drive.dt_end, drive.duration_time,
                    drive.route_length, drive.top_speed
                )
            )
        }
        drive = routeRoot.drives!![routeRoot.drives!!.lastIndex]
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)

        val lastDate: String = if (mDate != selString)
            eDate
        else
            currentDate

        val diff = getTimeStamp(lastDate) / 1000 - (getTimeStamp(drive.dt_end) +
                tz.getOffset(
                    getTimeStamp(drive.dt_end)
                )) / 1000
        Log.d(TAG, "fillDriveFirst: $mDate - $selString - ${drive.dt_end} $lastDate $endDate $diff")


        if (addAfter) {
            stop = routeRoot.stops!![routeRoot.stops!!.lastIndex]
            routeRootList.add(
                TripDetailsGps3(
                    "stop", stop.lat, stop.lng,
                    stop.speed, stop.dt_start, stop.dt_end,
                    stop.duration_time,
                    0.0, 0
                )
            )
        } else if (diff > 300) {
            val route = routeRoot.route!![routeRoot.route!!.lastIndex]
            routeRootList.add(
                TripDetailsGps3(
                    "stop",
                    route.lat, route.lng,
                    route.speed,
                    drive.dt_end, endDate,
                    diff.toInt(),
                    0.0, 0
                )
            )
        }
    }

    private fun fillDriveFirst(routeRoot: RouteRootGps3, addAfter: Boolean) {
        routeRootList = ArrayList()
        drive = routeRoot.drives!!.first()
        val diff = (getTimeStamp(drive.dt_start) + tz.getOffset(
            getTimeStamp(drive.dt_start)
        )) / 1000 - getTimeStamp(sDate) / 1000

        Log.d(TAG, "fillDriveFirst: curDate $sDate $diff")
        if (diff > 300
        ) {
            val route = routeRoot.route!![0]
            routeRootList.add(
                TripDetailsGps3(
                    "stop",
                    route.lat,
                    route.lng,
                    route.speed,
                    startDate,
                    drive.dt_start,
                    diff.toInt(),
                    0.0,
                    0
                )
            )
        }
        for (ind in routeRoot.stops!!.indices) {
            stop = routeRoot.stops!![ind]
            drive = routeRoot.drives!![ind]

            routeRootList.add(
                TripDetailsGps3(
                    "drive",
                    "0.0", "0.0",
                    drive.top_speed,
                    drive.dt_start, drive.dt_end,
                    drive.duration_time,
                    drive.route_length,
                    drive.top_speed
                )
            )
            routeRootList.add(
                TripDetailsGps3(
                    "stop",
                    stop.lat, stop.lng,
                    stop.speed,
                    stop.dt_start, stop.dt_end,
                    stop.duration_time,
                    0.0, 0
                )
            )
        }
        drive = routeRoot.drives!![routeRoot.drives!!.lastIndex]
        if (addAfter) routeRootList.add(
            TripDetailsGps3(
                "drive",
                "0.0", "0.0",
                drive.top_speed,
                drive.dt_start, drive.dt_end,
                drive.duration_time,
                drive.route_length, drive.top_speed
            )
        )
    }

    /**
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private fun getMarkerBitmapFromView(
        imageView: AppCompatImageView,
        bitmap: Bitmap,
        markerView: View
    ): Bitmap? {
        imageView.setImageBitmap(bitmap)
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        markerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            markerView.measuredWidth, markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = markerView.background
        drawable?.draw(canvas)
        markerView.draw(canvas)
        return returnedBitmap
    }

    private fun setHistoryList(tripDetailList: MutableList<TripDetails.FirstObj>) {

        historyList = mutableListOf()
        historyList.clear()

        var tempArray = tripDetailList
        Log.d(TAG, "setHistoryList: size ${tripDetailList.size}")
        val firstData = tempArray.first()



        if (tempArray[0].fromList.from_t - fromInterval > 300) {
            val historyObj = TripDetails().FirstObj()

            val from = TripDetails().From()

            from.from_t = fromInterval.toDouble()
            from.from_x = firstData.toList.to_x
            from.from_y = firstData.toList.to_y

            val to = TripDetails().To()
            to.to_t = firstData.fromList.from_t.toDouble()
            to.to_x = firstData.fromList.from_x
            to.to_y = firstData.fromList.from_y

            historyObj.fromList = from
            historyObj.toList = to

            historyObj.tripEventType = "p"

            historyList.add(historyObj)
        } else
            tempArray[0].fromList.from_t = fromInterval.toDouble()
        for (index in tempArray.indices) {
            Log.d(TAG, "setHistoryList: ind= $index - ${tempArray[index].tripEventType}")
            tempArray[index].tripEventType = "trip"

            historyList.add(tempArray[index])

            val nextIndex = index + 1
            if (nextIndex <= (tempArray.size - 1)) {
                val nextObject = tempArray[nextIndex]

                val trip = TripDetails().FirstObj()

                val from = TripDetails().From()
                from.from_t = tempArray[index].toList?.to_t
                from.from_x = tempArray[index].toList?.to_x
                from.from_y = tempArray[index].toList?.to_y

                val to = TripDetails().To()
                to.to_t = nextObject.fromList?.from_t
                to.to_x = nextObject.fromList?.from_x
                to.to_y = nextObject.fromList?.from_y

                trip.fromList = from
                trip.toList = to

                trip.tripEventType = "p"

                historyList.add(trip)
            } else {
                if (toInterval - tempArray[index].toList.to_t > 300) {
                    val trip = TripDetails().FirstObj()

                    val from = TripDetails().From()
                    val to = TripDetails().To()

                    from.from_t = tempArray[index].toList?.to_t
                    from.from_x = tempArray[index].toList?.to_x
                    from.from_y = tempArray[index].toList?.to_y

                    /*   to.to_x = tempArray[index].toList?.to_x
                       to.to_y = tempArray[index].toList?.to_y
                       to.to_t = toInterval.toDouble()*/

                    val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dateString =
                        formatDate.format(Date(tempArray[index].toList?.to_t!!.toLong() * 1000L))

                    if (dateString.equals(selectedDateString)) {
                        val currentDateString = formatDate.format(Date())

                        to.to_x = tempArray[index].toList?.to_x
                        to.to_y = tempArray[index].toList?.to_y

                        if (selectedDateString.equals(currentDateString, true)) {
                            to.to_t = (Calendar.getInstance().timeInMillis / 1000).toDouble()

                        } else {
                            val currentInterval = Utils.getDateToMilliSeconds(
                                selectedDateString,
                                "23:59:59"
                            )

                            to.to_t = currentInterval.toDouble()

                            to.to_t = toInterval.toDouble()
                        }


                    }
                    trip.fromList = from
                    trip.toList = to

                    trip.tripEventType = "p"
                    historyList.add(trip)

                } else {

                    Log.d(TAG, "setHistoryList:toList ${tempArray[index].fromList.from_t}")
                    val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                    val currentDateString = formatDate.format(Date())

                    if (selectedDateString.equals(currentDateString, true)) {
                        tempArray[index].toList.to_t =
                            (Calendar.getInstance().timeInMillis / 1000).toDouble()

                    } else {

                        tempArray[index].toList.to_t = toInterval.toDouble()

                    }

                    tempArray[index].toList.to_t = toInterval.toDouble()
                    historyList[index] = tempArray[index]

                }
            }
        }

        tempArray = historyList
        Log.e("TempArray size", ":" + tempArray.size)

        setUpCarAdapterClass(tempArray)
    }

    private fun setUpCarAdapterClass(tempArray: MutableList<TripDetails.FirstObj>) {
        carStatusAdapter.addAll(tempArray)
        initSetData()
    }

    @SuppressLint("SetTextI18n")
    private fun initSetData() {
        // round off value
        /*var tt = Math.round(rt.toDouble())*/

        val totalDist = carStatusAdapter.calculateTripDistance()
        val roundOff = Math.ceil(df.format(totalDist).toDouble())
        binding.txtKilometer.text = "$roundOff km"

        val totalMins = carStatusAdapter.calculateTripTime()
        binding.txtHours.text = Utils.formatDurationFullValues(totalMins)

        val totalParkMin = carStatusAdapter.calculateParkingHours()

        binding.txtParkTime.text = Utils.formatDurationFullValues(totalParkMin)
    }

    private fun showError() {
        if (viewmodel.errorMsg.isNotEmpty()) {
            Utils.showToast(requireContext(), viewmodel.errorMsg, Toast.LENGTH_SHORT)
            viewmodel.errorMsg = ""
        }
    }

    private fun loadMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onMapReady(mMap: GoogleMap) {
        if (isGooglePlayServicesAvailable()) {
            map = mMap
            if (ContextCompat.checkSelfPermission(

                    context!!, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity!!, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                }
            } else {
                (requireActivity() as MainActivity).mapSettings(map!!)
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
                    /*Show the dialog by calling startResolutionForResult(), and check the result
                    in onActivityResult().*/

                    //status.startResolutionForResult(activity!!, REQUEST_CHECK_SETTINGS)
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


    override fun onResume() {
        super.onResume()
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

    /**
     * This function is used to draw the path between the Origin and Destination.
     */
    private fun showPath(pos: Int, carClick: Boolean, isEmpity: Boolean) {
        Log.d(TAG, "showPath: fsfdfsdfsds")

        if (map != null) {
            map?.clear()
        }
        val dataList = carStatusAdapter.getWholeList()
        val builder = LatLngBounds.Builder()

        if (carClick) {

            for (trip in dataList?.indices!!) {
                if (pos == trip) {
                    val lt = dataList[trip].fromList.from_y
                    val ln = dataList[trip].fromList.from_x
                    builder.include(LatLng(lt, ln))
                    builder.include(LatLng(dataList[trip].toList.to_y, dataList[trip].toList.to_x))



                    if (dataList[trip].history_track != null && dataList[trip].history_track.toString()
                            .isNotEmpty()
                    ) {
                        val data = PolyUtil.decode(dataList[trip].history_track)
                        for (i in 0 until data.size) {
                            builder.include(data[i])
                        }
                        drawPolyline(dataList[trip].history_track)
                    } else {
                        drawLineGps3(pathHLatlng)
                    }

                    map?.addMarker(
                        MarkerOptions().position(LatLng(lt, ln)).flat(true).anchor(0.5f, 1f).icon(
                            Utils.bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_location_blue
                            )
                        )
                    )

                    var latLng: LatLng
                    if (isEmpity) {
                        latLng = LatLng(
                            pathHLatlng[pathHLatlng.lastIndex].latitude,
                            pathHLatlng[pathHLatlng.lastIndex].longitude
                        )
                    } else {
                        latLng = LatLng(
                            dataList[trip].toList.to_y,
                            dataList[trip].toList.to_x
                        )
                    }
                    map!!.addMarker(
                        MarkerOptions().position(
                            latLng

                        ).flat(true).anchor(0f, 1f).icon(
                            Utils.bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_flag
                            )
                        )
                    )

                    bounds = builder.build()
                    pinLocation = LatLng(bounds!!.center.latitude, bounds!!.center.longitude)
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds!!, 100),
                        1000,
                        null
                    )
                    break
                }
            }

        } else {
            for (trip in dataList?.indices!!) {
                if (pos == trip) {
                    val lt = dataList[trip].toList.to_y
                    val ln = dataList[trip].toList.to_x
                    builder.include(LatLng(dataList[trip].toList.to_y, dataList[trip].toList.to_x))
                    pinLocation = LatLng(dataList[trip].toList.to_y, dataList[trip].toList.to_x)

                    map?.addMarker(
                        MarkerOptions().position(LatLng(lt, ln)).flat(true).icon(
                            Utils.bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_parking
                            )
                        )
                    )
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lt, ln), 14f))
                    break
                }
            }
        }
    }

    /**
     * This function is used to draw the path between the Origin and Destination.
     */
    private fun showPathGps3(

        routeRoot: RouteRootGps3, tripList: TripDetailsGps3, carClick: Boolean
    ) {
        if (map != null) {
            map?.clear()
        }
        val builder = LatLngBounds.Builder()
        val tempList = ArrayList<LatLng>()
        if (!routeRoot.route.isNullOrEmpty())
            if (carClick) {
                val firstIndex =
                    routeRoot.route!!.indexOfFirst { routeGps3 -> routeGps3.dt_tracker == tripList.dt_start }
                val lastIndex =
                    routeRoot.route!!.indexOfLast { routeGps3 -> routeGps3.dt_tracker == tripList.dt_end }
                for (ind in firstIndex..lastIndex) {
                    tempList.add(
                        LatLng(
                            routeRoot.route!![ind].lat.toDouble(),
                            routeRoot.route!![ind].lng.toDouble()
                        )
                    )
                }
                val lt = tempList[0].latitude
                val ln = tempList[0].longitude

                drawLineGps3(tempList)
                map?.addMarker(
                    MarkerOptions().position(LatLng(lt, ln)).flat(true).anchor(0.5f, 1f).icon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(), R.drawable.ic_location_blue
                        )
                    )
                )
                map?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            tempList[tempList.lastIndex].latitude,
                            tempList[tempList.lastIndex].longitude
                        )
                    ).flat(true).anchor(0f, 1f).icon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(),
                            R.drawable.ic_flag
                        )
                    )
                )
                for (latLng in tempList) {
                    builder.include(latLng)
                }
                // this is used to set the bound of the Map
                val bounds = builder.build()
                pinLocation = LatLng(bounds.center.latitude, bounds.center.longitude)
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 70))
            } else {
                val lt = tripList.lat.toDouble()
                val ln = tripList.lng.toDouble()
                Log.d(TAG, "showPathGps3: $lt lng $ln")

                builder.include(
                    LatLng(
                        tripList.lat.toDouble(), tripList.lng.toDouble()
                    )
                )
                pinLocation = LatLng(
                    tripList.lat.toDouble(), tripList.lng.toDouble()
                )

                map?.addMarker(
                    MarkerOptions().position(LatLng(lt, ln)).flat(true).icon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(), R.drawable.ic_parking
                        )
                    )
                )
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lt, ln), 14f))
            }
    }

    private var firstPolyline: Polyline? = null
    private var secondPolyline: Polyline? = null
    private fun drawLineGps3(latLngList: ArrayList<LatLng>) {

        if (firstPolyline != null || secondPolyline != null) {
            firstPolyline?.remove()
            secondPolyline?.remove()
        }
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        // this is used to set the bound of the Map
        val bounds = builder.build()
        map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 70))

        val polylineOptions = PolylineOptions()
        polylineOptions.color(getColor(requireContext(), R.color.color_blue))
        polylineOptions.width(8f)
        polylineOptions.addAll(latLngList)
        firstPolyline = map?.addPolyline(polylineOptions)

    }

    private fun drawPolyline(track: String) {
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(track))
            .width(8f)
            .color(getColor(requireContext(), R.color.color_blue))
        map?.addPolyline(polyline)
    }
}