package com.houseofdevelopment.gps.network

import android.util.Log
import com.google.gson.JsonIOException
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.houseofdevelopment.gps.BuildConfig
import com.houseofdevelopment.gps.network.client.ApiInterface
import com.houseofdevelopment.gps.utils.DebugLog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiClientForBrainvire {

    companion object {

        private var okHttpClient: OkHttpClient? = null
        var retrofits: Retrofit? = null
        private var myApiInterface: ApiInterface? = null

        /**
         * This is the generic method which will create retrofit object as singleton.
         */
        fun initRetrofit() {
            if (retrofits == null) {
                retrofits = getRetrofit()
                myApiInterface = retrofits?.create(ApiInterface::class.java)!!
            }
        }

        /**
         * Return API interface
         *
         */
        fun getApiInterface(): ApiInterface {
            if (myApiInterface != null) {
                return myApiInterface!!
            }
            myApiInterface = retrofits?.create(ApiInterface::class.java)!!
            return myApiInterface as ApiInterface
        }

        /**
         * Generate Retrofit Client
         */
        fun getRetrofit(): Retrofit {

            val builder = Retrofit.Builder()
            builder.baseUrl( BuildConfig.BaseURLBrainvire)
            builder.addConverterFactory(GsonConverterFactory.create())
            builder.addCallAdapterFactory(CoroutineCallAdapterFactory())
            builder.client(getOkHttpClient())

            return builder.build()
        }

        /**
         * generate OKhttp client
         */
        private fun getOkHttpClient(): OkHttpClient {


            if (okHttpClient == null) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                val builder = OkHttpClient.Builder()
                if (BuildConfig.DEBUG) {
                    builder.addInterceptor(logging)
                }

                builder.addInterceptor { chain ->
                    Log.d("TAG", "getOkHttpClient:no done ")
                    val request =
                        chain.request().newBuilder().addHeader("Authorization", "aXc123sdG@qecb0")
                            .build()
                    chain.proceed(request)
                }

                builder.readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    //.addInterceptor(HttpHandleIntercept())
                    .build()
                okHttpClient = builder.build()

            } else {
                Log.d("TAG", "getOkHttpClient:no $okHttpClient ")
            }
            return okHttpClient!!
        }

        /**
         * generate custom response for exception
         */
        fun generateCustomResponse(code: Int, message: String, request: Request): Response? {
            return try {
                /*val body = getJSONObjectForException(message, code).toString()
                    .toResponseBody("application/json".toMediaTypeOrNull())*/
                val body = ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    getJSONObjectForException(message, code).toString()
                )
                Log.d("TAG", "generateCustomResponse: q")

                Response.Builder()
                    .code(code)
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(body)
                    .message(message)
                    .build()
            } catch (ex: JsonIOException) {
                DebugLog.print(ex)
                null
            }

        }

        /**
         * generate JSON object for error case
         */
        private fun getJSONObjectForException(message: String, code: Int): JSONObject {

            try {
                val jsonMainObject = JSONObject()

                val jsonObject = JSONObject()
                jsonObject.put("status", false)
                jsonObject.put("", message)
                jsonObject.put("message_code", code)
                jsonObject.put("status_code", code)

                jsonMainObject.put("meta", jsonObject)

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
}