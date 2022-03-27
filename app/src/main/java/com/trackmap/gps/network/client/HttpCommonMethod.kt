package com.trackmap.gps.network.client

import com.google.gson.JsonObject
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.utils.Validation

object HttpCommonMethod {

    /**
     * check Error Message
     */
    fun getErrorMessage(error: JsonObject?): String {
        var value = ""
        if (error != null) {
            val obj = error.asJsonObject //since you know it's a JsonObject
            try {
                if (obj != null) {

                    //will return members of your object
                    val entries = obj.entrySet()

                    for ((key, value1) in entries) {
                        println(key)
                        value = if (Validation.isNotNull(value)) {
                            value + "\n" + Utils.removeArrayBrace(value1.asJsonArray.toString())
                        } else {
                            Utils.removeArrayBrace(value1.asJsonArray.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                DebugLog.print(e)
                value = ""
            }

        }
        return value
    }

    /**
     * get Authorized token
     */
    fun getToken(): String {
        return Constants.BEARER + MyPreference.getValueString(
            PrefKey.ACCESS_TOKEN_BRAINVIRE,
            ""
        ).toString()
    }
}
