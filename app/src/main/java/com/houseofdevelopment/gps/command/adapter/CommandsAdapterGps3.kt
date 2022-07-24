package com.houseofdevelopment.gps.command.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.command.model.CommandDataGps3
import com.houseofdevelopment.gps.databinding.RowItemCommdsBinding
import java.util.*

class CommandsAdapterGps3 (private val cmds: List<CommandDataGps3>?, private val listener : CommandClickInterface) : RecyclerView.Adapter<CommandsAdapterGps3.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: RowItemCommdsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.row_item_commds, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = cmds?.size!!

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (Locale.getDefault().getDisplayLanguage().equals("العربية")){
            holder.binding.commandName.setTextDirection(View.TEXT_DIRECTION_RTL)
            holder.binding.imgTopUpperArrow.rotation= 270F

        }else {
            holder.binding.commandName.setTextDirection(View.TEXT_DIRECTION_LTR);
            holder.binding.imgTopUpperArrow.rotation= 90F
        }
        holder.bind(cmds!![position].name)
        holder.binding.constCommand1.tag = position
        holder.binding.constCommand1.setOnClickListener {
            listener.commandClick(cmds[it.tag as Int])
        }
    }


    inner class ViewHolder(var binding: RowItemCommdsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(n: String?) {
            binding.data = n
        }

    }

    interface CommandClickInterface {
        fun commandClick(model : CommandDataGps3)
    }
}
