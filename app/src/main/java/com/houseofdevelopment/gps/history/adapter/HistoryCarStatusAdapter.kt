package com.houseofdevelopment.gps.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterCarHistoryCarstatusBinding
import com.houseofdevelopment.gps.history.model.TripDetails
import com.houseofdevelopment.gps.track.viewmodel.TrackViewModel
import com.houseofdevelopment.gps.utils.Utils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class HistoryCarStatusAdapter(
    var context: Context,
    var handler: HistoryTripListener
    /*var tripDetailList: MutableList<TripDetails.FirstObj>*/
) :
    RecyclerView.Adapter<HistoryCarStatusAdapter.ViewHolder>() {

    private var tripDetailList: MutableList<TripDetails.FirstObj>? = null
    private lateinit var viewmodel: TrackViewModel

    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
//    var rawIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: AdapterCarHistoryCarstatusBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_car_history_carstatus, parent, false
        )
        return ViewHolder(binding)
    }

//    override fun getItemCount(): Int =  tripDetailList?.size!!

    override fun getItemCount(): Int {
        return if (tripDetailList == null) 0 else tripDetailList?.size!!
    }

    fun clearSelectedData() {
        tripDetailList?.map { firstObj: TripDetails.FirstObj -> firstObj.isSelected = false }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(tripDetailList?.get(position)!!, position)

    inner class ViewHolder(var binding: AdapterCarHistoryCarstatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bind(
            tripList: TripDetails.FirstObj,
            position: Int
        ) {

            try {
                binding.txtTime.text =
                    Utils.getDateFromMillis(tripList.fromList.from_t.toLong() * 1000, "hh:mm a")
                if (tripList.toList.to_t != null && tripList.fromList.from_t != null)
                    binding.txtMin.text =
                        Utils.formatDurationFullValues((tripList.toList.to_t - tripList.fromList.from_t).toLong())
                else  binding.txtMin.text="--"
                /*   if (binding.txtMin.text.equals("1H"))
                   {

                   }*/
                if (tripList.tripEventType.equals("p")) {
                    if (tripList.isSelected) {
                        binding.imgHistoryCar.setImageResource(R.drawable.parking_select)
                    } else {
                        binding.imgHistoryCar.setImageResource(R.drawable.parking_unselect)
                    }

                    binding.txtKm.visibility = View.GONE
                    binding.txtSpeedometer.visibility = View.GONE
                    binding.layout.setOnClickListener {
                        clearSelectedData()
                        tripList.isSelected = true
                        handler.onParkImageClicked(position)
                        notifyDataSetChanged()
                    }

                } else if (tripList.tripEventType.equals("trip")) {
                    if (tripList.isSelected != null && tripList.isSelected) {
                        binding.imgHistoryCar.setImageResource(R.drawable.ic_car_select)
                    } else {
                        binding.imgHistoryCar.setImageResource(R.drawable.ic_history_car)
                    }

                    tripList.fromList.from_t
//                tripList.toList.to_t
                    val distance = tripList.history_distance.toDouble() / 1000
                    binding.txtKm.text = df.format(distance) + " km"
                    binding.txtSpeedometer.text = tripList.history_max_speed.toString() + " km/h"

                    binding.txtKm.visibility = View.VISIBLE
                    binding.txtSpeedometer.visibility = View.VISIBLE

                    binding.layout.setOnClickListener {
                        Log.d(TAG, "bind:tiiiii ${tripList.fromList.from_t} ")

                        clearSelectedData()
                        tripList.isSelected = true
//                    Log.d(TAG, "bind:history ${tripList.history_track.length} ")
                        if (tripList.history_track != null && tripList.history_track.isNotEmpty()) handler.onCarImageClicked(
                            position,
                            tripList.fromList.from_t,
                            tripList.toList.to_t,
                            false
                        )
                        else handler.onCarImageClicked(
                            position,
                            tripList.fromList.from_t,
                            tripList.toList.to_t,
                            true
                        )

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
            } catch (e: Exception) {

                Log.d(TAG, "bind:catch history ${e.message} ")
            }


        }
    }

    fun calculateTripDistance(): Double {
        var distValue = 0.0
        for (i in tripDetailList?.indices!!) {
            if (tripDetailList!![i].tripEventType.equals("trip")) {
                distValue += tripDetailList!![i].history_distance
            }
        }

        return distValue / 1000
    }

    fun calculateTripTime(): Long {
        var totalDiff = 0L
        for (i in tripDetailList?.indices!!) {
            if (tripDetailList!![i].tripEventType.equals("trip")) {
                val timeDiff = (tripDetailList!![i].toList.to_t.toLong()
                    .minus(tripDetailList!![i].fromList.from_t.toLong()))
                totalDiff += timeDiff
            }
        }
        return totalDiff
    }

    fun calculateParkingHours(): Long {
        val totalHours: Long
        var totalDiff = 0L
        try {
            for (i in tripDetailList?.indices!!) {
                if (tripDetailList!![i].tripEventType.equals("p")) {
                    val timeDiff = (tripDetailList!![i].toList.to_t.toLong()
                        .minus(tripDetailList!![i].fromList.from_t.toLong()))
                    totalDiff += timeDiff
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "calculateParkingHours: CATCH ${e.message}")
        }

        totalHours = totalDiff
        return totalHours
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        tripDetailList?.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: MutableList<TripDetails.FirstObj>) {
        tripDetailList = list
        notifyDataSetChanged()
    }


    fun getWholeList(): MutableList<TripDetails.FirstObj>? {
        Log.d(TAG, "getWholeList: ListSize ${tripDetailList?.size}")
        return tripDetailList
    }

    private val TAG = "HistoryCarStatusAdapter"

    interface HistoryTripListener {
        fun onCarImageClicked(position: Int, fromT: Double, toT: Double, isEmpity: Boolean)
        fun onParkImageClicked(position: Int)
    }


}