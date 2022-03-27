package com.trackmap.gps.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ResponseListData<T> : ResponseWrapper<T>(){

    @SerializedName("data")
    @Expose
    var data: ArrayList<T>? = null

    override fun toString(): String {
        return "ResponseWrapper{" +
                "data=" + data .toString()
    }

}
