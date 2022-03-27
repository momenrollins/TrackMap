package com.trackmap.gps.track.movingcar;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.trackmap.gps.preference.MyPreference;
import com.trackmap.gps.utils.DebugLog;

import java.util.Objects;

public class CarMoveAnim {

//    Ideal location request for car animation.

    public static int time;
    public static int stopFlag = 0;

    public static void startcarAnimation(final Marker carMarker, final GoogleMap googleMap, final LatLng startPosition,
                                         final LatLng endPosition, boolean is3D, boolean isCentered) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);

        Log.d("TAG", "updateCarLocation: hassan" + time);
        valueAnimator.setDuration(time);
        final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();
        valueAnimator.setInterpolator(new LinearInterpolator());
        try {
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {


                    if (stopFlag != 0) {
                        valueAnimator.setDuration(time);
                        float v = valueAnimator.getAnimatedFraction();
                        double lng = v * endPosition.longitude + (1 - v)
                                * startPosition.longitude;
                        double lat = v * endPosition.latitude + (1 - v)
                                * startPosition.latitude;

                        LatLng newPos = latLngInterpolator.interpolate(v, startPosition, endPosition);

/*                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    // Log.e("", "" + animatedFraction);
                    circle.setRadius(animatedFraction * 100);*/
                        carMarker.setPosition(newPos);
                        MyPreference.INSTANCE.setValueString("lat", newPos.latitude + "");
                        MyPreference.INSTANCE.setValueString("lng", newPos.longitude + "");
                        carMarker.setAnchor(0.5f, 0.5f);
                        float beer = (float) bearingBetweenLocations(startPosition, endPosition);

//                        rotateMarker(carMarker,beer);
                        carMarker.setRotation((float) bearingBetweenLocations(startPosition, endPosition));
                        if (isCentered)
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(13.64f)
                                    .build()));
                        if (is3D)
                            googleMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition
                                            (new CameraPosition.Builder()
                                                    .target(newPos)
                                                    .bearing((float) bearingBetweenLocations(startPosition, endPosition))
                                                    .zoom(googleMap.getCameraPosition().zoom)
                                                    .build()));
                    } else {
                        stopFlag = 1;
                        valueAnimator.removeAllUpdateListeners();
                    }

            /*    else
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition
                                    (new CameraPosition.Builder()
                                            .target(newPos)
                                            .zoom(googleMap.getCameraPosition().zoom)
                                            .build()));*/
                }

            });
        } catch (Exception e) {
            Log.d("TAG", "movingCabMarker: " + e.getMessage());
            DebugLog.INSTANCE.e(Objects.requireNonNull(e.getMessage()));
        }
        valueAnimator.start();
    }


    public static void liveCarAnimation(final Marker carMarker, final LatLng startPosition,
                                        final LatLng endPosition, double time) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);

        valueAnimator.setDuration((long) time);
        final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();
        valueAnimator.setInterpolator(new LinearInterpolator());
        try {
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {

                    float v = valueAnimator.getAnimatedFraction();
                   /* double lng = v * endPosition.longitude + (1 - v)
                            * startPosition.longitude;
                    double lat = v * endPosition.latitude + (1 - v)
                            * startPosition.latitude;*/

                    LatLng newPos = latLngInterpolator.interpolate(v, startPosition, endPosition);

/*                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    // Log.e("", "" + animatedFraction);
                    circle.setRadius(animatedFraction * 100);*/
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
//                        carMarker.setRotation(angle);

                    carMarker.setRotation((float) bearingBetweenLocations(startPosition, endPosition));

            /*    else
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition
                                    (new CameraPosition.Builder()
                                            .target(newPos)
                                            .zoom(googleMap.getCameraPosition().zoom)
                                            .build()));*/
                }

            });
        } catch (Exception e) {
            DebugLog.INSTANCE.e(Objects.requireNonNull(e.getMessage()));
        }
        valueAnimator.start();
    }


    private static double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;
        double dLon = (long2 - long1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    public interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static void rotateMarker(final Marker marker, final float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = time;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

    }
}
