package com.houseofdevelopment.gps.command.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommandResponseModel {

    @SerializedName("item")
    @Expose
    var items: ItemData? = null

    class ItemData{
        @SerializedName("cmds")
        @Expose
        val cmds: List<CMDS>? = null
    }

    class CMDS {
        @SerializedName("n")
        @Expose
         val n: String? = null

        @SerializedName("a")
        @Expose
         val a: Int? = null

        @SerializedName("t")
        @Expose
         val t: String? = null

        @SerializedName("c")
        @Expose
         val c: String? = null

        @SerializedName("p")
        @Expose
         val p: String? = null
    }
}