package com.trackmap.gps.network.client

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.trackmap.gps.PostResponseModel
import com.trackmap.gps.addgroup.model.CreateGroupModelGps3
import com.trackmap.gps.bindvehicle.model.AddDriverRequest
import com.trackmap.gps.bindvehicle.model.BindDriverRequest
import com.trackmap.gps.changepassword.ChangePasswordModel
import com.trackmap.gps.command.model.CommandDataRootGps3
import com.trackmap.gps.command.model.CommandResponseModel
import com.trackmap.gps.geozone.model.GeoZoneListModel
import com.trackmap.gps.geozone.model.GeoZonesModelRootGps3
import com.trackmap.gps.history.model.TripDetails
import com.trackmap.gps.homemap.model.*
import com.trackmap.gps.login.model.LoginModelGps3
import com.trackmap.gps.login.model.SignInRequest
import com.trackmap.gps.login.model.SignInResponse
import com.trackmap.gps.network.ResponseBase
import com.trackmap.gps.network.model.ResponseData
import com.trackmap.gps.notification.model.NotificationDataGps3
import com.trackmap.gps.notification.model.NotificationDataRootGps3
import com.trackmap.gps.notification.model.NotificationDetails
import com.trackmap.gps.report.model.GeneratedReportModelRootGps3
import com.trackmap.gps.report.model.TempRoot
import com.trackmap.gps.track.model.MsgRootGps3
import com.trackmap.gps.history.model.RouteRootGps3
import com.trackmap.gps.track.model.VideoModel
import com.trackmap.gps.usersettings.model.GetNotificationStatusRequest
import com.trackmap.gps.usersettings.model.SignoutRequest
import com.trackmap.gps.vehicallist.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun callSetToken(
        @FieldMap options: Map<String, String>
    ): SetTokenModel

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getCarListDataOnMap(
        @FieldMap options: Map<String, String>
    ): MapListDataModel?

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getUnitInSession(
        @FieldMap options: Map<String, String>
    ): UnitSessionModel

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun checkUnitInSession(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @GET("wialon/ajax.html")
    fun Video_MODEL_CALL(
        @Query("svc") svc: String?,
        @Query("params") parms: String?,
        @Query("sid") sid: String?
    ): Call<VideoModel?>?

    @FormUrlEncoded
    @POST("gis_geocode")
    suspend fun getAddressOfCar(
        @FieldMap options: Map<String, String>
    ): Response<JsonArray>

    @GET("new_api")
    fun login(@Query("data") data: String): Call<LoginModelGps3>

    @GET("api/api.php")
    fun getCarLocation(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<String>

    @GET("new_api")
    fun getChangePasswordDataGps3(
        @Query("data") data: String
    ): Call<ChangePasswordModel>


    @POST("new_api/")
    fun createGroupCall(
        @Query("data") data: String?
    ): Call<CreateGroupModelGps3>

    @POST("new_api/")
    fun UpdateGroupImeis(
        @Query("data") data: String?
    ): Call<PostResponseModel>

    @GET("new_api/")
    fun getGroupCallGps3(
        @Query("data") data: String?
    ): Call<GroupListDataModelGps3>

    @GET("new_api/")
    fun getGroupDataGps3(
        @Query("data") data: String?
    ): Call<GroupImeisModelGps3>

    @GET("new_api/")
    fun deleteGroupGps3(
        @Query("data") data: String
    ): Call<PostResponseModel>

    @GET("new_api/")
    fun CarDriverDetails_CALL(
        @Query("data") data: String?
    ): Call<CarDriverModelGps3>

    @GET("new_api/")
    fun getSensors(
        @Query("data") data: String
    ): Call<SensorItemModelGps3>

    @GET("api/api.php")
    fun getCarsStringMsg(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<String>?

    @GET("api/api.php")
    fun getCarHistory(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<MsgRootGps3>

    @GET("api/api.php")
    fun getRouteGps3(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<RouteRootGps3>

    @GET("api/api.php")
    fun getCars(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<ArrayList<ItemGps3>>

    @GET("new_api/")
    suspend fun getExportReportGps3(
        @Query("data") data: String
    ): ResponseBody

    @GET("new_api/")
    suspend fun deleteReportGps3(
        @Query("data") data: String
    ): ResponseBody

    @GET("new_api/")
    fun getReportsGenerated(
        @Query("data") data: String
    ): Call<GeneratedReportModelRootGps3>?

    @GET("new_api/")
    fun getGeoZonesGenerated(
        @Query("data") data: String
    ): Call<GeoZonesModelRootGps3>?

    @POST("new_api/")
    fun geoZonesDeletedGps3(
        @Query("data") data: String
    ): Call<PostResponseModel>

    @POST("new_api/")
    fun CreateNotificationGps3(
        @Query("data") data: String
    ): Call<PostResponseModel>?

    @POST("api/delete_token")
    fun deleteTokenGps3(
        @Query("user") user: String,
        @Query("device_token") device_token: String
    ): Call<PostResponseModel>?

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getDriverName(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getCommands(
        @FieldMap options: Map<String, String>
    ): CommandResponseModel

    @GET("new_api/")
    fun getCommandsGps3(
        @Query("data") data: String
    ): Call<CommandDataRootGps3>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getCarDetails(
        @FieldMap options: Map<String, String>
    ): GetCarDetailsModel

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getSensorValue(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getGroupListData(
        @FieldMap options: Map<String, String>
    ): GroupListDataModel

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getCreatedGroupData(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getAddUnitsToGroup(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getDeleterGroupData(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    //    @FormUrlEncoded
    @GET("wialon/ajax.html")
    suspend fun getHistoryData(
        @Query("svc") svg: String,
        @Query("params") params: String,
        @Query("sid") sid: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getTripsData(
        @FieldMap options: Map<String, String>
    ): TripDetails

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getSetLocale(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getTemplateList(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @GET("api/api.php")
    fun sendCmdGps3(
        @Query("api") api: String,
        @Query("key") key: String,
        @Query("cmd") cmd: String
    ): Call<String>

    @POST("api/gps3_user_save_token")
    fun sendTokenGps3(
        @Query("user") user: String,
        @Query("device_token") device_token: String
    ): Call<PostResponseModel>

    @GET("new_api")
    fun getTempListGps3(@Query("data") data: String): Call<TempRoot>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getExecuteReport(
        @FieldMap options: Map<String, String>
    ): Response<JsonObject>

    @Streaming
    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getExportReport(
        @FieldMap options: Map<String, String>
    ): ResponseBody


    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getNotificationList(
        @FieldMap options: Map<String, String>
    ): ResponseBody

    @GET("new_api")
    fun getNotificationListGps3(
        @Query("data") data: String
    ): Call<NotificationDataRootGps3>

    @GET("api/get_notifications")
    fun getNotificationsGps3(
        @Query("user") user: String,
        @Query("imei") imei: String,
    ): Call<ArrayList<NotificationDataGps3>>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getSwitchOnOffData(
        @FieldMap options: Map<String, String>
    ): ResponseBody


    @GET("new_api")
    fun getSwitchOnOffDataGps3(@Query("data") data: String): Call<PostResponseModel>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getStartEngineONData(
        @FieldMap options: Map<String, String>
    ): ResponseBody


    @POST("oauth/signin")
    suspend fun callSignIn(
        @Body signInRequest: SignInRequest?
    ): Response<ResponseData<SignInResponse>>

    @POST("driver/create")
    suspend fun callAddDriver(
        /*@Header("Authorization") token: String?,*/
        @Body addDriverRequest: AddDriverRequest?
    ): Response<ResponseData<ResponseBase>>

    @POST("driver/bind-driver")
    suspend fun callBindDriver(
        @Body bindDriverRequest: BindDriverRequest?
    ): Response<ResponseData<ResponseBase>>

    @POST("setnotification")
    suspend fun callSetNotification(
        @Query("is_sent_notification") is_sent_notification: Boolean,
        @Query("device_token") device_token: String
    ): ResponseBody

    @POST("notificationlist")
    suspend fun callNotificationDetail(
        @Body notificationDetail: NotificationDetails?
    ): ResponseBody

    @POST("notificationstatus")
    suspend fun getNotificationStatus(
        @Body notificationDetail: GetNotificationStatusRequest?
    ): ResponseBody

    @GET("new_api")
    suspend fun getNotificationStatusGps3(
        @Query("data") data: String
    ): ResponseBody


    @POST("contact/create")
    suspend fun submitContact(
        @Body notificationDetail: SubmitContactRequest?
    ): ResponseBody


    @POST("signout")
    suspend fun callSignOut(
        @Body signoutRequest: SignoutRequest?
    ): ResponseBody

    @GET("wialon/ajax.html")
    suspend fun getGeoZoneDetailData(
        @Query("svc") svg: String,
        @Query("params") params: String,
        @Query("sid") sid: String
    ): ResponseBody

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getGeoZoneListData(
        @FieldMap options: Map<String, String>
    ): GeoZoneListModel


    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getAddGeoZoneData(
        @FieldMap options: Map<String, String>
    ): ResponseBody

    @POST("new_api/")
    fun getAddGeoZoneDataGps3(
        @Query("data") data: String
    ): Call<PostResponseModel>

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getDeleteGeoZoneData(
        @FieldMap options: Map<String, String>
    ): ResponseBody

    @FormUrlEncoded
    @POST("wialon/ajax.html")
    suspend fun getChangePasswordData(
        @FieldMap options: Map<String, String>
    ): ResponseBody

}



