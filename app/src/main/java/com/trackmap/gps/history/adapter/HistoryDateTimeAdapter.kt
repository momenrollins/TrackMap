package com.trackmap.gps.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.R
import com.trackmap.gps.databinding.AdapterCarHistoryDatetimeBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryDateTimeAdapter(
    var context: Context,
    private var finalList: ArrayList<Date>,
    var handler: DateItemClickHandler
) : RecyclerView.Adapter<HistoryDateTimeAdapter.ViewHolder>() {


    var row_index = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AdapterCarHistoryDatetimeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_car_history_datetime, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = finalList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(finalList[position], position)

    inner class ViewHolder(var binding: AdapterCarHistoryDatetimeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
        fun bind(value: Date, position: Int) {
            when (row_index) {
                position -> {
                    binding.cardDate.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.color_blue
                        )
                    )
                    binding.cardDate.radius = 15f
                    binding.txtDate.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.color_white
                        )
                    )
                }
                else -> {
                    binding.cardDate.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.color_white
                        )
                    )
                    binding.cardDate.radius = 15f
                    binding.txtDate.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorBlack
                        )
                    )
                }
            }
            val formatDate = SimpleDateFormat("dd MMM")
            binding.txtDate.text = formatDate.format(value)
            binding.cardDate.setOnClickListener {
                row_index = position
                notifyDataSetChanged()
                handler.onDateItemClickListener(value, position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun firstIndexClick() {
        row_index = finalList.size - 1
        handler.onDateItemClickListener(finalList[finalList.size - 1], finalList.size - 1)
        notifyDataSetChanged()
    }

    interface DateItemClickHandler {
        fun onDateItemClickListener(value: Date, position: Int)
    }
}