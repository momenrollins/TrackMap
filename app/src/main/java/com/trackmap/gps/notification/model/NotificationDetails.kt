package com.trackmap.gps.notification.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Constants

class NotificationDetails {

    @SerializedName("unit_name")
    @Expose
    var unit: String? = ""

    @SerializedName("start")
    @Expose
    var start: String? = ""

    @SerializedName("username")
    @Expose
    var username: String? = MyPreference.getValueString(Constants.USER_NAME, "")
    @SerializedName("device_token")
    @Expose
    var device_token: String? = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
}
