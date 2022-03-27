package com.trackmap.gps.report.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.report.model.GeneratedReportModelGps3
import com.trackmap.gps.report.viewmodel.ReportViewModel
import com.trackmap.gps.utils.CommonAlertDialog
import kotlinx.android.synthetic.main.adapter_group_vehical_layout.view.*
import java.util.*

class ReportAdapter(

    var context: Context,
    var handlerAlertDialog: CommonAlertDialog.DeleteCarHandler?,
    var generatedReportModelGps3s: ArrayList<GeneratedReportModelGps3>,
    var itemClick: OnItemClickListener
) :
    RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
    var mListener: OnItemClickListener? = null
    var reportViewModel: ReportViewModel? = null

    interface OnItemClickListener {
        fun onItemClick(
            position: Int,
            generatedReportModelGps3s: ArrayList<GeneratedReportModelGps3>?
        )
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    inner class ViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var repNameTV: TextView
        var repDateTV: TextView
        var repImg: ImageView
        var deleteImg: ImageView

        init {
            repNameTV = itemView.findViewById(R.id.car_name)
            repDateTV = itemView.findViewById(R.id.car_lastUpdate)
            repImg = itemView.findViewById(R.id.car_image)
            deleteImg = itemView.findViewById(R.id.img_delete)
            itemView.setOnClickListener { v: View? ->
                itemClick.onItemClick(
                    position,
                    generatedReportModelGps3s
                )

//                if (listener != null) {
//                    val position = adapterPosition
//                    if (position != RecyclerView.NO_POSITION) listener.onItemClick(
//                        position,
//                        generatedReportModelGps3s
//                    )
//                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.adapter_group_vehical_layout, parent, false)
        if (Locale.getDefault().displayLanguage == "العربية") view.imgtopUpperArrow.setRotation(270f)
        return ViewHolder(view, mListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.repNameTV.text = generatedReportModelGps3s[position].name
        holder.repDateTV.text = generatedReportModelGps3s[position].dt_report
        if (generatedReportModelGps3s[position].format == "pdf") holder.repImg.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24) else holder.repImg.setImageResource(
            R.drawable.ic_baseline_html_24
        )
        reportViewModel = ReportViewModel()
        holder.deleteImg.setOnClickListener { view: View? ->
            CommonAlertDialog.showYesNoAlerter(
                context!!,
                handlerAlertDialog!!,
                generatedReportModelGps3s[position].report_id.toInt(),
                position,
                context.getString(R.string.delete_report),
                context.getString(R.string.are_you_sure_n_you_want_to_delete_this_report)
            )

//            reportViewModel!!.callApiForDeleteReportGps3(
//                Objects.requireNonNull(
//                    generatedReportModelGps3s[position].report_id
//                )
//            )
//            generatedReportModelGps3s.removeAt(position)
//            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return generatedReportModelGps3s.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(filterList: ArrayList<GeneratedReportModelGps3>) {
        generatedReportModelGps3s = filterList
        notifyDataSetChanged()
    }
}