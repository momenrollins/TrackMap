package com.houseofdevelopment.gps.vehicallist.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SubmitContactRequest {
    @SerializedName("message")
    @Expose
    var message: String? = ""
    @SerializedName("mobile_no")
    @Expose
    var mobile_no: String? = ""
    @SerializedName("name")
    @Expose
    var name: String? = ""
    @SerializedName("subject")
    @Expose
    var subject: String? = ""
    @SerializedName("contact_details")
    @Expose
    var contact_details: String? = ""
}