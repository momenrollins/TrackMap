package com.trackmap.gps.bindvehicle.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey

class AddDriverRequest {

    @SerializedName("full_name")
    @Expose
    var fullName: String? = null

    @SerializedName("user_type")
    @Expose
    var userType: String? = null

    @SerializedName("job_title")
    @Expose
    var jobTitle: String? = null

    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String? = null

    @SerializedName("company_id")
    @Expose
    var companyId: String? = null

    @SerializedName("id_number")
    @Expose
    var idNumber: String? = null

    @SerializedName("code")
    @Expose
    var code: String? = null

    @SerializedName("verify_code")
    @Expose
    var verifyCode: String? = null

    @SerializedName("bio_method")
    @Expose
    var bioMethod: String? = null

    @SerializedName("interface")
    @Expose
    var interfaces: String? = null

    @SerializedName("owner_id")
    @Expose
    var ownerId: String? = null

    @SerializedName("user_uuid")
    @Expose
    var user_uuid: String? = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")

}