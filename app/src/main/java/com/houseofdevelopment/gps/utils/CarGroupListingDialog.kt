package com.houseofdevelopment.gps.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.DialogCarListingBinding
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import kotlinx.android.synthetic.main.template_layout_row_right.view.*
import java.util.*

class CarGroupListingDialog(private var vehicalList : MutableList<GroupListDataModel.Item>, private val listener: CarGroupClickListener, private val mContext: Context) :
    DialogFragment() {
    interface CarGroupClickListener {
        fun selectCar(model: GroupListDataModel.Item)
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
        if (Locale.getDefault().getDisplayLanguage().equals("العربية")) binding.backArrow!!.rotation=180F

        binding.backArrow.setOnClickListener {
            dialog?.dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

            binding.rvCarListing.adapter =
                    CarGroupAdapter(vehicalList, listener, dialog)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                val filterList = ArrayList<GroupListDataModel.Item>()
                for (name in vehicalList!!) {
                    if (name!!.nm?.toLowerCase()?.trim()?.contains(editable.toString().toLowerCase().toString())!!) {
                        filterList.add(name)
                    }
                }

                (binding.rvCarListing.adapter as CarGroupAdapter).updateList(filterList)
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






class CarGroupAdapter(
    private var mapList: MutableList<GroupListDataModel.Item>,
    private var listener: CarGroupListingDialog.CarGroupClickListener,
    private var dialog: Dialog?
) :
    RecyclerView.Adapter<CarGroupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.template_layout_row_right, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mapList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mapList.get(position).nm?.let {
            holder.bind(
                    it
        )
        }
        holder.itemView.mainData.setTag(position)
        holder.itemView.mainData.setOnClickListener {
            dialog!!.dismiss()
            listener.selectCar(mapList[it.tag as Int])
        }

    }


    fun updateList(filterList: ArrayList<GroupListDataModel.Item>) {
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