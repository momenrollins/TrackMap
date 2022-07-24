package com.houseofdevelopment.gps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


open class
ResponseData<T> : ResponseWrapper<T>(){

    @SerializedName("data")
    @Expose
    var data: T? = null

    override fun toString(): String {
        return "ResponseWrapper{" + "data=" + data.toString()
    }

}
