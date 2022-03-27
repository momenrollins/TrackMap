package com.trackmap.gps.preference


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import java.io.*

@SuppressLint("NewApi")
object MyPreference {

    private lateinit var prefs: SharedPreferences
    var filePath: String = ""
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PrefKey.PREFERENCE_NAME, Context.MODE_PRIVATE)
        filePath = context.getFilesDir().getParent()
            .toString() + "/shared_prefs/utils.xml"
    }

    fun removeKeys(firstString: String) {
        val editor = prefs.edit()
        for (key in prefs.all.keys) {
            if (key.startsWith(firstString)) {
                editor.remove(key)
            }
        }
        editor.commit()
    }

    fun getValueString(
        key: String,
        defaultValue: String
    ): String? {
        return prefs.getString(key, defaultValue)
    }

    fun RemoveItem(key: String) {
        val prefsPrivateEditor = prefs.edit()
        prefsPrivateEditor.remove(key)
        prefsPrivateEditor.apply()

    }

    fun setValueString(key: String, value: String) {
        val prefsPrivateEditor = prefs.edit()
        prefsPrivateEditor.putString(key, value)
        prefsPrivateEditor.apply()

    }

    fun getValueBoolean(
        key: String,
        defaultValue: Boolean
    ): Boolean {
        var result = defaultValue
        if (prefs.contains(key)) {
            result = prefs.getBoolean(key, defaultValue)
        }
        return result
    }

    fun setValueBoolean(key: String, value: Boolean) {
        val prefsPrivateEditor = prefs.edit()
        prefsPrivateEditor.putBoolean(key, value)
        prefsPrivateEditor.apply()

    }


    fun getValueInt(
        key: String,
        defaultValue: Int
    ): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun setValueInt(key: String, value: Int) {

        val prefsPrivateEditor = prefs.edit()
        prefsPrivateEditor.putInt(key, value)
        prefsPrivateEditor.apply()
    }

    fun clearAllData() {
        setValueBoolean(PrefKey.ISLOGIN, false)
        setValueBoolean(PrefKey.IS_SIGNIN, false)
        setValueBoolean(PrefKey.IS_CEO_DATA, false)
        setValueString(PrefKey.SELECTED_CAR_LISTING, "")
        setValueString(PrefKey.OWNER_ID, "")
        setValueString(Constants.E_ID, "")
        setValueString(PrefKey.ACCESS_TOKEN, "")
        removeKeys(PrefKey.FCM_TOKEN)
        removeKeys(PrefKey.NOTIFY)
//        setValueString(, "")
        setValueBoolean(PrefKey.IS_TRAFFIC, false)
        setValueBoolean(PrefKey.IS_GEO_ZONES, false)
        Constants.isFirstTimeApiCall = true
    }

    fun clear(context: Context){
        prefs = context.getSharedPreferences(PrefKey.PREFERENCE_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    fun setBeanValue(key: String, `object`: Any) {

        val prefsPrivateEditor = prefs.edit()

        val arrayOutputStream = ByteArrayOutputStream()

        val objectOutput: ObjectOutputStream
        try {
            objectOutput = ObjectOutputStream(arrayOutputStream)
            objectOutput.writeObject(`object`)
            val data = arrayOutputStream.toByteArray()
            objectOutput.close()
            arrayOutputStream.close()

            val out = ByteArrayOutputStream()
            val b64 = Base64OutputStream(out, Base64.DEFAULT)
            b64.write(data)
            b64.close()
            out.close()

            prefsPrivateEditor.putString(key, String(out.toByteArray()))

            prefsPrivateEditor.apply()
        } catch (e: IOException) {
            DebugLog.print(e)
        }

    }

    fun getBeanValue(key: String): Any? {
        val bytes = prefs.getString(key, "{}")!!.toByteArray()
        if (bytes.isEmpty()) {
            return null
        }
        val byteArray = ByteArrayInputStream(bytes)
        val base64InputStream = Base64InputStream(byteArray, Base64.DEFAULT)
        var `in`: ObjectInputStream? = null
        var myObject = Any()
        try {
            `in` = ObjectInputStream(base64InputStream)
            try {
                myObject = `in`.readObject()
            } catch (e: ClassNotFoundException) {
                DebugLog.print(e)
            }

        } catch (e: StreamCorruptedException) {
            DebugLog.print(e)
        } catch (e: IOException) {
            DebugLog.print(e)
        }

        if (`in` != null) {
            try {
                `in`.close()
            } catch (e: IOException) {
                DebugLog.print(e)
            }

        }

        return myObject
    }

}
