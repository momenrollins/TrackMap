package com.trackmap.gps.usersettings.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey

class SignoutRequest {

    @SerializedName("device_token")
    @Expose
    var deviceToken: String? = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")
}