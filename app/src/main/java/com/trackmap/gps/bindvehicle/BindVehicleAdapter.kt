package com.trackmap.gps.bindvehicle

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.homemap.model.Item
import com.trackmap.gps.network.client.ApiClient
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BindVehicleAdapter(
    var originalCarListing: ArrayList<Item>,
    val checkForBiometrics: Boolean,
    val hasFaceBiometric: Boolean,
    var bindhandler: BindItemClick
) : RecyclerView.Adapter<BindVehicleAdapter.ViewHolder>(), Filterable {


    val _status = MutableLiveData<ApiStatus>()
    var unitCarList = ArrayList<Item?>()
    var client = ApiClient.getApiClient()
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private var carAddress: String? = ""
    val carAddressData = MutableLiveData<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_single_vehical_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = unitCarList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(unitCarList[position]!!, position)


    inner class ViewHolder(var binding: View) : RecyclerView.ViewHolder(binding.rootView) {
        @SuppressLint("SetTextI18n")
        fun bind(itemList: Item, position: Int) {
            with(itemView) {
                itemView.const_noti_comm_btn.visibility = View.GONE
                itemView.const_address.visibility = View.GONE
                itemView.img_googleMap.visibility = View.GONE

                if (itemList != null && itemList.trip_curr_speed.equals("0")) {
                    itemView.img_car_running.setImageResource(R.drawable.ic_speedometer)
                    itemView.car_distance.visibility = View.GONE
                } else {
                    itemView.img_car_running.setImageResource(R.drawable.ic_speed_on_big)
                    itemView.car_distance.visibility = View.VISIBLE
                }
                /*if (itemList != null && itemList.trip_state.equals("0")) {
                    itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)
                } else {
                    itemView.engineOnOff.setImageResource(R.drawable.ic_car_on)
                }*/
                if (itemList != null && itemList.trip_state?.trim()
                        ?.isNotEmpty() == true && itemList.trip_state?.toDouble()!! > 0.0
                ) {
                    itemView.engineOnOff.setImageResource(R.drawable.ic_car_on)
                } else {
                    itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)
                }
                if (itemList.isExpanded) {
                    itemView.const_bindUser.visibility = View.VISIBLE
                    itemView.img_topUpperArrow.rotation = 180f
                } else {
                    itemView.const_bindUser.visibility = View.GONE
                    itemView.img_topUpperArrow.rotation = 90f
                }
                if (checkForBiometrics) {
                    itemView.const_fingerPrint.visibility = View.VISIBLE
                } else {
                    binding.const_fingerPrint.visibility = View.GONE
                }
                if (hasFaceBiometric) {
                    binding.const_faceDetection.visibility = View.VISIBLE
                } else {
                    binding.const_faceDetection.visibility = View.GONE
                }
                if (itemList != null && itemList.isSelected)
                    itemView.const_cardetails.setBackgroundColor(
                        getColor(
                            context!!,
                            R.color.selected_color
                        )
                    )
                else
                    itemView.const_cardetails.setBackgroundColor(
                        getColor(
                            context!!,
                            R.color.color_white
                        )
                    )
                itemView.car_name.text = itemList?.nm
                Glide.with(context)
                    .load("http://www.avltracmap.com" + itemList?.uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_car)
                    .into(itemView.car_image)

                itemView.car_speed.text =
                    itemList!!.trip_curr_speed + " " + resources.getString(R.string.km_h)
                itemView.txt_min.text = Utils.formatDuration(
                    (Calendar.getInstance().timeInMillis / 1000) - (itemList.trip_m?.toLong()
                        ?: 0)
                )

                if (((Calendar.getInstance().timeInMillis / 1000) - (itemList.trip_m?.toLong()
                        ?: 0)) / 60 >= 60) {
                    itemView.car_name.setTextColor(getColor(context,R.color.color_red))

                } else {
                    itemView.car_name.setTextColor(getColor(context,R.color.colorBlack))

                }
            /*    itemView.car_name.text=(((Calendar.getInstance().timeInMillis / 1000) - (itemList.trip_m?.toLong()
                    ?: 0)) / 60 >= 60
                        ).toString()*/

                /*if (itemList.tripToT.toLong().minus(itemList.tripFromT!!.toLong()) > 0)
                    itemView.car_time_2.text = Utils.formatDurationFullValues(itemList.tripToT.toLong().minus(itemList.tripFromT!!.toLong()))
                else*/
                itemView.car_time_2.text = Utils.formatDurationFullValues(
                    (Calendar.getInstance().timeInMillis / 1000) - (itemList.tripFromT?.toLong()
                        ?: 0)
                )

                val timeInterval = itemList.tripToT?.toLong()?.let {
                    itemList.trip_m?.toLong()?.minus(it.minus(itemList.tripFromT!!.toLong()))
                }

                timeInterval?.let {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    val dd = Date(timeInterval * 1000L)
                    val date1 = sdf.format(dd)
                    val date2 = sdf.format(Date())

                    if (date1.equals(date2)) {
                        val sdDateTime = SimpleDateFormat("hh:mm a", Locale.US)
                        val time = sdDateTime.format(Date(timeInterval * 1000L))
                        itemView.car_time_1.text = time
                    } else {
                        val sdDate = SimpleDateFormat("dd MMM", Locale.US)
                        itemView.car_time_1.text = sdDate.format(dd).toString()
                    }
                }

                val dist = ((itemList.trip_distance?.toDouble() ?: 0.0) / 1000)
                val roundOff = Math.round(df.format(dist).toDouble())

                itemView.car_distance.text = "$roundOff km/h"

                if (itemList.car_address == null) {
                    callApiForCarAddress(itemList)
                }

                itemView.img_topUpperArrow.tag = position
                itemView.img_topUpperArrow.setOnClickListener {
                    unitCarList[it.tag as Int]!!.isExpanded =
                        !unitCarList[it.tag as Int]!!.isExpanded
                    notifyItemChanged(it.tag as Int)
                }
                itemView.const_code.tag = position
                itemView.const_code.setOnClickListener {
                    bindhandler.onBindOptionClick(
                        "Code",
                        it.tag as Int,
                        unitCarList[it.tag as Int]!!
                    )
                }
                itemView.const_fingerPrint.tag = position
                itemView.const_fingerPrint.setOnClickListener {
                    bindhandler.onBindOptionClick(
                        "Finger",
                        it.tag as Int,
                        unitCarList[it.tag as Int]!!
                    )
                }
                itemView.const_faceDetection.tag = position
                itemView.const_faceDetection.setOnClickListener {
                    bindhandler.onBindOptionClick(
                        "Face",
                        it.tag as Int,
                        unitCarList[it.tag as Int]!!
                    )
                }

                /*itemView.const_cardetails.setOnClickListener {
                    itemView.const_cardetails.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color))
                    bindhandler.bindItemClick(position)
                }*/
            }
        }

        private fun callApiForCarAddress(itemList: Item) {
            val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
            val jsonObj = JSONObject()
            val carAddressArray = JSONArray()

            val userId = MyPreference.getValueString(Constants.USER_ID, "").toString().toInt()

            coroutineScope.launch {
                try {
                    jsonObj.put("lon", itemList.pos?.x)
                    jsonObj.put("lat", itemList.pos?.y)
                    carAddressArray.put(jsonObj)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val body = HashMap<String, String>()
                body["coords"] = carAddressArray.toString()
                body["flags"] = "1255211008"
                body["uid"] = userId.toString()

                _status.value = ApiStatus.LOADING

                DebugLog.d("RequestBody car_address=$body")
                if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                    _status.value = ApiStatus.NOINTERNET
                } else {
                    try {
                        // this will run on a thread managed by Retrofit
                        val response = client.getAddressOfCar(body)
                        response.let {
                            carAddressData.postValue(response.body()?.get(0)!!.asString.toString())
                            carAddress = response.body()?.get(0).toString()

                            itemView.car_location.text = carAddress
                        }
                        _status.value = ApiStatus.DONE

                    } catch (error: Throwable) {
                        error.toString()
                        _status.value = ApiStatus.ERROR
                    }

                }
            }
        }
    }


    interface BindItemClick {
        fun onBindOptionClick(opt: String, position: Int, model: Item)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                var filterList = ArrayList<Item>()
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filterList = originalCarListing
                } else {
                    val filteredList = ArrayList<Item>()
                    for (document in originalCarListing) {
                        if (document?.nm?.lowercase()!!.trim()
                                .contains(charString.lowercase().trim())
                        ) {
                            filteredList.add(document)
                        }
                    }
                    filterList = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                unitCarList = filterResults.values as ArrayList<Item?>
                notifyDataSetChanged()
            }
        }
    }
}
