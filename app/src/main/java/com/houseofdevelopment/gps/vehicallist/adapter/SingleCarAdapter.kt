package com.houseofdevelopment.gps.vehicallist.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.network.client.ApiClient
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.utils.Constants
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.viewmodel.VehiclesListViewModel
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

class SingleCarAdapter(
    var clickHandler: NotificationCommandHandler,
    var value: java.util.ArrayList<Item>,
    var isGroupUnitList: Boolean
) :
    RecyclerView.Adapter<SingleCarAdapter.ViewHolder>(), Filterable {
    private var filteredData = java.util.ArrayList<Item>()
    var viewmodel: VehiclesListViewModel? = null
    private var carAddress: String? = ""
    val carAddressData = MutableLiveData<String>()
    val status = MutableLiveData<ApiStatus>()
    var client = ApiClient.getApiClient()
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))

    init {
        filteredData.clear()
        filteredData.addAll(value)
    }

    fun addAllFiltered(mList: MutableList<Item>) {
        filteredData = mList as ArrayList<Item>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_single_vehical_layout, parent, false)
        return ViewHolder(view)
    }

    private val TAG = "SingleCarAdapter"
    override fun getItemCount(): Int = filteredData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(filteredData[position], position)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            itemList: Item?,
            position: Int
        ) {
            with(itemView) {
                if (isGroupUnitList) {
                    itemView.img_delete_group.visibility = View.VISIBLE
                }
                itemView.img_delete_group.setOnClickListener {
                    clickHandler.onRemoveClick(position)
                }
                itemView.const_noti_comm_btn.visibility = View.GONE
                itemView.const_address.visibility = View.GONE
                itemView.img_googleMap.visibility = View.GONE

                Log.d(TAG, "bind: trip_curr_speed ${itemList!!.id} ${itemList!!.nm} ${itemList.pos?.y} ${itemList.pos?.x}")

                if (itemList != null && itemList.trip_m != null && itemList.pos != null && itemList.sens != null) {
                    if (itemList != null && itemList.trip_curr_speed != null && !itemList.trip_curr_speed.equals(
                            "0"
                        )
                    ) {
                        itemView.img_car_running.setImageResource(R.drawable.ic_speed_on_big)
                        itemView.car_distance.visibility = View.VISIBLE
                    } else {
                        itemView.img_car_running.setImageResource(R.drawable.ic_speedometer)
                        itemView.car_distance.visibility = View.GONE
                    }
                    if (itemList?.trip_state != null && itemList.trip_state?.trim()!!
                            .isNotEmpty() && itemList.trip_state?.toDouble()!! > 0.0
                    ) {
                        itemView.engineOnOff.setImageResource(R.drawable.ic_car_on)
                    } else {
                        itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)
                    }
                    if (itemList?.isExpanded != null && itemList.isExpanded) {
                        itemView.const_noti_comm_btn.visibility = View.VISIBLE
                        itemView.const_address.visibility = View.VISIBLE
                        itemView.img_googleMap.visibility = View.VISIBLE
                        itemView.img_topUpperArrow.rotation = 180f
                    } else {
                        itemView.const_noti_comm_btn.visibility = View.GONE
                        itemView.const_address.visibility = View.GONE
                        itemView.img_googleMap.visibility = View.GONE
                        if (Locale.getDefault().displayLanguage
                                .equals("العربية")
                        ) itemView.img_topUpperArrow.rotation = 270f
                        else
                            itemView.img_topUpperArrow.rotation = 90f
                    }
                    if (itemList != null && itemList.isSelected)
                        itemView.const_cardetails.setBackgroundColor(
                            getColor(context!!, R.color.selected_color)
                        )
                    else
                        itemView.const_cardetails.setBackgroundColor(
                            getColor(context!!, R.color.color_white)
                        )
                    itemView.car_name.text = itemList?.nm
                    Log.d("TAG", "bind: trip_m ${itemList.trip_m}")

                    Glide.with(context)
                        .load("http://gps.hod.sa" + itemList?.uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.default_car)
                        .into(itemView.car_image)

                    Log.d(TAG, "bind: URI ${"http://gps.hod.sa" + itemList?.uri}")
                    itemView.car_speed.text =
                        itemList?.trip_curr_speed + " " + resources.getString(R.string.km_h)
                    itemView.txt_min.text = Utils.formatDuration(
                        (Calendar.getInstance().timeInMillis / 1000) - (itemList.trip_m?.toLong()
                            ?: 0)
                    )

                    if (((Calendar.getInstance().timeInMillis / 1000) - (itemList.trip_m?.toLong()
                            ?: 0)) / 60 >= 60
                    ) {
                        itemView.car_name.setTextColor(getColor(context, R.color.dash_red))
                    } else {
                        itemView.car_name.setTextColor(getColor(context, R.color.colorBlack))
                    }

                    itemView.car_time_2.text = Utils.formatDurationFullValues(
                        (Calendar.getInstance().timeInMillis / 1000) - (itemList.tripFromT?.toLong()
                            ?: 0)
                    )

                    val timeInterval = itemList.tripToT?.toLong()?.let {
                        itemList.trip_m?.toLong()?.minus(it.minus(itemList.tripFromT!!.toLong()))
                    }

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    if (timeInterval != null) {
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

                    itemView.btn_notification.tag = position
                    itemView.btn_notification.setOnClickListener {
                        clickHandler.onNotificationClick(filteredData[it.tag as Int].nm.toString())
                    }
                    itemView.btn_commands.tag = position
                    itemView.btn_commands.setOnClickListener {
                        Utils.showProgressBar(context)
                        clickHandler.onCommandClick(filteredData[it.tag as Int].id.toString())
                    }
                    itemView.img_topUpperArrow.tag = position
                    itemView.img_topUpperArrow.setOnClickListener {
                        filteredData[it.tag as Int].isExpanded =
                            !filteredData[it.tag as Int].isExpanded
                        notifyItemChanged(it.tag as Int)
                    }

                    itemView.const_cardetails.tag = position
                    itemView.const_cardetails.setOnClickListener {
                        filteredData[it.tag as Int]!!.isSelected =
                            (!filteredData[it.tag as Int]!!.isSelected)
                        clickHandler.onItemClick(
                            filteredData[it.tag as Int],
                            filteredData[it.tag as Int]!!.isSelected
                        )
                        notifyItemChanged(it.tag as Int)
                    }
                    itemView.img_googleMap.tag = position
                    itemView.img_googleMap.setOnClickListener {
                        clickHandler.onMapIconClick(it.tag as Int)
                    }
                } else {
                    itemView.car_time_1.text = "--:-- AM"
                    itemView.car_time_2.text = "-- h -- min"
                    itemView.car_speed.text = "-- km/h"
                    itemView.car_distance.visibility = View.GONE

                    Glide.with(context)
                        .load(R.drawable.default_car)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.default_car)
                        .into(itemView.car_image)
                    itemView.const_cardetails.setBackgroundColor(
                        getColor(
                            context!!, R.color.color_white
                        )
                    )
                    itemView.img_car_running.setImageResource(R.drawable.ic_speedometer)
                    itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)

                    itemView.const_noti_comm_btn.visibility = View.GONE
                    itemView.const_address.visibility = View.GONE
                    itemView.img_googleMap.visibility = View.GONE
                    itemView.txt_min.text = "---"
                    if (Locale.getDefault().displayLanguage
                            .equals("العربية")
                    ) itemView.img_topUpperArrow.rotation = 270f
                    else
                        itemView.img_topUpperArrow.rotation = 90f
                    itemView.car_name.text = " ${itemList.nm} "
                    itemView.car_name.setTextColor(getColor(context, R.color.dash_red))
                    itemView.img_topUpperArrow.isEnabled=false;
                    itemView.const_cardetails.setOnClickListener {
                        Toast.makeText(
                            context,
                            AppBase.instance.getString(R.string.device_is_not_connected),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }


        }

        private fun callApiForCarAddress(itemList: Item) {
            val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
            val jsonObj_ = JSONObject()
            val _carAddressArray = JSONArray()

            val userId = MyPreference.getValueString(Constants.USER_ID, "").toString().toInt()

            coroutineScope.launch {
                try {
                    jsonObj_.put("lon", itemList.pos?.x)
                    jsonObj_.put("lat", itemList.pos?.y)
                    _carAddressArray.put(jsonObj_)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val body = HashMap<String, String>()
                body["coords"] = _carAddressArray.toString()
                body["flags"] = "1255211008"
                body["uid"] = userId.toString()

                status.value = ApiStatus.LOADING

                DebugLog.d("RequestBody car_address=$body")
                if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                    status.value = ApiStatus.NOINTERNET
                } else {
                    try {
                        // this will run on a thread managed by Retrofit
                        val response = client.getAddressOfCar(body)
                        response.let {
                            carAddressData.postValue(response.body()?.get(0)!!.asString.toString())
                            carAddress = response.body()?.get(0).toString()

                            itemView.car_location.text =
                                carAddress.toString().removePrefix("\"").removeSuffix("\"")
                        }
                        status.value = ApiStatus.DONE

                    } catch (error: Throwable) {
                        error.toString()
                        Log.d(TAG, "callApiForCarAddress: CATCH ${error.message}")
                        status.value = ApiStatus.DONE
                    }
                }
            }
        }
    }

    interface NotificationCommandHandler {
        fun onNotificationClick(toString: String)
        fun onCommandClick(carId: String)
        fun onItemClick(model: Item, selected: Boolean)
        fun onMapIconClick(position: Int)
        fun onRemoveClick(position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filterList: ArrayList<Item>
                if (charString.isEmpty()) {
                    filterList = value
                } else {
                    val filteredList = ArrayList<Item>()
                    for (document in value) {
                        if (document.nm?.lowercase(Locale.getDefault())?.trim()
                                ?.contains(charString.lowercase(Locale.getDefault()).trim())!!
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
                filteredData = filterResults.values as java.util.ArrayList<Item>
                notifyDataSetChanged()
            }
        }
    }

    fun getFilteredList(): ArrayList<Item> {
        return filteredData
    }
}