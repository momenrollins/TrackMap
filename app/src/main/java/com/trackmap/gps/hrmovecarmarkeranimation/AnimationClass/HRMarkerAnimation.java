package com.trackmap.gps.hrmovecarmarkeranimation.AnimationClass;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.trackmap.gps.hrmovecarmarkeranimation.CallBacks.UpdateLocationCallBack;
import com.trackmap.gps.hrmovecarmarkeranimation.Utils.Utilities;


public class HRMarkerAnimation {

    private final UpdateLocationCallBack updateLocation;
    private ValueAnimator valueAnimator;
    private final GoogleMap googleMap;
    private final long animationDuration;

    public HRMarkerAnimation(GoogleMap googleMap,long duration , UpdateLocationCallBack updateLocation) {
        this.updateLocation = updateLocation;
        this.googleMap =googleMap;
        this.animationDuration=duration;
    }

    public void animateMarker(final LatLng endPosition, final LatLng startPosition, final Marker marker) {
        if (marker != null) {
//            final LatLng startPosition = marker.getPosition();
//            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            if (valueAnimator != null)
                valueAnimator.end();

            final Utilities.LatLngInterpolator latLngInterpolator = new Utilities.LatLngInterpolator.LinearFixed();
            valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(animationDuration); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());

            valueAnimator.addUpdateListener(animation -> {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);

                   // marker.setRotation(computeRotation(v, marker.getRotation(), (float)Utilities.bearingBetweenLocations(startPosition, newPosition)));
                   // marker.setRotation(Utilities.computeRotation(v, marker.getRotation(), (float)Utilities.bearingBetweenLocations(startPosition, newPosition)));

                   // markerView.findViewById(R.id.rotatedView).setRotation(Utilities.computeRotation(v, marker.getRotation(), (float)Utilities.bearingBetweenLocations(startPosition, newPosition)));
                    marker.setAnchor(0.5f, 0.5f);
//                   marker.setRotation((float)Utilities.bearingBetweenLocations(startPosition, endPosition));

                  //  marker.setFlat(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    // handle exception here
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    updateLocation.onUpdatedLocation(startPosition);
                }
            });
            valueAnimator.start();

            //when marker goes out from screen it automatically move into center
            if (false){
                if (!Utilities.isMarkerVisible(googleMap,startPosition)){
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(new CameraPosition.Builder()
                                    .target(endPosition)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build()));
                }else {
                    try {
                        googleMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(new CameraPosition.Builder()
                                        .target(endPosition)
                                        .tilt(0)
                                        .zoom(googleMap.getCameraPosition().zoom)
                                        .build()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                googleMap.setPadding(0, 0, 0, 20);
            }

        }
    }

}
