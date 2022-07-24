package com.houseofdevelopment.gps.utils;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.houseofdevelopment.gps.R;

public class BindingAdapters {

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(view.getContext())
                    .load("http://gps.hod.sa" + imageUrl)
                    .placeholder(R.drawable.default_car)
                    .into(view);
        } else {
            Glide.with(view.getContext())
                    .load(R.drawable.default_car)
                    .into(view);
        }
    }


    @BindingAdapter("loadIntImage")
    public static void loadIntImage(ImageView view, Integer imageUrl) {
        if (imageUrl != null) {
            Glide.with(view.getContext())
                    .load(imageUrl)
                    .placeholder(imageUrl)
                    .into(view);
        }
    }
}
