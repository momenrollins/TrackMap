package com.houseofdevelopment.gps.login.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignInRequest {

   /* @SerializedName("password")
    @Expose
    var password: String? = "brainvire"*/

    /*@SerializedName("role")
    var role: String? = "ADMIN"

    @SerializedName("grant_type")
    @Expose
    var grant_type: String? = "password"*/

    @SerializedName("email_username")
    @Expose
//    var email_username: String? = "brainvire1"
    var email_username: String? = null

    @SerializedName("device_type")
    @Expose
    var device_type: String? = "android"

    @SerializedName("device_token")
    @Expose
    var device_token: String? = null
    @SerializedName("domain_name")
    @Expose
    var domain_name: String? = null

}