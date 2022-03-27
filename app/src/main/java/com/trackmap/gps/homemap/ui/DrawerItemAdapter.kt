package com.trackmap.gps.homemap.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.databinding.DrawerItemLayoutBinding


class DrawerItemAdapter(val activity: MainActivity, var handler: DrawerItemClick) :
    RecyclerView.Adapter<DrawerItemAdapter.ViewHolder>() {

    private val drawer_images = arrayListOf(
        R.drawable.ic_drawer_map,
        R.drawable.ic_drawer_track,
        R.drawable.ic_drawer_notification,
//        R.drawable.ic_drawer_poi,
        R.drawable.ic_drawer_geo_zones,
        R.drawable.dashboard,
//        R.drawable.ic_drawer_quality_driving,
        R.drawable.ic_drawer_user_setting,
        R.drawable.ic_drawer_reports,
        R.drawable.ic_drawer_bind,
        R.drawable.ic_drawer_contactus,
        R.drawable.ic_drawer_aboutus,
        R.drawable.ic_drawer_privacy_policy
    )

    private val drawer_title = arrayListOf(
        R.string.drawer_map,
        R.string.drawer_tracks,
        R.string.drawer_notification,
//        R.string.drawer_poi,
        R.string.drawer_geo_zones,
        R.string.drawer_dashboard,
        R.string.drawer_user_setting,
        R.string.drawer_reports,
        R.string.drawer_bind,
        R.string.drawer_contact_us,
        R.string.drawer_about_us,
        R.string.drawer_privacy_policy
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val inflater = LayoutInflater.from(parent.context)
//            val view = inflater.inflate(R.layout.drawer_item_layout, parent, false)
        val binding: DrawerItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.drawer_item_layout, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = drawer_images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(drawer_images[position], drawer_title[position], position)


    inner class ViewHolder(var binding: DrawerItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            image: Int,
            title: Int,
            position: Int
        ) {
            if (position % 2 == 0) {

                binding.constDrawerItemCard.setCardBackgroundColor(activity.resources.getColor(R.color.drawer_blue))

            } else {
                binding.constDrawerItemCard.setCardBackgroundColor(activity.resources.getColor(R.color.light_blue2))
            }
            binding.drawerImage = image
            binding.drawerTitle = activity.getString(title)
            binding.executePendingBindings()
            binding.constDrawerItemCard.setOnClickListener {
                handler.drawerItemClick(position)
            }
        }

    }

    interface DrawerItemClick {
        fun drawerItemClick(position: Int)
    }
}