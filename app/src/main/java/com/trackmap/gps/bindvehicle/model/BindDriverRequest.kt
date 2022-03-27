package com.trackmap.gps.bindvehicle.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BindDriverRequest {

    @SerializedName("driver_id")
    @Expose
    var driverId: String? = null

    @SerializedName("unit_id")
    @Expose
    var unitId: String? = null

    @SerializedName("resource_id")
    @Expose
    var resourceId: String? = null

    @SerializedName("user_uuid")
    @Expose
    var userUuid: String? = null

    @SerializedName("interface")
    @Expose
    var interfaceMode: String? = null

    @SerializedName("bind_type")
    @Expose
    var bind_type: String? = null

    @SerializedName("passcode")
    @Expose
    var passcode: String? = null
}