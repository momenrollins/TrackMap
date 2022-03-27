package com.trackmap.gps

import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import java.util.*

object DataValues {
    val tz: TimeZone = TimeZone.getDefault()
    var apiKey = MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!
    var username = MyPreference.getValueString(PrefKey.USERNAME, "")!!
    var serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "")!!
    var isFromGroup = false
    var showSpeed = false
}