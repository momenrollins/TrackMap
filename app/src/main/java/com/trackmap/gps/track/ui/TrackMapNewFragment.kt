package com.trackmap.gps.track.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.DataValues.tz
import com.trackmap.gps.MainActivity
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.LayoutTrackMapBinding
import com.trackmap.gps.history.model.RouteGps3
import com.trackmap.gps.history.model.TripDetails
import com.trackmap.gps.history.viewmodel.HistoryListViewModel
import com.trackmap.gps.homemap.model.CheckedUnitModel
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.homemap.ui.CarOnMapRenderer
import com.trackmap.gps.homemap.ui.ClusterRender
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.track.model.MessageRoot
import com.trackmap.gps.track.model.TrackPathModel
import com.trackmap.gps.track.movingcar.CarMoveAnim
import com.trackmap.gps.track.viewmodel.TrackViewModel
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.Utils
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import org.json.JSONObject
import java.io.Serializable
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.trackmap.gps.R

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class TrackMapNewFragment : BaseFragment(), OnMapReadyCallback, Serializable,
    ResultCallback<LocationSettingsResult>,
    GoogleApiClient.ConnectionCallbacks {
    private lateinit var builder: LatLngBounds.Builder
    private lateinit var carDetails: Item
    private var inputFormat = "yyyy-MM-dd hh:mm a"
    private lateinit var binding: LayoutTrackMapBinding
    private lateinit var viewmodel: TrackViewModel
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    var duration: Int = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    var lastTimeParking = ArrayList<String>()
    var carSpeedList = ArrayList<Int>()
    var carTimeList = ArrayList<Long>()
    var trackDetailList = ArrayList<MessageRoot>()
    var oldCarLocationList = ArrayList<LatLng>()
    var carLocationList = ArrayList<LatLng>()
    var carList = ArrayList<MessageRoot>()
    var carpoints = ArrayList<LatLng>()
    var animator: ObjectAnimator? = null
    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private var oldLocation: LatLng? = null
    private var newLocation: LatLng? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private var livehandlerlocation: Handler? = null
    private var liverunnablelocation: Runnable? = null
    private var chooseSpeedList = arrayOf("x", "10x", "25x", "50x", "75x", "100x")
    var isBacked: Boolean = true
    var isDone: Boolean = false
    var ismove: Boolean = false
    var isStateChanged: Boolean = false
    var isPlaying = false
    var isFAB = false
    private lateinit var historyListViewModel: HistoryListViewModel

    private var mRequestingLocationUpdates: Boolean = false
    var trackPath = TrackPathModel()
    var cardetails = Item()
    var is3D: Boolean = false
    var isCentered: Boolean = false
    var isMove: Boolean = false
    var imageArrow: AppCompatImageView? = null
    var textView: TextView? = null
    var imageView: AppCompatImageView? = null
    var markerView: View? = null
    private val TAG = "TrackMapNewFragment"

    private var isFirstTime = true

    private val itemId = MyPreference.getValueString("itemId", "")
    private val timeFrom = Utils.getDateFromMillis(
        (MyPreference.getValueString("timeFrom", "")!!.toLong() * 1000),
        "yyyy-MM-dd HH:mm:ss"
    ).toString()
    private val timeTo = Utils.getDateFromMillis(
        (MyPreference.getValueString("timeTo", "")!!.toLong() * 1000),
        "yyyy-MM-dd HH:mm:ss"
    ).toString()
    private val startDate = Utils.getDateFromMillis(
        ((getTimeStamp(timeFrom)) - tz.getOffset(getTimeStamp(timeFrom))), "yyyy-MM-dd HH:mm:ss"
    )!!
    private val endDate = Utils.getDateFromMillis(
        ((getTimeStamp(timeTo)) - tz.getOffset(getTimeStamp(timeTo))), "yyyy-MM-dd HH:mm:ss"
    )!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        builder = LatLngBounds.Builder()
        MyPreference.RemoveItem(PrefKey.Index)
        MyPreference.RemoveItem("lat")
        MyPreference.RemoveItem("lng")
        trackPath = arguments?.getSerializable("model") as TrackPathModel
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    lateinit var inflaterCar: LayoutInflater
    lateinit var markerViewCar: View
    lateinit var imageViewCar: ImageView
    lateinit var imageArrowCar: AppCompatImageView

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.layout_track_map, container, false
        )
        handler = Handler(Looper.getMainLooper())
        livehandlerlocation = Handler(Looper.myLooper()!!)
        historyListViewModel = ViewModelProvider(this)[HistoryListViewModel::class.java]
        MyPreference.RemoveItem(PrefKey.Index)
        MyPreference.RemoveItem("lat")
        MyPreference.RemoveItem("lng")
        MyPreference.setValueInt(PrefKey.HandlerIndex, 0)
        inflaterCar = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        markerViewCar = inflater.inflate(R.layout.layout_custom_mapmarker, null)
        imageViewCar = markerViewCar.findViewById<View>(R.id.image) as ImageView
        imageArrowCar = markerViewCar.findViewById<View>(R.id.image_arrow) as AppCompatImageView
        viewmodel = ViewModelProvider(this)[TrackViewModel::class.java]
        val arrayAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, chooseSpeedList
        )
        val materialDesignSpinner = binding.accountSpinner
        materialDesignSpinner.adapter = arrayAdapter
        materialDesignSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // this used to increase or decrease speed
                    var pos = 1
                    when (position) {
                        0 -> pos = 1
                        1 -> pos = 10
                        2 -> pos = 25
                        3 -> pos = 50
                        4 -> pos = 75
                        5 -> pos = 100
                    }
                    index = MyPreference.getValueInt(PrefKey.HandlerIndex, 0)
                    if (index != 0 && isPlaying)
                        MyPreference.setValueInt(PrefKey.HandlerIndex, (index - 1))
                    if (carLocationList.size > 0) {
                        CarMoveAnim.time = ((carTimeList[index] * 1000 / pos).toInt())
                        CarMoveAnim.stopFlag = 0
                        if (runnable != null)
                            handler!!.removeCallbacks(runnable!!)
                        showMovingCar(carLocationList)
                    }
                }
            }
        if (!serverData.contains("s3")) {
            carDetails = Item()
            (requireActivity() as MainActivity).homeMapViewModel.status.observe(viewLifecycleOwner) {
                if (it.equals(ApiStatus.LOADING))
                    Utils.showProgressBar(requireActivity())
                else
                    Utils.hideProgressBar()
            }
            (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
            val endDateStr = Utils.getDateFromMillis(trackPath.endTimeStamp * 1000, "dd-MM-yyyy")
/*            val currentDateStr = Utils.getCurrentDate("dd-MM-yyyy")
            if (currentDateStr == endDateStr) {
            }*/

            val endDateStrNew =
                Utils.getDateFromMillis(
                    trackPath.endTimeStamp * 1000, "dd-MM-yyyy HH:mm:ss"
                )
            val endDateNew =
                endDateStrNew?.let {
                    Utils.convertStringToDate(endDateStrNew, "dd-MM-yyyy HH:mm:ss")
                }
            val currentDateStrNew = Utils.getCurrentDate("dd-MM-yyyy")

            if (endDateNew?.after(Date()) == true || currentDateStrNew == endDateStr) {
                Log.d(TAG, "onCreateView:c1 👌")


                if (getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")) < getTimeStamp(
                        timeTo
                    )
                ) {
                    Log.d(TAG, "onCreateView:c2 👌")

                    (requireActivity() as MainActivity).homeMapViewModel.unitList.observe(
                        viewLifecycleOwner
                    ) {
                        Log.d(TAG, "onCreateView: testLoop 👌")
                        if (map != null) {
                            it.let {
                                if (it.isNotEmpty()) {
                                    for (i in it.indices) {
                                        if (it[i].carId.equals(
                                                trackPath.selectedCarData[0].carId.toString(), true
                                            )
                                        ) {
                                            val list = ArrayList<CheckedUnitModel>()
                                            list.clear()
                                            list.add(it[i])
                                            val uri =
                                                (requireActivity() as MainActivity).homeMapViewModel.getCarImageFromData(
                                                    list, requireContext()
                                                )
                                            DebugLog.e("TrackMap UnitList")
                                            carDetails = uri[0]
                                            setCarValues(
                                                carDetails.trip_curr_speed?.toInt() ?: 0 > 0
                                            )
                                            break
                                        }
                                    }
                                }
                            }

                            val timeToStamp = MyPreference.getValueString("timeTo", "")!!.toLong()
                            val timeFromStamp =
                                MyPreference.getValueString("timeFrom", "")!!.toLong()
                            val date = Utils.getDateFromMillis(
                                timeToStamp * 1000, "dd/MM/yyyy"
                            )
                            Log.d(
                                TAG,
                                "onMapReady: date ${Utils.getCurrentDate("dd/MM/yyyy")} $date"
                            )

                            isMove = !isMove

                            viewmodel.callLocation()
                            viewmodel.callApiForTrackHistoryDetails(
                                itemId!!, timeFromStamp, timeToStamp
                            )


                            isFirstTime = false
                        }
                    }
                }
            }
        }
        loadMap(savedInstanceState)
        initGoogleAPIClient()
        createLocationRequest()
        buildLocationSettingsRequest()
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    private fun setCarValues(isTripVisible: Boolean) {
        DebugLog.e("carDetails.trip_distance ${carDetails.trip_distance}")

        if (isTripVisible) carDetails.trip_distance?.let { it1 -> setDistance(it1.toDouble()) }
        if (carDetails.trip_curr_speed?.toInt() ?: 0 > 0) {
            setTime(
                (carDetails.tripToT?.toDouble() ?: 0.0) - (carDetails.tripFromT?.toDouble() ?: 0.0)
            )
            DebugLog.e("Car Running")
            carDetails.tripToLat?.toDouble()?.let { it1 ->
                carDetails.tripToLong?.toDouble()?.let { it2 ->
                    ClusterRender(
                        it1, it2, carDetails
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getIntentData() {
        setDistance(0.0)
        setTime(0.0)
        if (!trackPath.dataList.isNullOrEmpty()) getLatLngListForPath(0)
        if (!trackPath.stopDataList.isNullOrEmpty()) setStopLatLong(0)
    }

    @SuppressLint("SetTextI18n")
    private fun getIntentDataGps3() {
        binding.totalTimeValue.text = trackPath.routeRootGps3!!.drives_duration
        setDistance(trackPath.routeRootGps3!!.route_length * 1000)
        createStopsAndParkingPoints(0)
        getLatLngListForPathGps3()
    }

    @SuppressLint("SimpleDateFormat")
    private fun createStopsAndParkingPoints(index: Int) {
        for (item in index until trackPath.routeRootGps3!!.stops!!.size) {
            if (getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_end) - getTimeStamp(
                    trackPath.routeRootGps3!!.stops!![item].dt_start
                ) >= 300000
            ) {
                val timestamp: Long =
                    getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_start) +
                            tz.getOffset(getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_start))
                if (item == 0) {
                    val endDateStamp =
                        getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_end) +
                                tz.getOffset(getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_end))

                    addOriginDestinationMarkerAndGet(
                        LatLng(
                            trackPath.routeRootGps3!!.stops!![item].lat.toDouble(),
                            trackPath.routeRootGps3!!.stops!![item].lng.toDouble()
                        ), timeFrom,
                        Utils.formatDurationFullValues((endDateStamp - getTimeStamp(timeFrom)) / 1000),
                        R.drawable.ic_parking
                    )
                } else {
                    try {
                        addOriginDestinationMarkerAndGet(
                            LatLng(
                                trackPath.routeRootGps3!!.stops!![item].lat.toDouble(),
                                trackPath.routeRootGps3!!.stops!![item].lng.toDouble()
                            ),
                            SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH
                            ).format(Date(timestamp)),
                            trackPath.routeRootGps3!!.stops!![item].duration,
                            R.drawable.ic_parking
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "createStopsAndParkingPoints: ${e.message}")
                    }
                }
            } else {
                val timeStamp =
                    getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_start) + tz.getOffset(
                        getTimeStamp(trackPath.routeRootGps3!!.stops!![item].dt_start)
                    )
                map?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            trackPath.routeRootGps3!!.stops!![item].lat.toDouble(),
                            trackPath.routeRootGps3!!.stops!![item].lng.toDouble()
                        )
                    )
                        .title(
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(
                                Date(timeStamp)
                            )
                        )
                        .snippet(trackPath.routeRootGps3!!.stops!![item].duration).flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_stop))
                )
            }
        }
    }


    var oldSpeed: Int = 0

    @SuppressLint("ResourceType", "InflateParams")
    private fun createCustomMarker(
        latLng: LatLng, angle: Float, speed: Int?
    ) {
        val inflater: LayoutInflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
        val imageView: AppCompatImageView =
            markerView.findViewById<View>(R.id.image) as AppCompatImageView
        val imageArrow: AppCompatImageView =
            markerView.findViewById<View>(R.id.image_arrow) as AppCompatImageView
        imageView.setImageResource(R.drawable.default_car)
        Log.d(TAG, "createCustomMarker: SPEED $speed")
        if (speed!! > 0) {
            imageArrow.visibility = View.VISIBLE
        } else {
            imageArrow.visibility = View.GONE
        }
        if (!ismove) {
            marker?.remove()
            Glide.with(requireContext()).asBitmap()
                .placeholder(R.drawable.default_car)
                .error(R.drawable.default_car)
                .load("http://gps.tawasolmap.com" + carDetails.uri).fitCenter()
                .into(object : CustomTarget<Bitmap?>(30, 40) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        marker = map?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .flat(true)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageView, resource, markerView
                                        )!!
                                    )
                                )
                                .rotation(angle)
                                .anchor(0.5f, 0.5f)
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
        marker?.title = "$speed km/h"
        if (marker != null && marker!!.isInfoWindowShown)
            marker!!.showInfoWindow()
        oldSpeed = speed
    }


    @SuppressLint("ResourceType", "InflateParams")
    private fun createCustomMarkerGps3(rout: RouteGps3) {
        val inflater: LayoutInflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
//        val textView: TextView = markerView?.findViewById<View>(R.id.text_label) as TextView
        val imageView: AppCompatImageView =
            markerView.findViewById<View>(R.id.image) as AppCompatImageView
        val imageArrow: AppCompatImageView =
            markerView.findViewById<View>(R.id.image_arrow) as AppCompatImageView
        imageView.setImageResource(R.drawable.default_car)
        val speed: Int = rout.speed
        Log.d(TAG, "createCustomMarkerGps3: SPEED $isStateChanged ${rout.speed}")

        if (speed > 0) {
            imageArrow.visibility = View.VISIBLE
        } else {
            imageArrow.visibility = View.GONE
        }
        val snippet: String = Utils.formatDurationFullValues(
            (getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")) -
                    ((getTimeStamp(rout.dt_tracker)) +
                            tz.getOffset(getTimeStamp(rout.dt_tracker)))) / 1000
        )
        if (!ismove) {
            marker?.remove()
            Glide.with(requireContext()).asBitmap()
                .placeholder(R.drawable.default_car)
                .error(R.drawable.default_car)
                .load(R.drawable.default_car).fitCenter()
                .into(object : CustomTarget<Bitmap?>(30, 40) {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        val date = Utils.getDateFromMillis(
                            getTimeStamp(rout.dt_tracker) +
                                    tz.getOffset(getTimeStamp(rout.dt_tracker)),
                            "yyyy-MM-dd HH:mm:ss"
                        )
                        marker = map?.addMarker(
                            MarkerOptions()
                                .position(carLocationList[carLocationList.lastIndex])
                                .flat(true)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageView, resource, markerView
                                        )!!
                                    )
                                )
                                .rotation(rout.angle.toFloat())
                                .anchor(0.5f, 0.5f)
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
        Log.d(TAG, "createCustomMarkerGps3: SPEED $marker")
        marker?.title = "$speed km/h"
        if (marker != null && marker!!.isInfoWindowShown)
            marker!!.showInfoWindow()
        oldSpeed = speed
    }

    /**
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private fun getMarkerBitmapFromView(
        imageView: ImageView, bitmap: Bitmap, markerView: View
    ): Bitmap? {
        imageView.setImageBitmap(bitmap)
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        markerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            markerView.measuredWidth,
            markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = markerView.background
        drawable?.draw(canvas)
        markerView.draw(canvas)
        return returnedBitmap
    }

    private fun setTime(d: Double) {
        val duration: Double = trackPath.dataList.sumOf { firstObj: TripDetails.FirstObj ->
            firstObj.toList.to_t - firstObj.fromList.from_t
        }
        DebugLog.e("carDetails.trip_distance ${trackPath.dataList.size} duration: $duration / $d")
        binding.totalTimeValue.text = Utils.formatDurationFullValues((duration + d).toLong())
    }

    @SuppressLint("SetTextI18n")
    private fun setDistance(data: Double) {
        val sumArray: Double = trackPath.dataList.sumOf { firstObj: TripDetails.FirstObj ->
            firstObj.history_distance
        }
        // val df = DecimalFormat("#.###")
        val df = DecimalFormat("#.###", DecimalFormatSymbols(Locale.ENGLISH))
        df.roundingMode = RoundingMode.CEILING
        binding.kmValues.text = df.format((sumArray + data) / 1000) + " km"
    }

    private fun setStopLatLong(index: Int) {
        for (int in index until trackPath.stopDataList.size) {
            val duration =
                trackPath.stopDataList[int].toList.to_t - trackPath.stopDataList[int].fromList.from_t

            Log.d(
                TAG,
                "setStopLatLong: duration $duration ${trackPath.stopDataList[int].toList.to_t}  ${trackPath.stopDataList[int].fromList.from_t}"
            )
            if (trackPath.stopDataList[int].history_curr_speed <= trackPath.stopDataList[int].history_max_speed && (duration > 30 && duration < 300)) {
                map?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            trackPath.stopDataList[int].toList.to_y,
                            trackPath.stopDataList[int].toList.to_x
                        )
                    )
                        .title(
                            Utils.getDateFromMillis(
                                (trackPath.stopDataList[int].toList.to_t * 1000).toLong(),
                                inputFormat
                            ).toString(),
                        )
                        .snippet(Utils.formatDurationFullValues((duration).toLong()))
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_stop))
                )
                //                        MarkerOptions().position(LatLng(stopDataList[int].toList.to_y,  stopDataList[int].toList.to_x)).flat(true).icon(BitmapDescriptorFactory.fromBitmap(getCarBitmap(requireContext())))
            }
        }


    }

    var lastParkingDate: Double = 0.0

    @SuppressLint("PotentialBehaviorOverride")
    private fun getLatLngListForPath(index: Int) {
        Log.d(TAG, "getLatLngListForPath: $index ${trackPath.dataList.size}")
//        try {
        for (i in index until trackPath.dataList.size) {
            val lt = trackPath.dataList[i].fromList.from_y
            val ln = trackPath.dataList[i].fromList.from_x

//            drawPolyline(trackPath.dataList[i].history_track)
            val fromDate: String = when (i) {
                0 -> Utils.getDateFromMillis(trackPath.startTimeStamp * 1000, inputFormat)
                    .toString()
                // trackDetailList.size - 1 -> Utils.getDateFromMillis(trackPath.endTimeStamp * 1000, inputFormat).toString()
                else -> Utils.getDateFromMillis(
                    (trackPath.dataList[i - 1].toList.to_t * 1000).toLong(),
                    inputFormat
                ).toString()
            }
            var parkingTime: String
            var icon = R.drawable.ic_parking

            when (i) {
                0 -> {
                    Log.d(
                        TAG,
                        "getLatLngListForPath: duration ${trackPath.dataList[0].fromList.from_t - trackPath.startTimeStamp}"
                    )
                    if (trackPath.dataList[0].fromList.from_t - trackPath.startTimeStamp < 300)
                        icon = R.drawable.ic_stop
                    parkingTime =
                        Utils.formatDurationFullValues((trackPath.dataList[0].fromList.from_t - trackPath.startTimeStamp).toLong())
                }
                else -> {
                    parkingTime =
                        Utils.formatDurationFullValues((trackPath.dataList[i].fromList.from_t - trackPath.dataList[i - 1].toList.to_t).toLong())

                }
            }
            addOriginDestinationMarkerAndGet(
                LatLng(lt, ln),
                fromDate,
                parkingTime,
                icon
            )
            icon = R.drawable.ic_parking
            if (trackPath.dataList.size - 1 == i) {
                Log.d(
                    TAG,
                    "getLatLngListForPath: duration ${System.currentTimeMillis() / 1000} ${trackPath.dataList[i].toList.to_t}"
                )
                if (Utils.getCurrentDate("dd-MM-yyyy") == Utils.getDateFromMillis(
                        trackPath.endTimeStamp * 1000, "dd-MM-yyyy"
                    )
                ) {
                    var snpt: String
                    if (System.currentTimeMillis() / 1000 < MyPreference.getValueString(
                            "timeTo",
                            ""
                        )!!.toLong()
                    ) {
                        if (trackPath.carSpeed == 0) {
                            snpt =
                                Utils.formatDurationFullValues(((System.currentTimeMillis() / 1000) - trackPath.dataList[i].toList.to_t).toLong())
                            if ((System.currentTimeMillis() / 1000) - trackPath.dataList[i].toList.to_t < 300)
                                icon = R.drawable.ic_stop
                            addOriginDestinationMarkerAndGet(
                                LatLng(
                                    trackPath.dataList[i].toList.to_y,
                                    trackPath.dataList[i].toList.to_x
                                ),
                                Utils.getDateFromMillis(
                                    (trackPath.dataList[i].toList.to_t * 1000).toLong(),
                                    inputFormat
                                ).toString(),
                                snpt,
                                icon
                            )
                        }
                    } else {
                        snpt = Utils.formatDurationFullValues(
                            (MyPreference.getValueString(
                                "timeTo",
                                ""
                            )!!.toLong() - trackPath.dataList[i].toList.to_t).toLong()
                        )
                        if (MyPreference.getValueString(
                                "timeTo",
                                ""
                            )!!.toLong() - trackPath.dataList[i].toList.to_t < 300
                        )
                            icon = R.drawable.ic_stop

                        addOriginDestinationMarkerAndGet(
                            LatLng(
                                trackPath.dataList[i].toList.to_y,
                                trackPath.dataList[i].toList.to_x
                            ),
                            Utils.getDateFromMillis(
                                (trackPath.dataList[i].toList.to_t * 1000).toLong(),
                                inputFormat
                            ).toString(),
                            snpt,
                            icon
                        )
                    }
                } else {
                    if (trackPath.endTimeStamp - trackPath.dataList[i].toList.to_t < 300)
                        icon = R.drawable.ic_stop
                    addOriginDestinationMarkerAndGet(
                        LatLng(
                            trackPath.dataList[i].toList.to_y,
                            trackPath.dataList[i].toList.to_x
                        ),
                        Utils.getDateFromMillis(
                            (trackPath.dataList[i].toList.to_t * 1000).toLong(),
                            inputFormat
                        ).toString(),
                        Utils.formatDurationFullValues((trackPath.endTimeStamp - trackPath.dataList[i].toList.to_t).toLong()),
                        icon
                    )
                }

                Log.d(TAG, "getLatLngListForPath: $i ${trackPath.dataList.size}")
                if (i > 0)
                    lastParkingDate = trackPath.dataList[i - 1].toList.to_t * 1000
                else
                    lastParkingDate =
                        trackPath.dataList[trackPath.dataList.lastIndex].toList.to_t * 1000
                Log.d(TAG, "getLatLngListForPath: fromDate $fromDate")
            }


            if (isFirstTime) {
                map?.setOnMarkerClickListener {
                    it.showInfoWindow()
                    return@setOnMarkerClickListener true
                }
            }
        }
//        } catch (e: Exception) {
//            Log.e(TAG, "getLatLngListForPath: e1 ${e.message}")
//        }
    }


    var oldLocationList = ArrayList<LatLng>()
    var newLocationList = ArrayList<LatLng>()

    @SuppressLint("PotentialBehaviorOverride", "SimpleDateFormat")
    private fun getLatLngListForPathGps3() {
        carLocationList = ArrayList()
        carLocationList.clear()
        carSpeedList.clear()
        carTimeList.clear()
        try {
            for (item in trackPath.routeRootGps3!!.route!!.indices) {
                val lt = trackPath.routeRootGps3!!.route!![item].lat.toDouble()
                val ln = trackPath.routeRootGps3!!.route!![item].lng.toDouble()
                carLocationList.add(LatLng(lt, ln))
                builder.include(LatLng(lt, ln))

                if (item + 1 <= trackPath.routeRootGps3!!.route!!.lastIndex) {
                    carSpeedList.add(trackPath.routeRootGps3!!.route!![item].speed)
                    carTimeList.add(
                        (getTimeStamp(trackPath.routeRootGps3!!.route!![item + 1].dt_tracker) / 1000 -
                                getTimeStamp(trackPath.routeRootGps3!!.route!![item].dt_tracker) / 1000)
                    )
                } else {
                    carSpeedList.add(trackPath.routeRootGps3!!.route!![item].speed)
                    carTimeList.add(15)
                }
                if (item == trackPath.routeRootGps3!!.route!!.size - 1) {

                    val date = Utils.getDateFromMillis(
                        getTimeStamp(trackPath.routeRootGps3!!.stops!![trackPath.routeRootGps3!!.stops!!.lastIndex].dt_start) + tz.getOffset(
                            getTimeStamp(trackPath.routeRootGps3!!.stops!![trackPath.routeRootGps3!!.stops!!.lastIndex].dt_start)
                        ), "yyyy-MM-dd HH:mm:ss"
                    )

                    var snipt: String = Utils.formatDurationFullValues(
                        (getTimeStamp(timeTo) / 1000) - (getTimeStamp(date) / 1000)
                    )
                    if (Utils.getCurrentDate("yyyy-MM-dd") == date!!.subSequence(0, 10)) {
                        snipt = Utils.formatDurationFullValues(
                            (getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")) / 1000) -
                                    (getTimeStamp(date) / 1000)
                        )
                    }

                    Log.e(TAG, "getLatLngListForPathGps3: sp ${trackPath.routeRootGps3!!.route!![item].speed}")
                    addOriginLastParkingIcon(
                        LatLng(
                            trackPath.routeRootGps3!!.route!![item].lat.toDouble(),
                            trackPath.routeRootGps3!!.route!![item].lng.toDouble()
                        ),
                        date, snipt,
                        trackPath.routeRootGps3!!.route!![item].angle.toFloat(),trackPath.routeRootGps3!!.route!![item].speed
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatLngListForPathGps3: e1 ${e.message}")
        }
        drawPolylineGps3(carLocationList)

        if (oldLocation == null) {
            oldLocation = carLocationList[carLocationList.lastIndex]
        }

        var oldIndex: Int = trackPath.routeRootGps3!!.stops!!.size - 1
        try {
            Log.d(TAG, "getLatLngListForPathGps3:date ${getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss"))} ${getTimeStamp(endDate)}")

            if (getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")) < getTimeStamp(timeTo)) {
                isMove = !isMove
                (requireActivity() as MainActivity).homeMapViewModel.carCarDetailsGPS3.observe(this) {
                    historyListViewModel.callApiForRouteGps3(itemId!!, startDate, endDate, 0.5f)
                }
                var latlang: LatLng? = null
                var oldSpeed = 0
                historyListViewModel.getRoutesGps3.observe(viewLifecycleOwner) {
                    trackPath.routeRootGps3 = it
                    try {
                        if (it.stops!!.size - 1 > oldIndex) createStopsAndParkingPoints(oldIndex)
                        oldIndex = it.stops!!.size - 1
                        binding.totalTimeValue.text = trackPath.routeRootGps3!!.drives_duration
                        setDistance(trackPath.routeRootGps3!!.route_length * 1000)
                        Log.d(
                            TAG,
                            "markerGps3: ss ${it.route!![it.route!!.lastIndex].speed} $oldSpeed"
                        )
                        if (it!!.route!![it.route!!.size - 1].speed + oldSpeed != 0) {
                            createCustomMarkerGps3(it.route!![it.route!!.size - 1])
                            ismove = true
                            isDone = false
                            carLocationList.clear()
                            carSpeedList.clear()
                            carTimeList.clear()
                            for (i in it.route!!.indices) {
                                val lt = it.route!![i].lat.toDouble()
                                val ln = it.route!![i].lng.toDouble()
                                carLocationList.add(LatLng(lt, ln))
                                carSpeedList.add(it.route!![i].speed)
                                if (i < it.route!!.lastIndex) {
                                    carTimeList.add(
                                        (getTimeStamp(it.route!![i + 1].dt_tracker) / 1000 -
                                                getTimeStamp(it.route!![i].dt_tracker) / 1000)
                                    )
                                } else {
                                    carTimeList.add(15)
                                }
                            }
                            drawPolylineGps3(carLocationList)

                            var ind = 0
                            Log.d(TAG, "getLatLngListForPathGps3:ttttt ${oldLocationList.size} ")

                            if (oldLocationList.isNotEmpty()) {
                                newLocationList.clear()
                                newLocationList.addAll(carLocationList - oldLocationList)
                                Log.d(TAG, "getLatLngListForPathGps3:tt  ${newLocationList.size} ")
                                val time: Double = (5.0 / newLocationList.size) * 1000.0

                                if (newLocationList.isNotEmpty() && carLocationList.size > oldLocationList.size) {
                                    Log.d(
                                        TAG,
                                        "getLatLngListForPathGps3:last ${oldLocationList[oldLocationList.lastIndex]} -- ${newLocationList[0]} /// ${newLocationList[newLocationList.lastIndex]}"
                                    )
                                    liverunnablelocation = Runnable {
                                        livehandlerlocation?.postDelayed(
                                            liverunnablelocation!!, time.toLong()
                                        )
                                        try {
                                            newLocation = LatLng(
                                                newLocationList[ind].latitude,
                                                newLocationList[ind].longitude
                                            )
                                            if (marker != null && newLocation != null && oldLocation != null) {
                                                CarMoveAnim.liveCarAnimation(
                                                    marker, oldLocation, newLocation, time
                                                )
                                            }
                                            if (ind == newLocationList.size - 1) {
                                                livehandlerlocation?.removeCallbacks(
                                                    liverunnablelocation!!
                                                )
                                            } else {
                                                ind++
                                            }
                                            oldLocation = newLocation
                                        } catch (e: Exception) {
                                            Log.e(
                                                TAG, "getLLLstForPathGps3: CATCH ${e.message}",
                                            )
                                        }

                                    }
                                    livehandlerlocation!!.post(liverunnablelocation!!)
                                }
                            }
                            oldLocationList.clear()
                            oldLocationList.addAll(carLocationList)
                        } else {
                            if (!isDone) {
                                ismove = false
                                createCustomMarkerGps3(it.route!![it.route!!.size - 1])
                                isDone = true
                            }
                        }
                        oldSpeed = it.route!![it.route!!.size - 1].speed
                    } catch (e: Exception) {
                        DebugLog.e(e.message.toString())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatLngListForPathGps3: e2 ${e.message}")
        }

        try {
            val bounds = builder.build()
            map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200), 1000, null)
        } catch (e: Exception) {
            Toast.makeText(
                context, getString(R.string.no_track_available_on_this_dates), Toast.LENGTH_LONG
            ).show()
            activity?.onBackPressed()

            binding.startTracking.visibility = View.GONE
            Log.d(TAG, "onCreateView: " + e.message)
        }
        try {
            map?.setOnMarkerClickListener {
                it.showInfoWindow()
                return@setOnMarkerClickListener true
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatLngListForPathGps3: e3 ${e.message}")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    //========================================================================================
    private fun drawPolyline(track: String) {
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(track)).width(width)
            .color(ContextCompat.getColor(requireContext(), R.color.color_blue))
        map?.addPolyline(polyline)
    }

    var width = 6f;
    private fun drawPolylineGps3(latLngList: ArrayList<LatLng>) {
        if (latLngList.size > oldCarLocationList.size) {
//            width *= 2
            val temp: ArrayList<LatLng> = ArrayList()
            temp.addAll(latLngList)
            if (oldCarLocationList.isNotEmpty()) {
                temp.clear()
                temp.add(oldCarLocationList.last())
                temp.addAll(latLngList - oldCarLocationList)
            }
            val bluePolylineOptions = PolylineOptions()
            if (oldCarLocationList.size == 0)
                bluePolylineOptions.color(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_blue
                    )
                )
            else bluePolylineOptions.color(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red
                )
            )
            bluePolylineOptions.width(width)
            bluePolylineOptions.addAll(temp)
            map?.addPolyline(bluePolylineOptions)
        }
        oldCarLocationList.clear()
        oldCarLocationList.addAll(latLngList)
    }

    private fun addOriginDestinationMarkerAndGet(
        latLng: LatLng, title: String, text: String, icon: Int,
    ) {
        try {
            map?.addMarker(
                MarkerOptions().position(latLng).flat(true)
                    .icon(Utils.bitmapDescriptorFromVector(requireContext(), icon))
                    .snippet(text).title(title)
            )!!
        } catch (e: Exception) {
            Log.e(TAG, "addOriginDestinationMarkerAndGet: CATCH ${e.message}")
        }
    }

    @SuppressLint("CheckResult", "InflateParams")
    private fun addOriginLastParkingIcon(
        latLng: LatLng, title: String,
        snippet: String, angle: Float,speed:Int
    ) {
        val inflater: LayoutInflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
        val imageView: ImageView =
            markerView.findViewById<View>(R.id.image) as ImageView

        val imageArrow: AppCompatImageView =
            markerView.findViewById<View>(R.id.image_arrow) as AppCompatImageView

        imageArrow.visibility = View.GONE

        if (speed > 0) {
            imageArrow.visibility = View.VISIBLE
        } else {
            imageArrow.visibility = View.GONE
        }
        val img = Glide.with(requireContext()).asBitmap()
        img.placeholder(R.drawable.default_car)
        img.error(R.drawable.default_car)
        if (serverData.contains("s3")) {
            img.load(R.drawable.default_car)
        } else img.load(
            "http://gps.tawasolmap.com" +
                    MyPreference.getValueString(PrefKey.CAR_URI, "")
        )
        img.diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap?>(100, 100) {
                override fun onResourceReady(
                    resource: Bitmap, transition: Transition<in Bitmap?>?
                ) {
                    if (marker != null) {
                        marker!!.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    imageView, resource, markerView!!
                                )!!
                            )
                        )
                    } else {
                        marker = map?.addMarker(
                            MarkerOptions()
                                .position(latLng).flat(true)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageView, resource, markerView!!
                                        )!!
                                    )
                                )
                                .anchor(0.5f, 0.5f).rotation(angle)
                        )
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun loadMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    var findParkingRenderer: CarOnMapRenderer? = null

    @SuppressLint("ResourceAsColor", "PotentialBehaviorOverride")

    var lastlatlang: LatLng? = null

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(mMap: GoogleMap) {
        if (isGooglePlayServicesAvailable()) {
            map = mMap
            map?.setPadding(0, 0, 0, 185)

            /*if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            } else {
            }*/
            if (Locale.getDefault().displayLanguage.equals("العربية"))
                binding.cardViewMain.layoutDirection = View.LAYOUT_DIRECTION_RTL
            else binding.cardViewMain.layoutDirection = View.LAYOUT_DIRECTION_LTR
            val ltr: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.left_to_right)
            val rtl: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.right_to_left)

            binding.floatingActionButton.setOnClickListener {
                isFAB = !isFAB
                if (isFAB) {
                    binding.floatingActionButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_baseline_close_24
                        )
                    )
                    binding.cardViewMain.visibility = View.VISIBLE
                    binding.cardViewMain.startAnimation(ltr)
                } else {
                    binding.floatingActionButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_baseline_play_circle_24
                        )
                    )
                    binding.cardViewMain.startAnimation(rtl)
                    binding.cardViewMain.visibility = View.INVISIBLE
                }
            }
            try {

                (requireActivity() as MainActivity).mapSettings(map!!)
                map?.setOnMyLocationButtonClickListener {
                    checkLocationSettings()
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "catch onMapReady: e1 ${e.message}")
            }
            if (!serverData.contains("s3")) {
                viewmodel.callLocation()
                //setUpCluster()
                val timeToStamp = MyPreference.getValueString("timeTo", "")!!.toLong()
                val timeFromStamp = MyPreference.getValueString("timeFrom", "")!!.toLong()
                val date = Utils.getDateFromMillis(
                    timeToStamp * 1000, "dd/MM/yyyy"
                )
                Log.d(TAG, "onMapReady: date ${Utils.getCurrentDate("dd/MM/yyyy")} $date")
                if (getTimeStamp(Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")) < getTimeStamp(timeTo)) {

                    viewmodel.tripData.observe(requireActivity()) {
                        if (it != null && it.length() > 0) {
                            try {
                                Log.d(TAG, "initListeners:le ${it.length()}")
                                val tripsObj = JSONObject(it.get(1).toString())
                                val tripsObjTrips = tripsObj.getJSONObject("trips")
                                val stopTripOnject =
                                    JSONObject(it.get(2).toString()).getJSONObject("trips")
                                val trips: TripDetails.Trips =
                                    Gson().fromJson(
                                        tripsObjTrips.toString(), TripDetails.Trips::class.java
                                    )
                                val stopData: TripDetails.Trips =
                                    Gson().fromJson(
                                        stopTripOnject.toString(), TripDetails.Trips::class.java
                                    )
                                if (trips.getFirstObj().size > 0) {
                                    val oldStopIndex: Int = trackPath.stopDataList.size - 1
                                    val oldParkingIndex: Int = trackPath.dataList.size - 1
                                    trackPath.dataList.clear()
                                    trackPath.stopDataList.clear()
                                    trackPath.dataList.addAll(trips.getFirstObj())
                                    trackPath.stopDataList.addAll(stopData.getFirstObj())
                                    if (trackPath.dataList.size - 1 > oldParkingIndex)
                                        getLatLngListForPath(oldParkingIndex)
                                    if (trackPath.stopDataList.size - 1 > oldStopIndex)
                                        setStopLatLong(oldStopIndex)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "onMapReady: e1 ${e.message}")
                            }
                        }
                    }
                }
                var oldSpeed = 0

                viewmodel.locationList.observe(viewLifecycleOwner) {
                    carLocationList = ArrayList()
                    carLocationList.clear()
                    carSpeedList.clear()
                    carTimeList.clear()
                    index = 0
                    var result: Long
                    carTimeList.add(15)
                    if (it != null && it.messages != null && it.messages.isNotEmpty()) {
                        try {
                            trackPath.carSpeed = it.messages[it.messages.size - 1].pos.s
                            trackPath.carAngle = it.messages[it.messages.size - 1].pos.c

                            for (item in 0 until it.messages.size) {
                                if (it.messages[item].pos != null) {
                                    if (item + 1 < it.messages.size) {
                                        val num2 = it.messages[item].t!!
                                        val num1: Long = it.messages[item + 1].t!!
                                        result = num1 - num2
                                        carTimeList.add(result)
                                        index++
                                    }
                                    carSpeedList.add(it.messages[item].pos.s)
                                    carLocationList.add(
                                        LatLng(it.messages[item].pos.y, it.messages[item].pos.x)
                                    )
                                    builder.include(
                                        LatLng(
                                            it.messages[item].pos.y,
                                            it.messages[item].pos.x
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "onMapReady: e2 ${e.message}")
                        }

                    }
                    Log.d(TAG, "onResponse: listSize ${carLocationList.size}")
                    if (carLocationList.size == 0) {
                        Toast.makeText(
                            context, getString(R.string.no_track_available_on_this_dates),
                            Toast.LENGTH_LONG
                        ).show()
                        activity?.onBackPressed()
                        binding.startTracking.visibility = View.GONE
                    } else {
                        drawPolylineGps3(carLocationList)


                        /*           if (oldLocation == null) {
           //                            newLocation = LatLng(
           //                                carLocationList[carLocationList.lastIndex].latitude,
           //                                carLocationList[carLocationList.lastIndex].longitude
           //                            )
                                       oldLocation = carLocationList[carLocationList.lastIndex]

                                       Log.d(
                                           TAG,
                                           "getLatLngListForPathGps3:last2ooooo  ${oldLocation} ///  "
                                       )
                                   }*/
                        /* else {
                             newLocation = LatLng(
                                 carLocationList[carLocationList.lastIndex].latitude,
                                 carLocationList[carLocationList.lastIndex].longitude
                             )
                         }*/
                        Log.d(TAG, "onMapReady:$isFirstTime ")
                        try {
                            if (!isFirstTime && it.messages[it.messages.lastIndex].pos != null) {
                                if (it.messages[it.messages.size - 1].pos.s + oldSpeed != 0) {
                                    createCustomMarker(
                                        LatLng(
                                            carLocationList[carLocationList.lastIndex].latitude,
                                            carLocationList[carLocationList.lastIndex].longitude
                                        ),
                                        it.messages[it.messages.lastIndex].pos.c.toFloat(),
                                        it.messages[it.messages.lastIndex].pos.s
                                    )
                                    ismove = true
                                    isDone = false


                                    var ind: Int = 0
                                    if (oldLocationList.isNotEmpty()) {

                                        newLocationList.clear()

                                        newLocationList.addAll(carLocationList - oldLocationList)


                                        val time: Double = (5.0 / newLocationList.size) * 1000.0

                                        if (newLocationList.isNotEmpty() && carLocationList.size > oldLocationList.size) {
                                            liverunnablelocation = Runnable {
                                                livehandlerlocation?.postDelayed(
                                                    liverunnablelocation!!,
                                                    time.toLong()
                                                )

                                                try {
                                                    if (oldLocation == null) {
                                                        oldLocation = lastlatlang

                                                    }
                                                    newLocation = LatLng(
                                                        newLocationList[ind].latitude,
                                                        newLocationList[ind].longitude
                                                    )

                                                    if (marker != null && newLocation != null && oldLocation != null) {
                                                        CarMoveAnim.liveCarAnimation(
                                                            marker, oldLocation, newLocation, time
                                                        )
                                                    }

                                                    if (ind == newLocationList.size - 1) {
                                                        livehandlerlocation?.removeCallbacks(
                                                            liverunnablelocation!!
                                                        )
                                                    } else {
                                                        ind++
                                                    }
                                                    oldLocation = newLocation
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "onMapReady: CATCH ${e.message}")
                                                }

                                            }
                                            livehandlerlocation!!.post(liverunnablelocation!!)
                                        }
                                    }
                                    oldLocationList.clear()
                                    oldLocationList.addAll(carLocationList)

                                } else {
                                    if (!isDone) {
                                        ismove = false
                                        createCustomMarker(
                                            newLocation!!,
                                            it.messages[it.messages.lastIndex].pos.c.toFloat(),
                                            it.messages[it.messages.lastIndex].pos.s
                                        )
                                        isDone = true
                                    }
                                }
                                oldSpeed = it.messages[it.messages.lastIndex].pos.s

                            } else {
                                oldLocationList.clear()
                                oldLocationList.addAll(carLocationList)
                                lastlatlang = carLocationList[carLocationList.lastIndex]
                                val snippet: String
                                val date1: String
                                if (Utils.getCurrentDate("yyyy-MM-dd") ==
                                    Utils.getDateFromMillis(
                                        it.messages[it.messages.lastIndex].t * 1000,
                                        "yyyy-MM-dd"
                                    )!!
                                ) {
                                    snippet = Utils.formatDurationFullValues(
                                        (getTimeStamp(
                                            Utils.getCurrentDate("yyyy-MM-dd HH:mm:ss")
                                        ) / 1000) - it.messages[it.messages.lastIndex].t
                                    )
                                    date1 = Utils.getDateFromMillis(
                                        it.messages[it.messages.lastIndex].t * 1000,
                                        "yyyy-MM-dd HH:mm:ss"
                                    )!!
                                } else {
                                    snippet = Utils.formatDurationFullValues(
                                        (getTimeStamp(timeTo) - lastParkingDate.toLong()) / 1000
                                    )
                                    date1 = Utils.getDateFromMillis(
                                        lastParkingDate.toLong(), inputFormat
                                    ).toString()
                                    Log.d(
                                        TAG,
                                        "onMapReady: hassan $snippet $date1 ${
                                            getTimeStamp(timeTo)
                                        }  ${trackPath.dataList[trackPath.dataList.size - 1].fromList.from_t * 1000}"
                                    )
                                }


                                Log.d(TAG, "onMapReady:speed ${trackPath.carSpeed}  ")
                                addOriginLastParkingIcon(
                                    LatLng(
                                        carLocationList[carLocationList.lastIndex].latitude,
                                        carLocationList[carLocationList.lastIndex].longitude
                                    ), date1, snippet, trackPath.carAngle.toFloat(),trackPath.carSpeed
                                )
                                try {
                                    val bounds = builder.build()
                                    map?.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(bounds, 200),
                                        1000, null
                                    )

                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.no_track_available_on_this_dates),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    activity?.onBackPressed()

                                    binding.startTracking.visibility = View.GONE
                                    Log.d(TAG, "onCreateView: " + e.message)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "onMapReady: e3 ${e.message}")
                        }
                        // oldLocation = newLocation
                        carTimeList.add(15)
                    }
                }
            }
            binding.startTracking.setOnClickListener {
                try {
                    isPlaying = !isPlaying
                    if (isPlaying) {
                        imageArrowCar.visibility = View.VISIBLE
                        CarMoveAnim.stopFlag = 1
                        binding.startTracking.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
                    } else {
                        imageArrowCar.visibility = View.INVISIBLE
                        CarMoveAnim.stopFlag = 0
                        val index = MyPreference.getValueInt(PrefKey.HandlerIndex, 0)
                        if (index != 0)
                            MyPreference.setValueInt(PrefKey.HandlerIndex, (index - 1))
                        binding.startTracking.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                    }
                    index = MyPreference.getValueInt(PrefKey.HandlerIndex, 0)
                    if (index == 0) {
                        var startLatLng = LatLng(46.689784, 24.793458)
                        if (carLocationList.size > 0)
                            startLatLng = carLocationList[0]
                        map?.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder().target(startLatLng).zoom(13.64f).build()
                            )
                        )
                    }
                    showMovingCar(carLocationList)
                } catch (e: Exception) {
                    Log.e(TAG, "catch onMapReady: ${e.message}")
                }
            }
            binding.start3d.setOnClickListener {
                is3D = !is3D
                if (is3D)
                    binding.start3d.setImageResource(R.drawable.ic_baseline_3d_rotation_24_dark)
                else
                    binding.start3d.setImageResource(R.drawable.ic_baseline_3d_rotation_24)

            }
            binding.centerIv.setOnClickListener {
                isCentered = !isCentered
                if (isCentered)
                    binding.centerIv.setImageResource(R.drawable.car_dark)
                else
                    binding.centerIv.setImageResource(R.drawable.car_light)
            }

            if (serverData.contains("s3"))
                getIntentDataGps3()
            else getIntentData()

            mMap?.setInfoWindowAdapter(object : InfoWindowAdapter {
                // Use default InfoWindow frame
                override fun getInfoWindow(marker: Marker): View? {
                    binding.mapClick.visibility = View.GONE
                    return null
                }

                // Defines the contents of the InfoWindow
                @SuppressLint("InflateParams")
                override fun getInfoContents(marker: Marker): View? {
                    try {
                        // Getting view from the layout file info_window_layout
                        if (marker.title != null && marker.snippet != null && marker.snippet!!.isNotEmpty()) {
                            val v: View = layoutInflater.inflate(R.layout.custom_info, null)
                            val tvLat = v.findViewById<View>(R.id.info) as TextView
                            tvLat.text = marker.snippet
                            val title = v.findViewById<View>(R.id.title) as TextView
                            title.text = marker.title
                            binding.mapClick.visibility = View.VISIBLE
                            binding.mapClick.setOnClickListener {
                                val uri: String = java.lang.String.format(
                                    Locale.ENGLISH, "geo:%f,%f",
                                    marker.position.latitude, marker.position.longitude
                                )
                                val urlAddress =
                                    "http://maps.google.com/maps?q=" + marker.position.latitude.toString() + "," + marker.position.longitude.toString() + "(" + marker.title!!.toString() + ")&iwloc=A&hl=es"

                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress))
                                context!!.startActivity(intent)
                            }
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLng(
                                    LatLng(marker.position.latitude, marker.position.longitude)
                                )
                            )
                            return v
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "catch getInfoContents: ${e.message}")
                    }

                    return null
                }
            })
            mMap?.setOnInfoWindowCloseListener(OnInfoWindowCloseListener {
                //hide or do something you want
                binding.mapClick.visibility = View.GONE
            })
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
                DebugLog.i("Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings ")

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
        isPlaying = false

        binding.startTracking.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        (activity as MainActivity).txt_toolbar_title.text = trackPath.carName

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
        MyPreference.RemoveItem(PrefKey.Index)
        MyPreference.RemoveItem("lat")
        MyPreference.RemoveItem("lng")
        historyListViewModel.isNewData.postValue("false")

        if (runnable != null)
            handler?.removeCallbacks(runnable!!)
    }

    private fun moveCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(20f).build()
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        val center = CameraUpdateFactory.newLatLng(latLng)
        map?.moveCamera(center)
        map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    var index = 0

    @SuppressLint("SetTextI18n")
    private fun showMovingCar(cabLatLngList: ArrayList<LatLng>) {
        if (runnable != null)
            handler?.removeCallbacks(runnable!!)

        var index = MyPreference.getValueInt(PrefKey.HandlerIndex, 0)
        var pos = 1
        when (binding.accountSpinner.selectedItemPosition) {
            // position using to increase or decrease car speed
            0 -> pos = 1
            1 -> pos = 10
            2 -> pos = 25
            3 -> pos = 50
            4 -> pos = 75
            5 -> pos = 100
        }
        runnable = Runnable {
            run {
                if (isPlaying) {
                    Log.d(TAG, "showMovingCar index $index")
                    if (index <= cabLatLngList.size - 1) {
                        if (carSpeedList[index] > 0) {
                            CarMoveAnim.time = (carTimeList[index] * 1000 / pos).toInt()
                            handler!!.postDelayed(
                                runnable!!, CarMoveAnim.time.toLong()
                            )
                        } else {
                            CarMoveAnim.time = 15
                            handler!!.postDelayed(runnable!!, CarMoveAnim.time.toLong())
                        }
                        if (index < carTimeList.size) {
                            binding.tv.text = "${carSpeedList[index]} km/h"
                        }
                        updateCarLocation(
                            cabLatLngList[index]
                        )
                        index++
                        MyPreference.setValueInt(PrefKey.HandlerIndex, index)
                    } else {
                        index = 0
                        MyPreference.setValueInt(PrefKey.HandlerIndex, index)
                        handler!!.removeCallbacks(runnable!!)
                        binding.startTracking.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                        isPlaying = false
                        previousLatLng = null
                        if (context != null) {
                            binding.tv.text = getString(R.string.trip_ends)
                            MyPreference.RemoveItem(PrefKey.Index)
                            MyPreference.RemoveItem("lat")
                            MyPreference.RemoveItem("lng")
                            movingCabMarker?.remove()
                            Log.d(TAG, "movingCabMarker: remove")
                            movingCabMarker = null
                        }
                    }
                }
            }
        }
        handler!!.post(runnable!!)
    }

    private fun updateCarLocation(latLng: LatLng) {
        if (movingCabMarker == null) {
            if (serverData.contains("s3")) {
                Glide.with(requireContext()).asBitmap()
                    .placeholder(R.drawable.default_car)
                    .error(R.drawable.default_car)
                    .load(
                        R.drawable.default_car
                    ).fitCenter().into(object : CustomTarget<Bitmap?>(100, 100) {
                        override fun onResourceReady(
                            resource: Bitmap, transition: Transition<in Bitmap?>?
                        ) {
                            if (movingCabMarker != null) {
                                movingCabMarker?.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageViewCar, resource, markerViewCar
                                        )!!
                                    )
                                )
                            } else {
                                movingCabMarker = map?.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .flat(true)
                                        .icon(
                                            BitmapDescriptorFactory.fromBitmap(
                                                getMarkerBitmapFromView(
                                                    imageViewCar, resource, markerViewCar
                                                )!!
                                            )
                                        )
                                        .anchor(0.5f, 0.5f).snippet("")
                                )
                                Log.d(TAG, "movingCabMarker:0 $movingCabMarker")

                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } else
                addCarMarkerAndGetMoving(latLng)
            moveCamera(latLng)
        }
        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng!!
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            Log.d(TAG, "updateCarLocation: hassan null")

        } else {
            previousLatLng = if (MyPreference.getValueString("lat", "") != "")
                LatLng(
                    MyPreference.getValueString("lat", "")!!.toDouble(),
                    MyPreference.getValueString(
                        "lng",
                        ""
                    )!!.toDouble()
                )
            else currentLatLng
            currentLatLng = latLng
            if (movingCabMarker != null)
                CarMoveAnim.startcarAnimation(
                    movingCabMarker, map, previousLatLng, currentLatLng, is3D, isCentered
                )
        }
    }

    @SuppressLint("CheckResult")
    private fun addCarMarkerAndGetMoving(latLng: LatLng) {
        Glide.with(requireContext()).asBitmap()
            .placeholder(R.drawable.default_car)
            .error(R.drawable.default_car)
            .load(
                "http://gps.tawasolmap.com" + MyPreference.getValueString(
                    PrefKey.CAR_URI, ""
                )
            ).fitCenter().into(object : CustomTarget<Bitmap?>(100, 100) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    if (movingCabMarker != null) {
                        movingCabMarker?.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    imageViewCar, resource, markerViewCar
                                )!!
                            )
                        )
                    } else {
                        movingCabMarker = map?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .flat(true)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmapFromView(
                                            imageViewCar, resource, markerViewCar
                                        )!!
                                    )
                                )
                                .anchor(0.5f, 0.5f)
                        )
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    //    private fun setUpCluster() {
//        mClusterManager = ClusterManager(context, map!!)
//        map!!.setOnCameraIdleListener(mClusterManager)
//        findParkingRenderer = CarOnMapRenderer(requireActivity(), map!!, mClusterManager!!)
//        mClusterManager!!.renderer = findParkingRenderer
//    }
    override fun onDestroy() {
        historyListViewModel.isNewData.postValue("false")
        if (!isBacked) {
            if (runnable != null)
                handler!!.removeCallbacks(runnable!!)
            MyPreference.RemoveItem(PrefKey.Index)
            MyPreference.RemoveItem("lat")
            MyPreference.RemoveItem("lng")
        }
        super.onDestroy()
    }
}