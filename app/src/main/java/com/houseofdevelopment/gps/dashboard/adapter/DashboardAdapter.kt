package com.houseofdevelopment.gps.dashboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.houseofdevelopment.gps.R
import kotlinx.android.synthetic.main.dashboard_item.view.*

class DashboardAdapter(
    private var mContext: Context?,
    var itemListener:itemlistener,
    var dashTitle: ArrayList<String>,
    var dashNumbers: ArrayList<ArrayList<String>>,
    var dashImages: ArrayList<Int>,
    var dashColors: ArrayList<Int>
) : RecyclerView.Adapter<DashboardAdapter.viewholder>() {


    inner class viewholder(var binding: View) : RecyclerView.ViewHolder(binding.rootView) {
        fun bind(
            position: Int,
            mContext: Context?
        ) {
            binding.dash_card.setOnClickListener {
                itemListener.itemClick(position)
            }
            binding.dash_status.text = dashTitle[position]
            binding.dash_status.setTextColor(
                ContextCompat.getColor(
                    mContext!!,
                    dashColors[position]
                )
            )
            binding.dash_number.text = dashNumbers[position].size.toString()
            binding.dash_icon.setImageResource(dashImages[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.dashboard_item, parent, false)
        return viewholder(view)
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {

        holder.bind(position, mContext)
    }

    override fun getItemCount(): Int {
        return dashTitle.size
    }
    fun Refresh(){
        notifyDataSetChanged()
    }

    interface itemlistener{
        fun itemClick( position: Int)
    }
}