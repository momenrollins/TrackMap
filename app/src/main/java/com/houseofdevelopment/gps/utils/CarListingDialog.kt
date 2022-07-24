package com.houseofdevelopment.gps.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.DataValues.showSpeed
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.DialogCarListingBinding
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import kotlinx.android.synthetic.main.template_layout_row_right.view.*
import java.util.*

class CarListingDialog(private val listener: CarClickListener, private val mContext: Context) :
    DialogFragment() {
    private var vehicalList = ArrayList<Item>()
    private var vehicalListGps3 = ArrayList<ItemGps3>()

    interface CarClickListener {
        fun selectCar(model: Item)
    }

    lateinit var binding: DialogCarListingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    /**
     * Create views for dialog
     * @param inflater LayoutInflater
     * @param container ViewGroup?
     * @param savedInstanceState Bundle?
     * @return View?
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_car_listing, container, false
        )
        if (Locale.getDefault().getDisplayLanguage()
                .equals("العربية")
        ) binding.backArrow!!.rotation = 180F

        binding.backArrow.setOnClickListener {
            dialog?.dismiss()
        }
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()

        return binding.root
    }

    private var serverData: String = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!serverData.contains("s3")) {
            if (Utils.getCarListingData(mContext).items != null && Utils.getCarListingData(mContext).items.size > 0) {
                vehicalList.clear()
                vehicalList.addAll(Utils.getCarListingData(mContext).items)
            }
            binding.rvCarListing.adapter =
                CarAdapter(vehicalList, listener, dialog)
        } else {
            if (Utils.getCarListingDataGps3(mContext).items != null && Utils.getCarListingDataGps3(
                    mContext
                ).items.size > 0
            ) {
                vehicalListGps3.clear()
                vehicalListGps3.addAll(Utils.getCarListingDataGps3(mContext).items)
            }
            binding.rvCarListing.adapter =
                CarAdapterGps3(vehicalListGps3, listener, dialog)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (!serverData.contains("s3")) {
                    val filterList = ArrayList<Item>()
                    for (name in vehicalList!!) {
                        if (name!!.nm?.lowercase()?.trim()
                                ?.contains(editable.toString().lowercase().toString())!!
                        ) {
                            filterList.add(name)
                        }
                    }
                    (binding.rvCarListing.adapter as CarAdapter).updateList(filterList)

                } else {
                    val filterList = ArrayList<ItemGps3>()
                    for (name in vehicalListGps3!!) {
                        if (name!!.name?.lowercase()?.trim()
                                ?.contains(editable.toString().lowercase().toString())!!
                        ) {
                            filterList.add(name)
                        }
                    }
                    (binding.rvCarListing.adapter as CarAdapterGps3).updateList(filterList)
                }
            }
        })
    }


    /**
     * Create a dialog
     * @param savedInstanceState Bundle?
     * @return Dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onBackPressed() { //do your stuff
                dismiss()
            }
        }
    }
}

class CarAdapter(
    private var mapList: ArrayList<Item>,
    private var listener: CarListingDialog.CarClickListener,
    private var dialog: Dialog?
) :
    RecyclerView.Adapter<CarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.template_layout_row_right, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mapList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mapList[position]?.let {
            holder.bind(
                it
            )
        }
        holder.itemView.mainData.tag = position
        holder.itemView.mainData.setOnClickListener {
            dialog!!.dismiss()
            listener.selectCar(mapList[it.tag as Int])
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<Item>) {
        mapList = filterList
        notifyDataSetChanged()
    }

    private val TAG = "CarListingDialog"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(value: Item) {
            try {
                itemView.textView.text = value.nm
                if (value.trip_curr_speed!!.toInt() > 0 && showSpeed) {
                    itemView.moving.visibility = View.VISIBLE
                    itemView.moving.text = "${value.trip_curr_speed} km/h "
                } else {
                    itemView.moving.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "bind: CATCH ${e.message}")
            }
            itemView.selectValues.visibility = View.GONE
        }
    }
}

class CarAdapterGps3(
    private var mapList: ArrayList<ItemGps3>,
    private var listenerGps3: CarListingDialog.CarClickListener,
    private var dialog: Dialog?
) :
    RecyclerView.Adapter<CarAdapterGps3.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.template_layout_row_right, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mapList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mapList[position].name?.let {
            holder.bind(
                it
            )
        }
        holder.itemView.mainData.setTag(position)
        holder.itemView.mainData.setOnClickListener {
            dialog!!.dismiss()
        }

    }


    fun updateList(filterList: ArrayList<ItemGps3>) {
        mapList = filterList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(value: String) {

            itemView.textView.text = value

            itemView.selectValues.visibility = View.GONE

        }
    }


}