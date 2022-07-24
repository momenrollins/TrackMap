package com.houseofdevelopment.gps.track


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.houseofdevelopment.gps.DataValues.serverData
import com.houseofdevelopment.gps.DataValues.showSpeed
import com.houseofdevelopment.gps.DataValues.tz
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentTracksBinding
import com.houseofdevelopment.gps.history.model.TripDetails
import com.houseofdevelopment.gps.history.viewmodel.HistoryListViewModel
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.homemap.model.UpdatedUnitModel
import com.houseofdevelopment.gps.homemap.model.UpdatedUnitModelGps3
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.report.model.CarListingDialogGps3
import com.houseofdevelopment.gps.track.model.TrackPathModel
import com.houseofdevelopment.gps.track.viewmodel.TrackViewModel
import com.houseofdevelopment.gps.utils.CarListingDialog
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

class TracksFragment : BaseFragment() {

    lateinit var binding: FragmentTracksBinding

    private lateinit var viewmodel: TrackViewModel
    private var carDetailList: MutableList<Item>? = null
    var carId = ""
    private var inputFormat = "dd/MM/yyyy HH:mm:ss"
    private val dateFormat = SimpleDateFormat(inputFormat, Locale.US)
    private var selectedPosiotion = 0
    private var trackPathModelProvider = TrackPathModel()
    var carDetailsGPS3: ArrayList<ItemGps3> = ArrayList()
    private lateinit var historyListViewModel: HistoryListViewModel

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private lateinit var viewModelGps3: HistoryListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelGps3 = ViewModelProvider(this)[HistoryListViewModel::class.java]
        viewmodel = ViewModelProvider(this)[TrackViewModel::class.java]
        trackPathModelProvider = TrackPathModel()
        historyListViewModel = ViewModelProvider(this)[HistoryListViewModel::class.java]
        if (Utils.getCarListingDataGps3(
                requireContext()
            ).items.size > 0
        ) {
            carDetailsGPS3 = Utils.getCarListingDataGps3(requireContext()).items
            carId = carDetailsGPS3[selectedPosiotion].imei
            MyPreference.setValueString("itemId", carId)
            carDetailsGPS3[selectedPosiotion].name.let {
                carDetailsGPS3[selectedPosiotion].imei.let { it2 ->
                    UpdatedUnitModelGps3(
                        it, it2,
                    )
                }
            }.let {
                trackPathModelProvider.selectedCarDataGps3.add(it)
                trackPathModelProvider.carName = it.name

            }
        }
        if (Utils.getCarListingData(
                requireContext()
            ).items.size > 0
        ) {
            carDetailList = Utils.getCarListingData(requireContext()).items
            carId = carDetailList!![selectedPosiotion].id.toString()
            MyPreference.setValueString("itemId", carId)
            MyPreference.setValueString(
                PrefKey.CAR_URI, carDetailList!![selectedPosiotion].uri.toString()
            )
            carDetailList!![selectedPosiotion].nm?.let {
                carDetailList!![selectedPosiotion].cls?.let { it1 ->
                    carDetailList!![selectedPosiotion].id?.let { it2 ->
                        carDetailList!![selectedPosiotion].bact?.let { it3 ->
                            UpdatedUnitModel(
                                it, it1, it2, it3
                            )
                        }
                    }
                }
            }?.let {
                trackPathModelProvider.selectedCarData.add(it)
                try {
                    trackPathModelProvider.carName = it.name
                    trackPathModelProvider.carSpeed =
                        carDetailList!![selectedPosiotion].trip_curr_speed!!.toInt()


                } catch (e: Exception) {
                    Log.e(TAG, "onCreate: CATCH ${e.message}")
                }


            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tracks, container, false)
        viewmodel.clearData()
        initListeners()
        getCurrentDate()
        dateDialogListener()
        setSpinner()
        binding.btnExecute.setOnClickListener {
//            if (handler != null) handler!!.removeCallbacks(runnable!!)
            trackPathModelProvider.dataList.clear()
            trackPathModelProvider.stopDataList.clear()
            trackPathModelProvider.dataListGps3.clear()
            val fromDateInterval =
                Utils.getMillisFromDate(binding.startDate.text.toString(), inputFormat)
            val toDateInterval =
                Utils.getMillisFromDate(binding.endDate.text.toString(), inputFormat)

            MyPreference.setValueString(
                "timeFrom",
                Utils.getMillisFromDate(binding.startDate.text.toString(), inputFormat).toString()
            )
            MyPreference.setValueString(
                "timeTo",
                Utils.getMillisFromDate(binding.endDate.text.toString(), inputFormat).toString()
            )

            binding.btnExecute.isEnabled=false
            if (serverData.contains("s3")) {
                val timeFrom = Utils.getDateFromMillis(
                    (MyPreference.getValueString("timeFrom", "")!!.toLong() * 1000),
                    "yyyy-MM-dd HH:mm:ss"
                ).toString()

                val timeTo = Utils.getDateFromMillis(
                    (MyPreference.getValueString("timeTo", "")!!.toLong() * 1000),
                    "yyyy-MM-dd HH:mm:ss"
                ).toString()
                val itemId = MyPreference.getValueString("itemId", "")

                Utils.showProgressBar(requireContext())
                //  viewmodel.loading(true)
                val startDate = Utils.getDateFromMillis(
                    ((getTimeStamp(timeFrom)) - tz.getOffset(
                        getTimeStamp(timeFrom)
                    )), "yyyy-MM-dd HH:mm:ss"
                )
                val endDate = Utils.getDateFromMillis(
                    ((getTimeStamp(timeTo)) - tz.getOffset(
                        getTimeStamp(timeTo)
                    )), "yyyy-MM-dd HH:mm:ss"
                )
                Log.d(TAG, "onCreateView: tt $timeFrom $timeTo $endDate")

                viewModelGps3.callApiForRouteGps3(itemId!!, startDate!!, endDate!!, 0.5f)

//                sendDataToMapPageGps3()

            } else viewmodel.callApiForUnloadHistoryDetails()
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    private fun getCurrentDate() {
        if (trackPathModelProvider.startTimeStamp > 0) {
            binding.startDate.text = trackPathModelProvider.startDate
            binding.endDate.text = trackPathModelProvider.endDate
        } else {
            val cal: Calendar = Calendar.getInstance()
            cal.time = Date()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            binding.startDate.text = dateFormat.format(cal.time)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            binding.endDate.text = dateFormat.format(cal.time)

            trackPathModelProvider.startDate = binding.startDate.text.toString()
            trackPathModelProvider.endDate = binding.endDate.text.toString()
            trackPathModelProvider.startTimeStamp =
                Utils.getMillisFromDate(binding.startDate.text.toString(), inputFormat)
            trackPathModelProvider.endTimeStamp =
                Utils.getMillisFromDate(binding.endDate.text.toString(), inputFormat)
        }

    }

    private fun dateDialogListener() {

        binding.startLayout.setOnClickListener {
            val year =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "yyyy")
                    .toInt()
            var month =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "MM")
                    .toInt()
            when (month > 0) {
                true -> month -= 1
            }
            val day = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "dd")
                .toInt()
            val hour = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "HH")
                .toInt()
            val minute =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "mm")
                    .toInt()
            val sec = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "ss")
                .toInt()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val timePickerDialog =
                        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->

                            binding.startDate.text = Utils.convertDateFormat(
                                dayOfMonth.toString() + "/" + (month + 1) + "/" + year.toString() + " " + hourOfDay + ":" + minute + ":" + sec,
                                inputFormat, inputFormat
                            )
                            trackPathModelProvider.startDate = binding.startDate.text.toString()
                            trackPathModelProvider.startTimeStamp = Utils.getMillisFromDate(
                                binding.startDate.text.toString(), inputFormat
                            )
                        }, hour, minute, true)
                    timePickerDialog.show()
                },
                year, month, day
            )
            datePickerDialog.datePicker.minDate = 1325368800000
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.endLayout.setOnClickListener {
            val year = Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "yyyy")
                .toInt()
            var month =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "MM").toInt()
            when (month > 0) {
                true -> month -= 1
            }
            val day =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "dd").toInt()
            val hour =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "HH").toInt()
            val minute =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "mm").toInt()
            val sec = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "ss")
                .toInt()

            val datePickerDialog1 = DatePickerDialog(
                requireContext(),
                { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val timePickerDialog =
                        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                            binding.endDate.text = Utils.convertDateFormat(
                                dayOfMonth.toString() + "/" + (month + 1) + "/" + year.toString() + " " + hourOfDay + ":" + minute + ":" + sec,
                                inputFormat, inputFormat
                            )
                            trackPathModelProvider.endDate = binding.endDate.text.toString()
                            trackPathModelProvider.endTimeStamp = Utils.getMillisFromDate(
                                binding.endDate.text.toString(), inputFormat
                            )
                        }, hour, minute, DateFormat.is24HourFormat(context))
                    timePickerDialog.show()
                },
                year, month, day
            )
            Log.d(TAG, "dateDialogListener: ${System.currentTimeMillis()}")
            datePickerDialog1.datePicker.minDate = 1325368800000
            datePickerDialog1.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog1.show()

        }
    }

    private fun initListeners() {
        viewmodel._status.observe(requireActivity()) {
            if (it.equals(ApiStatus.LOADING)) {
                Utils.showProgressBar(requireContext())
            } else {
                Utils.hideProgressBar()
            }
        }
        viewmodel.unloadtripData.observe(requireActivity()) {
            val fromDateInterval =
                Utils.getMillisFromDate(binding.startDate.text.toString(), inputFormat)
            val toDateInterval =
                Utils.getMillisFromDate(binding.endDate.text.toString(), inputFormat)
            viewmodel.callApiForTrackHistoryDetails(carId, fromDateInterval, toDateInterval)
        }

        viewmodel.tripData.observe(requireActivity()) {
            // tripData = it
            binding.btnExecute.isEnabled=true

            if (it != null && it.length() > 0) {
                Log.d(TAG, "initListeners:le ${it.length()}")
                val tripsObj = JSONObject(it.get(1).toString())
                val tripsObjTrips = tripsObj.getJSONObject("trips")
                val stopTripOnject = JSONObject(it.get(2).toString()).getJSONObject("trips")
                val trips: TripDetails.Trips =
                    Gson().fromJson(tripsObjTrips.toString(), TripDetails.Trips::class.java)
                val stopData: TripDetails.Trips =
                    Gson().fromJson(stopTripOnject.toString(), TripDetails.Trips::class.java)

                if (trips.getFirstObj().size > 0) {

                    sendDataToMapPage(trips.getFirstObj(), stopData.getFirstObj())
                    Log.d(
                        TAG,
                        "initListeners: last ${stopData.getFirstObj()[stopData.getFirstObj().lastIndex].fromList.from_t}"
                    )
                } else {
//                    sendDataToMapPage(trips.getFirstObj(), stopData.getFirstObj())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_track_available_on_this_dates),
                        Toast.LENGTH_SHORT
                    ).show()
                }

//                sendDataToMapPage(trips.getFirstObj(), stopData.getFirstObj())
            } else {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_track_available_on_this_dates),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModelGps3.isNewData.observe(viewLifecycleOwner) {
            binding.btnExecute.isEnabled=true

            if (it.equals("true")) {
                viewModelGps3.getRoutesGps3.observe(viewLifecycleOwner) {
                    Utils.hideProgressBar()
                    try {
                        if (it != null && it.route != null)
                            if (it.route!!.size > 0) {
                                trackPathModelProvider.routeRootGps3 = it

                                try {
                                    findNavController().navigate(
                                        TracksFragmentDirections.actionAddTrackMapFragmentToTrackFragment(
                                            trackPathModelProvider
                                        )
                                    )
                                } catch (e: Exception) {
                                    DebugLog.e(e.message!!)
                                }
                            } else {
                                viewModelGps3.isNewData.postValue("false")

                                Toast.makeText(
                                    AppBase.instance,
                                    getString(R.string.no_track_available_on_this_dates),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewmodel.loading(false)
                            }

                    } catch (e: Exception) {
                        Log.d(TAG, "initListeners: exception $e")
                    }
                }

            } else if (it.equals("no tracks")) {

                Toast.makeText(
                    AppBase.instance,
                    getString(R.string.no_track_available_on_this_dates),
                    Toast.LENGTH_SHORT
                ).show()
                viewModelGps3.isNewData.postValue("false")

                viewmodel.loading(false)
            }
        }

    }

    private fun sendDataToMapPage(
        tripData: MutableList<TripDetails.FirstObj>,
        stopData: MutableList<TripDetails.FirstObj>
    ) {
        try {

            trackPathModelProvider.dataList.addAll(tripData)
            trackPathModelProvider.stopDataList.addAll(stopData)

            findNavController().navigate(
                TracksFragmentDirections.actionAddTrackMapFragmentToTrackFragment(
                    trackPathModelProvider
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "sendDataToMapPage: CATCH ${e.message}")
        }

    }

    private val TAG = "TracksFragment"
    private fun setSpinner() {
        if (serverData.contains("s3")) {
            if (carDetailsGPS3.size > 0) {
                try {
                    binding.spSpinner.text = trackPathModelProvider.selectedCarDataGps3[0].name

                } catch (e: Exception) {
                    Log.e(TAG, "setSpinner: CATCH ${e.message}")
                }
                binding.selectCarLL.setOnClickListener {
                    showSpeed = true
                    val dialog =
                        CarListingDialogGps3(object : CarListingDialogGps3.CarClickListener {
                            override fun selectCarGps3(model: ItemGps3) {
                                trackPathModelProvider.selectedCarDataGps3.clear()
                                UpdatedUnitModelGps3(
                                    model.name, model.imei,
                                ).let { it2 ->
                                    trackPathModelProvider.selectedCarDataGps3.add(
                                        it2
                                    )
                                }
                                carId = model.imei
                                MyPreference.setValueString("itemId", model.imei.toString())
                                //  MyPreference.setValueString(PrefKey.CAR_URI, model.uri.toString())
                                trackPathModelProvider.carName = model.name
                                binding.spSpinner.text = model.name
                            }

                        }, requireContext())
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                }
            }
        } else {
            if (carDetailList != null && carDetailList!!.size > 0) {
                try {
                    binding.spSpinner.text = trackPathModelProvider.selectedCarData[0].name

                } catch (e: Exception) {
                    Log.e(TAG, "setSpinner: CATCH ${e.message}")
                }
                /*
                val nameList = mutableListOf<String>()
                 for (car in carDetailList!!) {
                     val name = car.nm.toString()
                     nameList.add(name)
                 }
                val adapter = ArrayAdapter(requireContext(), R.layout.template_layout_row, R.id.textView, nameList)
                 carId = carDetailList!![selectedPosiotion].id.toString()
                 binding.spSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                     override fun onItemSelected(
                             parent: AdapterView<*>?,
                             view: View?,
                             position: Int,
                             id: Long
                     ) {
                         // An item was selected. You can retrieve the selected item using
                         //val selectedItem = parent.getItemAtPosition(position) as String
                         selectedPosiotion = position
                         carId = carDetailList!![position].id.toString()
                     }

                     override fun onNothingSelected(parent: AdapterView<*>?) {
                         // If an option is removed then what to do
                         // or anything else
                     }

                 }
                 binding.spSpinner.adapter = adapter*/
                binding.selectCarLL.setOnClickListener {
                    showSpeed = true
                    val dialog =
                        CarListingDialog(object : CarListingDialog.CarClickListener {
                            override fun selectCar(model: Item) {
                                trackPathModelProvider.selectedCarData.clear()
                                model.nm?.let { it1 ->
                                    model.cls?.let { it2 ->
                                        model.id?.let { it3 ->
                                            model.bact?.let { it4 ->
                                                UpdatedUnitModel(
                                                    it1, it2, it3, it4
                                                )
                                            }
                                        }
                                    }
                                }?.let { it2 -> trackPathModelProvider.selectedCarData.add(it2) }
                                carId = model.id.toString()

                                MyPreference.setValueString("itemId", model.id.toString())
                                MyPreference.setValueString(PrefKey.CAR_URI, model.uri.toString())
                                try {
                                    trackPathModelProvider.carName = model.nm!!
                                    trackPathModelProvider.carSpeed =
                                        model.trip_curr_speed?.toInt()!!
                                } catch (e: Exception) {
                                    Log.e(TAG, "selectCar: CATCH ${e.message}")
                                }


                                binding.spSpinner.text = model.nm
                            }
                        }, requireContext())
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                }
            }
        }
    }

    override fun onResume() {
        viewModelGps3.isNewData.postValue("false")
        super.onResume()
        handleActionBarHidePlusIcon(R.string.tracks)
    }
}