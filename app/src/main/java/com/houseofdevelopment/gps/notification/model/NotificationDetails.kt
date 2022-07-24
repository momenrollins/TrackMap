package com.houseofdevelopment.gps.notification.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.Constants

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
