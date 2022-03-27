package com.trackmap.gps.vehicallist.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.databinding.AdapterCarHistoryCarstatusBinding

class HistoryCarStatusAdapter : RecyclerView.Adapter<HistoryCarStatusAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterCarHistoryCarstatusBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_car_history_carstatus, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 15

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind()


    inner class ViewHolder(var binding: AdapterCarHistoryCarstatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

            val text = "<b>20</b> h <b>52</b> min"
            val text2 = "<b>20</b> km"

            binding.txtMin.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE)
            binding.txtKm.setText(Html.fromHtml(text2), TextView.BufferType.SPANNABLE)

            binding.txtSpeedometer.visibility = View.GONE
            binding.txtEngineOnOff.visibility = View.GONE

            if (adapterPosition == 0) {
                binding.outerLine1.visibility = View.GONE
                binding.outerLine2.visibility = View.VISIBLE
                binding.txtTime.visibility = View.INVISIBLE
            } else if (adapterPosition == itemCount - 1) {
                binding.outerLine1.visibility = View.VISIBLE
                binding.outerLine2.visibility = View.GONE
            }


        }

    }
}