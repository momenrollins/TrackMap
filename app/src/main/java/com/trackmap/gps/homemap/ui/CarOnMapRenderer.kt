package com.trackmap.gps.homemap.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import java.util.*


class CarOnMapRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<ClusterRender>
) : DefaultClusterRenderer<ClusterRender>(context, map, clusterManager) {
    private val mIconGenerator: IconGenerator
    private val mClusterIconGenerator: IconGenerator
    private val mImageView: AppCompatImageView

    //    private val mClusterImageView: AppCompatImageView
    private val mDimensionHeight: Int
    private val mDimensionWidth: Int
    private val TRANSPARENT_DRAWABLE = ColorDrawable(Color.TRANSPARENT)

    val parent: ViewGroup? = null

    init {

        val multiProfile =
            LayoutInflater.from(context).inflate(
                R.layout.layout_clusteritemmulti,
                parent,
                false
            )
        mIconGenerator = IconGenerator(context)
        mClusterIconGenerator = IconGenerator(context)
        mImageView = AppCompatImageView(context)
        mDimensionHeight = context.resources.getDimension(R.dimen.markar_height).toInt()
        mDimensionWidth = context.resources.getDimension(R.dimen.markar_width).toInt()
        mImageView.layoutParams = ViewGroup.LayoutParams(mDimensionWidth, mDimensionHeight)
        mIconGenerator.setContentView(mImageView)
        mClusterIconGenerator.setContentView(multiProfile)


    }

    override fun onClusterItemRendered(clusterItem: ClusterRender, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.position = clusterItem.position
        marker.title = ""
        marker.tag = clusterItem.mCarModel.id
        marker.hideInfoWindow()
        marker.setAnchor(0.5f, 0.5f)
        marker.isFlat = true
        createCustomMarker(context, clusterItem, marker)

    }

    override fun onClusterUpdated(cluster: Cluster<ClusterRender?>, marker: Marker) {
        // Same implementation as onBeforeClusterRendered() (to update cached markers)

    }

    override fun onClusterItemUpdated(clusterItem: ClusterRender, marker: Marker) {
        marker.position = clusterItem.position
        marker.tag = clusterItem.mCarModel.id
        marker.hideInfoWindow()
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<ClusterRender>,
        markerOptions: MarkerOptions
    ) {

        mClusterIconGenerator.setBackground(TRANSPARENT_DRAWABLE)
        val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))

    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterRender>): Boolean {
        return cluster.size > 1
                && MyPreference.getValueBoolean(PrefKey.IS_GROUP_UNITS, true)
    }


    private fun getMarkerBitmapFromView(
        imageView: AppCompatImageView,
        bitmap: Bitmap,
        markerView: View
    ): Bitmap? {

        imageView.setImageBitmap(bitmap)
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        markerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            markerView.measuredWidth, markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = markerView.background
        drawable?.draw(canvas)
        markerView.draw(canvas)
        return returnedBitmap
    }

    fun updateMarker(marker: Marker, clusterItem: ClusterRender) {
        // marker.position = clusterItem.position
        marker.title = ""
        marker.tag = clusterItem.mCarModel.id
        marker.hideInfoWindow()
        createCustomMarker(context, clusterItem, marker)
    }


    @SuppressLint("ResourceType")
    private fun createCustomMarker(
        context: Context,
        clusterItem: ClusterRender,
        marker: Marker
    ) {
        if (clusterItem.markerView == null) {
            val inflater: LayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val markerView = inflater.inflate(R.layout.layout_custom_mapmarker, null)
            clusterItem.markerView = markerView
        }

        val textView: TextView =
            clusterItem.markerView?.findViewById<View>(R.id.text_label) as TextView
        val imageView: AppCompatImageView =
            clusterItem.markerView!!.findViewById<View>(R.id.image) as AppCompatImageView
        val imageArrow: AppCompatImageView =
            clusterItem.markerView!!.findViewById<View>(R.id.image_arrow) as AppCompatImageView
        val rotatedView: ConstraintLayout =
            clusterItem.markerView!!.findViewById<View>(R.id.rotatedView) as ConstraintLayout

        textView.text = clusterItem.mCarModel.nm
        if (((Calendar.getInstance().timeInMillis / 1000) - (clusterItem.mCarModel.trip_m?.toLong()
                ?: 0)) / 60 >= 60
        ) {
            textView.setTextColor(context.resources.getColor(R.color.color_red))

        } else {
            textView.setTextColor(context.resources.getColor(R.color.color_dark_blue))

        }
        if (MyPreference.getValueBoolean(PrefKey.IS_ADD_BG, false)) {

            if (((Calendar.getInstance().timeInMillis / 1000) - (clusterItem.mCarModel.trip_m?.toLong()
                    ?: 0)) / 60 >= 60
            ) {
                textView.setBackgroundResource(
                    R.drawable.card_bg_rounded_red
                )
            } else {
                textView.setBackgroundResource(
                    R.drawable.card_bg_rounded
                )
            }
            textView.setPadding(10, 8, 10, 8)

        } else {

            textView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        }
        imageView.setImageResource(R.drawable.default_car)
        rotatedView.rotation = clusterItem.mCarModel.trip_course?.toFloat() ?: 0f
        val speed: Int = clusterItem.mCarModel.trip_curr_speed?.toInt() ?: 0
        if (speed > 0) {
            imageArrow.visibility = View.VISIBLE
        } else {
            imageArrow.visibility = View.GONE
        }
        try {
            marker.setIcon(
                BitmapDescriptorFactory.fromBitmap(
                    getMarkerBitmapFromView(
                        imageView, BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.default_car
                        ), clusterItem.markerView!!
                    )!!
                )
            )
        } catch (e: Exception) {

        }
        Glide.with(AppBase.instance).asBitmap()
            .placeholder(R.drawable.default_car)
            .error(R.drawable.default_car)
            .load("http://gps.tawasolmap.com" + clusterItem.snippet).fitCenter()
            .into(object : SimpleTarget<Bitmap?>(80, 80) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    try {
                        marker.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    imageView,
                                    resource,
                                    clusterItem.markerView!!
                                )!!
                            )
                        )
                    } catch (e: Exception) {

                    }

                }
            })
    }
}

