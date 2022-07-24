package com.houseofdevelopment.gps.network.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.houseofdevelopment.gps.BuildConfig
import com.houseofdevelopment.gps.network.exeception.NoConnectionException
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private val gson = GsonBuilder().serializeNulls().create()

    /**
     * Custom Interceptor to handle exception
     */
    private val responseCodeInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response: Response?
        try {
            response = chain.proceed(request)
            if (response.code == 401) {
                return@Interceptor generateCustomResponse(
                    401,
                    "",
                    chain.request()
                )!!

            } else if (response.code == 500) {
                return@Interceptor generateCustomResponse(
                    500,
                    "",
                    chain.request()
                )!!
            }
        } catch (e: Exception) {
            Utils.hideProgressBar()
            DebugLog.print(e)
            Utils.hideProgressBar()
            return@Interceptor generateCustomResponse(
                1007,
                "",
                chain.request()
            )!!
        }

        response
    }

    /**
     * Generate Retrofit Client
     */
    private fun getRetrofit(): Retrofit {

        val builder = Retrofit.Builder()
        val url: String? = MyPreference.getValueString(
            PrefKey.SELECTED_SERVER_DATA,
            BuildConfig.BaseURL
        )
        builder.baseUrl(url!!)
//            builder.baseUrl(MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, BuildConfig.BaseURL))

        builder.addConverterFactory(GsonConverterFactory.create(gson))
        builder.addCallAdapterFactory(CoroutineCallAdapterFactory())
        builder.addConverterFactory(ScalarsConverterFactory.create())
        builder.client(getOkHttpClient())
        retrofit = builder.build()

        return retrofit!!
    }

    /**
     * Generate Retrofit Client For Brainvire API
     */
    private fun getRetrofitBrainvire(): Retrofit {
        val builder = Retrofit.Builder()
        builder.baseUrl(BuildConfig.BaseURLBrainvire)
        builder.addConverterFactory(GsonConverterFactory.create(gson))
        builder.addCallAdapterFactory(CoroutineCallAdapterFactory())
        builder.client(getOkHttpClient())
        retrofit = builder.build()
        return retrofit!!
    }

    /**
     * generate OKhttp client
     */




    private fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val builder = OkHttpClient.Builder()
            /* if (BuildConfig.DEBUG) {
                 builder.addInterceptor(logging)
             }*/
            builder.readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(responseCodeInterceptor)
                .build()
            okHttpClient = builder.build()

        }
        return okHttpClient!!
    }

    /**
     * Generate API interface object for foreground with progress bar
     *
     * @param con instance of Context
     * @param loaderMessage any message want to display with progress bar as String
     * @param isForBackground true if want to execute API calling in background
     */
    @Throws(NoConnectionException::class)
    fun getApiClient(): ApiInterface {
        return getRetrofit().create(ApiInterface::class.java)
    }

    @Throws(NoConnectionException::class)
    fun getApiClientAddress(): ApiInterface {
        val builder = Retrofit.Builder()
        val url: String? = MyPreference.getValueString(
            PrefKey.SELECTED_SERVER_DATA,
            BuildConfig.BaseURL
        )
        builder.baseUrl(url!!)
        builder.addConverterFactory(ScalarsConverterFactory.create())
        builder.addCallAdapterFactory(CoroutineCallAdapterFactory())
        builder.client(getOkHttpClient())
        retrofit = builder.build()

        return retrofit!!.create(ApiInterface::class.java)
    }


    @Throws(NoConnectionException::class)
    fun getApiClientBrainvire(): ApiInterface {

        return getRetrofitBrainvire().create(ApiInterface::class.java)
    }

    /**
     * generate custom resendResponse for exception
     */
    private fun generateCustomResponse(code: Int, message: String, request: Request): Response? {
        try {
            val body = ResponseBody.create(
                "application/json".toMediaTypeOrNull(),
                getJSONObjectForException(message, code).toString()
            )
            return Response.Builder()
                .code(code)
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(body)
                .message(message)
                .build()
        } catch (ex: JsonIOException) {
            DebugLog.print(ex)
            return null
        }

    }

    /**
     * generate JSON object for error case
     */
    private fun getJSONObjectForException(message: String, code: Int): JSONObject {

        try {
            val jsonMainObject = JSONObject()

            val `object` = JSONObject()
            `object`.put("status", false)
            `object`.put("message", message)
            `object`.put("message_code", code)
            `object`.put("status_code", code)

            jsonMainObject.put("meta", `object`)

            val obj = JSONObject()
            obj.put("error", JSONArray().put(message))

            jsonMainObject.put("errors", obj)

            return jsonMainObject
        } catch (e: JSONException) {
            DebugLog.print(e)
            return JSONObject()
        }

    }
}
