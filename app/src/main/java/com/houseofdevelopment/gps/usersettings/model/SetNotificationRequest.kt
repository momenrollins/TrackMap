package com.houseofdevelopment.gps.usersettings.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey

class SetNotificationRequest {

    @SerializedName("is_sent_notification")
    @Expose
    var isSentNotification: Boolean = false

    @SerializedName("device_token")
    @Expose
    var device_token: String? = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")

    constructor(isSentNotification: Boolean, device_token: String?) {
        this.isSentNotification = isSentNotification
        this.device_token = device_token
    }
}