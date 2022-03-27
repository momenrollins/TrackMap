package com.trackmap.gps.report.model

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
import com.trackmap.gps.DataValues.showSpeed
import com.trackmap.gps.R
import com.trackmap.gps.databinding.DialogCarListingBinding
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.model.ItemGps3
import kotlinx.android.synthetic.main.template_layout_row_right.view.*
import java.util.*

class CarListingDialogGps3(private val listener: CarClickListener, private val mContext: Context) :
    DialogFragment() {
    private var vehicleListGps3 = ArrayList<ItemGps3>()

    interface CarClickListener {
        fun selectCarGps3(model: ItemGps3)
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
        if (Locale.getDefault().displayLanguage.equals("العربية")) binding.backArrow.rotation = 180F

        binding.backArrow.setOnClickListener {
            dialog?.dismiss()
        }
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()

        return binding.root
    }

    private var serverData: String = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Utils.getCarListingDataGps3(mContext).items != null && Utils.getCarListingDataGps3(
                mContext
            ).items.size > 0
        ) {
            vehicleListGps3.clear()
            vehicleListGps3.addAll(Utils.getCarListingDataGps3(mContext).items)
        }
        binding.rvCarListing.adapter =
            CarAdapterGps3(vehicleListGps3, listener, dialog)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                val filterList = ArrayList<ItemGps3>()
                for (name in vehicleListGps3) {
                    if (name.name.lowercase().trim()
                            .contains(editable.toString().lowercase())
                    ) {
                        filterList.add(name)
                    }
                }
                (binding.rvCarListing.adapter as CarAdapterGps3).updateList(filterList)
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

class CarAdapterGps3(
    private var mapList: ArrayList<ItemGps3>,
    private var listenerGps3: CarListingDialogGps3.CarClickListener,
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
        mapList[position].let { holder.bind(it) }
        holder.itemView.mainData.tag = position
        holder.itemView.mainData.setOnClickListener {
            dialog!!.dismiss()
            listenerGps3.selectCarGps3(mapList[it.tag as Int])
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<ItemGps3>) {
        mapList = filterList
        notifyDataSetChanged()
    }

    private val TAG = "CarListingDialogGps3"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(value: ItemGps3) {
            try {
                itemView.textView.text = value.name


                if (value.speed.toInt() > 0 && showSpeed) {
                    itemView.moving.visibility = View.VISIBLE
                    itemView.moving.text = "${value.speed} km/h "
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