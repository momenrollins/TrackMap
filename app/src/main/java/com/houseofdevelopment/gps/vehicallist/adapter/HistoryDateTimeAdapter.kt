package com.houseofdevelopment.gps.vehicallist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.AdapterCarHistoryDatetimeBinding

class HistoryDateTimeAdapter(var context : Context) : RecyclerView.Adapter<HistoryDateTimeAdapter.ViewHolder>() {


    val numbers = listOf("1 Oct", "2 Oct", "3 Oct", "4 Oct",
        "5 Oct", "6 Oct", "7 Oct", "8 Oct",
        "9 Oct", "10 Oct", "11 Oct", "12 Oct",
        "13 Oct", "14 Oct", "15 Oct", "16 Oct")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterCarHistoryDatetimeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
                R.layout.adapter_car_history_datetime, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 15

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind()


    inner class ViewHolder(var binding: AdapterCarHistoryDatetimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

            binding.txtDate.text = numbers[adapterPosition]
            binding.txtDate.setOnClickListener {
                binding.cardDate.setCardBackgroundColor(ContextCompat.getColor(context, R.color.color_green2))
                binding.cardDate.radius = 10f
                binding.txtDate.setTextColor(ContextCompat.getColor(context, R.color.color_white))
            }
        }

    }
}