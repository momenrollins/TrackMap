package com.trackmap.gps.homemap.model

import java.io.Serializable

data class CheckedUnitModelGPS3(var carId : String,
                                var ignitionFromLong : String,
                                var ignitionFromLat : String,
                                var ignitionToLong : String,
                                var ignitionToLat : String,

                                var tripFromLong : String,
                                var tripFromLat : String,
                                var tripFromT: String,
                                var tripToLong : String,
                                var tripToLat : String,
                                var tripToT: String,

                                var trip_m : String,
                                var trip_f : String,
                                var trip_state : String,
                                var trip_max_speed : String,
                                var trip_curr_speed : String,
                                var trip_avg_speed : String,
                                var trip_distance : String,
                                var trip_odometer : String,
                                var trip_course : String,
                                var trip_altitude : String) : Serializable