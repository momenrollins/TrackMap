package com.houseofdevelopment.gps.usersettings.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey

class SignoutRequest {

    @SerializedName("device_token")
    @Expose
    var deviceToken: String? = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
}