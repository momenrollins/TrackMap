package com.houseofdevelopment.gps.usersettings.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.utils.Constants

class GetNotificationStatusRequest {

    @SerializedName("username")
    @Expose
    var username: String? = MyPreference.getValueString(Constants.USER_NAME, "")
}