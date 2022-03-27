package com.trackmap.gps.usersettings.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.utils.Constants

class GetNotificationStatusRequest {

    @SerializedName("username")
    @Expose
    var username: String? = MyPreference.getValueString(Constants.USER_NAME, "")
}