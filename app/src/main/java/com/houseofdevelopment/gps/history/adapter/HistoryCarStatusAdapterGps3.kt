package com.houseofdevelopment.gps.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.houseofdevelopment.gps.DataValues.tz
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterCarHistoryCarstatusBinding
import com.houseofdevelopment.gps.history.model.RouteRootGps3
import com.houseofdevelopment.gps.history.model.TripDetailsGps3
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Utils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


class HistoryCarStatusAdapterGps3(
    var context: Context,
    var handler: HistoryTripItemListener
) :
    RecyclerView.Adapter<HistoryCarStatusAdapterGps3.ViewHolder>() {
    var tripDetailList: ArrayList<TripDetailsGps3>? = null
    var routeRoot: RouteRootGps3? = null

    //    var itemSelectedModel: ItemSelectedModel = ItemSelectedModel()
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterCarHistoryCarstatusBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_car_history_carstatus, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (tripDetailList == null) 0 else tripDetailList?.size!!
    }

    fun clearSelectedData() {
        tripDetailList?.map { firstObj: TripDetailsGps3 -> firstObj.isSelected = false }
//        MyPreference.setValueBoolean(PrefKey.isSelected, true)
        /*itemSelectedModel.isSelected = false
        MyPreference.setValueBoolean(PrefKey.isSelected, true)*/
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tripDetailList?.get(position)!!, position)
    }

    inner class ViewHolder(var binding: AdapterCarHistoryCarstatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n", "SimpleDateFormat", "NotifyDataSetChanged")
        fun bind(
            tripDetails: TripDetailsGps3,
            position: Int
        ) {
            val mDate = Utils.getDateFromMillis(
                ((getTimeStamp(tripDetails.dt_start)) + tz.getOffset(getTimeStamp(tripDetails.dt_start))),
                "yyyy-MM-dd"
            )
            binding.txtTime.text = Utils.getDateFromMillis(
                ((getTimeStamp(tripDetails.dt_start)) + tz.getOffset(
                    getTimeStamp(tripDetails.dt_start)
                )), "hh:mm a"
            )
            binding.txtMin.text = Utils.formatDurationFullValues(tripDetails.duration.toLong())

            if (position == 0) {
                binding.txtTime.text = "12:00 AM"
                if (tripDetailList!!.size > 2) {
                    Log.d(
                        "TAG", "bind: date ${
                            Utils.getDateFromMillis(
                                ((getTimeStamp(tripDetailList!![1].dt_start))), "HH:mm"
                            )
                        }"
                    )
                    val firstTime = (getTimeStamp(tripDetailList!![1].dt_start) + tz.getOffset(
                        (getTimeStamp(tripDetailList!![1].dt_start))
                    )) / 1000 - (getTimeStamp("$mDate 00:00:00") / 1000)
                    binding.txtMin.text = Utils.formatDurationFullValues(firstTime)
                }
            }
            val currentDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
            Log.d("TAG", "bind: mDate $mDate - currentDate $currentDate")
            if (position == tripDetailList!!.lastIndex)
                if (mDate != currentDate) {
                    val lastTime = (getTimeStamp(
                        "$mDate 23:59:59"
                    ) / 1000) - (getTimeStamp(tripDetails.dt_start) + tz.getOffset(
                        (getTimeStamp(tripDetails.dt_start))
                    )) / 1000
                    binding.txtMin.text = Utils.formatDurationFullValues(lastTime)
                } else {
                    val lastTime = (getTimeStamp(
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time)
                    ) / 1000) - (getTimeStamp(tripDetails.dt_start) + tz.getOffset(
                        (getTimeStamp(tripDetails.dt_start))
                    )) / 1000
                    binding.txtMin.text = Utils.formatDurationFullValues(lastTime)
                }
            if (tripDetails.type == "stop") {
                if (tripDetails.isSelected != null && tripDetails.isSelected) {
                    binding.imgHistoryCar.setImageResource(R.drawable.parking_select)
                } else {
                    binding.imgHistoryCar.setImageResource(R.drawable.parking_unselect)
                }
                binding.txtKm.visibility = View.GONE
                binding.txtSpeedometer.visibility = View.GONE
                binding.layout.setOnClickListener {
                    clearSelectedData()
                    tripDetails.isSelected = true
                    handler.onCarImageClicked(routeRoot!!, tripDetails, false)
                    MyPreference.setValueBoolean(PrefKey.isSelected, false)

                    notifyDataSetChanged()
                }
            } else {
                if (tripDetails.isSelected != null && tripDetails.isSelected) {
                    binding.imgHistoryCar.setImageResource(R.drawable.ic_car_select)
                } else {
                    binding.imgHistoryCar.setImageResource(R.drawable.ic_history_car)
                }
                var maxSpeed = 0
                var distance = 0.0
                var tripSpeed: Int
                binding.txtKm.text = "${df.format(tripDetails.route_length)} km"
                binding.txtSpeedometer.text = "${tripDetails.top_speed} km/h"
                binding.txtKm.visibility = View.VISIBLE
                binding.txtSpeedometer.visibility = View.VISIBLE
                binding.layout.setOnClickListener {
                    clearSelectedData()
                    tripDetails.isSelected = true
                    handler.onCarImageClicked(routeRoot!!, tripDetails, true)
                    MyPreference.setValueBoolean(PrefKey.isSelected, false)
                    notifyDataSetChanged()
                }
            }

            if (adapterPosition == 0) {
                binding.outerLine1.visibility = View.GONE
                binding.outerLine2.visibility = View.VISIBLE
            } else if (adapterPosition == itemCount - 1) {
                binding.outerLine1.visibility = View.VISIBLE
                binding.outerLine2.visibility = View.GONE
            }
        }
    }

    fun distanceBetween(point1: LatLng?, point2: LatLng?): Double? {
        return if (point1 == null || point2 == null) {
            null
        } else SphericalUtil.computeDistanceBetween(point1, point2)
    }

    /*@SuppressLint("SimpleDateFormat")
    private fun changeDateFormat(date: String): CharSequence? {
        val originalFormat: DateFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val targetFormat: DateFormat = SimpleDateFormat("h 'h' m 'min'", Locale.US)
        val newDate: Date = originalFormat.parse(date)!!
        return targetFormat.format(newDate)
    }*/

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        tripDetailList?.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: ArrayList<TripDetailsGps3>, routeRoot: RouteRootGps3) {
        clearSelectedData()
        tripDetailList = list
        this.routeRoot = routeRoot
        notifyDataSetChanged()
    }

    fun getWholeList(): MutableList<TripDetailsGps3>? {
        return tripDetailList
    }

    interface HistoryTripItemListener {
        fun onCarImageClicked(
            routeRoot: RouteRootGps3,
            tripList: TripDetailsGps3,
            carClick: Boolean
        )
    }
}
