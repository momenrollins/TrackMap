package com.trackmap.gps.vehicallist.adapter

import android.annotation.SuppressLint
import android.util.Log
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
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.trackmap.gps.R
import com.trackmap.gps.addgroup.ui.GroupUnitListFragment
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.homemap.model.SensorItemModelGps3
import com.trackmap.gps.network.client.ApiClient
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.track.model.MessageRoot
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.model.ItemGps3
import com.trackmap.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.*
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.car_speed
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.engineOnOff
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.img_car_running
import kotlinx.android.synthetic.main.adapter_single_vehical_layout.view.img_topUpperArrow
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.awaitResponse
import java.lang.Exception
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SingleCarAdapterGps3(
    var clickHandler: NotificationCommandHandler,
    var value: ArrayList<ItemGps3>,
    var isGroupUnitList: Boolean
) :
    RecyclerView.Adapter<SingleCarAdapterGps3.ViewHolder>(), Filterable {
    private var filteredData = ArrayList<ItemGps3>()
    var viewmodel: VehiclesListViewModel? = null
    var carAddress: String = ""
    val carAddressData = MutableLiveData<String>()
    val _status = MutableLiveData<ApiStatus>()
    var client = ApiClient.getApiClient()
    var clientAddress = ApiClient.getApiClientAddress()
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
    val tz = TimeZone.getDefault()!!
    var groupUnitListFragment: GroupUnitListFragment = GroupUnitListFragment()
    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        filteredData.clear()
        filteredData.addAll(value)
    }

    fun addAll(mList: MutableList<ItemGps3>) {
        value = mList as ArrayList<ItemGps3>
    }

    fun addAllFiltered(mList: MutableList<ItemGps3>) {
        filteredData = mList as ArrayList<ItemGps3>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_single_vehical_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredData.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(filteredData[position], position)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(
            itemList: ItemGps3?,
            position: Int
        ) {
            with(itemView) {
                if (isGroupUnitList) {
                    itemView.img_delete_group.visibility = View.VISIBLE
                }
                itemView.const_noti_comm_btn.visibility = View.GONE
                itemView.const_address.visibility = View.GONE
                itemView.img_googleMap.visibility = View.GONE

                if (itemList != null && itemList.speed == "0") {
                    itemView.img_car_running.setImageResource(R.drawable.ic_speedometer)
                    itemView.car_distance.visibility = View.GONE
                } else {
                    itemView.img_car_running.setImageResource(R.drawable.ic_speed_on_big)
                    itemView.car_distance.visibility = View.VISIBLE
                    itemView.car_distance.isSelected = true
                    itemView.car_distance.text = itemList!!.dist.toString()
                }
                getSens(itemList?.imei, itemView)

                if (itemList.isExpanded) {
                    itemView.const_noti_comm_btn.visibility = View.VISIBLE
                    itemView.const_address.visibility = View.VISIBLE
                    itemView.img_googleMap.visibility = View.VISIBLE
                    itemView.img_topUpperArrow.rotation = 180f
                } else {
                    itemView.const_noti_comm_btn.visibility = View.GONE
                    itemView.const_address.visibility = View.GONE
                    itemView.img_googleMap.visibility = View.GONE
                    if (Locale.getDefault().displayLanguage.equals("العربية"))
                        itemView.img_topUpperArrow.rotation = 270f
                    else
                        itemView.img_topUpperArrow.rotation = 90f
                }
                if (itemList.isSelected)
                    itemView.const_cardetails.setBackgroundColor(
                        getColor(
                            context!!, R.color.selected_color
                        )
                    )
                else
                    itemView.const_cardetails.setBackgroundColor(
                        getColor(
                            context!!, R.color.color_white
                        )
                    )
                itemView.car_name.text = itemList.name

                Glide.with(context)
                    .load(R.drawable.default_car)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.default_car)
                    .into(itemView.car_image)

                itemView.car_speed.text =
                    itemList.speed + " " + resources.getString(R.string.km_h)
                val timeStamp: Long = getTimeStamp(itemList.dt_server)
                itemView.txt_min.text =
                    Utils.formatDuration(
                        (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                            timeStamp
                        )) / 1000
                    )
                if (((Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis / 1000) - ((timeStamp) + tz.getOffset(
                        timeStamp
                    )) / 1000) / 60 >= 60
                ) {
                    itemView.car_name.setTextColor(getColor(context, R.color.color_red))
                } else {
                    itemView.car_name.setTextColor(getColor(context, R.color.colorBlack))
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)

                val time: Long = if (itemList.speed.toDouble() > 0)
                    getTimeStamp(itemList.dt_last_move) + tz.getOffset(timeStamp)
                else
                    getTimeStamp(itemList.dt_last_stop) + tz.getOffset(timeStamp)
                val dd = Date(time)
                val date1 = sdf.format(dd)
                val date2 = sdf.format(Date())

                if (date1.equals(date2)) {
                    val sdDateTime = SimpleDateFormat("hh:mm a", Locale.US)
                    val time1 = sdDateTime.format(Date(time))
                    itemView.car_time_1.text = time1
                } else {
                    val sdDate = SimpleDateFormat("dd MMM", Locale.US)
                    itemView.car_time_1.text = sdDate.format(dd).toString()
                }
                itemView.car_time_2.text = Utils.formatDurationFullValues(
                    (Calendar.getInstance(TimeZone.getTimeZone(tz.id)).timeInMillis - time) / 1000
                )
                if (filteredData[position].oldAddress != "")
                    itemView.car_location.text = filteredData[position].oldAddress
                if (itemList.address == "") {
                    callApiForCarAddressGps3(itemList, position)
                }
                itemView.img_delete_group.setOnClickListener {
                    clickHandler.onRemoveClick(position)
                }
                itemView.btn_notification.tag = position
                itemView.btn_notification.setOnClickListener {
                    clickHandler.onNotificationClick(filteredData[it.tag as Int].imei)
                }
                itemView.btn_commands.tag = position
                itemView.btn_commands.setOnClickListener {
                    clickHandler.onCommandClick(filteredData[it.tag as Int].imei)
                }
                itemView.img_topUpperArrow.tag = position
                itemView.img_topUpperArrow.setOnClickListener {
                    filteredData[it.tag as Int].isExpanded =
                        !filteredData[it.tag as Int].isExpanded
                    notifyItemChanged(it.tag as Int)
                }
                itemView.const_cardetails.tag = position
                itemView.const_cardetails.setOnClickListener {
                    filteredData[it.tag as Int].isSelected =
                        !filteredData[it.tag as Int].isSelected
                    clickHandler.onItemClick(
                        filteredData[it.tag as Int], filteredData[it.tag as Int].isSelected
                    )
                    notifyItemChanged(it.tag as Int)
                }
                itemView.img_googleMap.tag = position
                itemView.img_googleMap.setOnClickListener {
                    clickHandler.onMapIconClick(it.tag as Int)
                }
            }
        }

        private fun callApiForCarAddressGps3(itemList: ItemGps3, index: Int) {
            coroutineScope.launch {
                val call = clientAddress.getCarLocation(
                    "user",
                    MyPreference.getValueString(
                        PrefKey.ACCESS_TOKEN, ""
                    )!!,
                    "GET_ADDRESS,${itemList.lat},${itemList.lng}"
                ).awaitResponse()
                _status.value = ApiStatus.LOADING
                if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                    _status.value = ApiStatus.NOINTERNET
                } else {
                    try {
                        if (call.isSuccessful) {
                            carAddress = call.body()!!.toString()
                            carAddressData.postValue(carAddress)
                            itemView.car_location.text = carAddress
                            filteredData[index].oldAddress =
                                carAddress
                        }
                        _status.value = ApiStatus.DONE
                    } catch (error: Throwable) {
                        Log.d(TAG, "callApiForCarAddressGps3: ${error.message}")
                        _status.value = ApiStatus.ERROR
                    }
                }
            }
        }
    }

    private fun distanceBetween(point1: LatLng?, point2: LatLng?): Double? {
        return if (point1 == null || point2 == null) {
            null
        } else SphericalUtil.computeDistanceBetween(point1, point2)
    }

    private fun isMoreThan5Mins(listHItemp: ArrayList<MessageRoot>): Boolean {
        var totalTime: Long = 0

        for (c in listHItemp.indices) {
            if (c > 0)
                totalTime += getTimeStamp(listHItemp[c].date) / 1000 - getTimeStamp(
                    listHItemp[c - 1].date
                ) / 1000
        }
        if (totalTime < 295) {
            return false
        }
        return true
    }

    private fun getSens(imei: String?, itemView: View) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_sensors",
                "imei" to imei,
                "sensor_id" to "*",
                "api_key" to MyPreference.getValueString(
                    PrefKey.ACCESS_TOKEN, ""
                )!!
            )
            val params = JSONObject(map as Map<*, *>).toString()
            var isOn = false
            _status.value = ApiStatus.LOADING
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    val call = client.getSensors(params).awaitResponse()
                    if (call.isSuccessful) {
                        val sensorRoot: SensorItemModelGps3 = call.body()!!
                        if (sensorRoot.status) {
                            for (item in sensorRoot.data!!) {
                                if (item.name!!.lowercase() == "حالة المحرك")
                                    isOn =
                                        item.value != null && item.value.toString().toDouble() > 0
                            }
                            if (isOn) {
                                itemView.engineOnOff.setImageResource(R.drawable.ic_car_on)
                            } else
                                itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)
                        } else {
                            itemView.engineOnOff.setImageResource(R.drawable.ic_car_off)
                        }
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeStamp(dateString: String?): Long {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString!!)
        return date!!.time
    }

    interface NotificationCommandHandler {
        fun onNotificationClick(toString: String)
        fun onCommandClick(carId: String)
        fun onItemClick(model: ItemGps3, selected: Boolean)
        fun onMapIconClick(position: Int)
        fun onRemoveClick(position: Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charS = charSequence.toString()
                val filterList: java.util.ArrayList<ItemGps3> = if (charS.isEmpty()) {
                    value
                } else {
                    val filteredList = ArrayList<ItemGps3>()
                    for (document in value)
                        if (document.name.lowercase().trim().contains(charS.lowercase().trim()))
                            filteredList.add(document)
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                try {
                    Log.d(TAG, "publishResults: filteredDataSize ${filteredData.size}")
                    filteredData = filterResults.values as java.util.ArrayList<ItemGps3>
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.d(TAG, "publishResults: ${e.message}")
                }

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    fun getFilteredListGps3(): ArrayList<ItemGps3> {
        return filteredData
    }

    companion object {
        private const val TAG = "SingleCarAdapterGps3"
    }
}