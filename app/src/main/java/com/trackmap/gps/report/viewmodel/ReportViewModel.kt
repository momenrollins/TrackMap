package com.trackmap.gps.report.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseViewModel
import com.trackmap.gps.report.model.TempData
import com.trackmap.gps.network.client.ApiStatus
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.report.model.GeneratedReportModelRootGps3
import com.trackmap.gps.report.model.RepModel
import com.trackmap.gps.report.ui.PdfViewerActivity
import com.trackmap.gps.utils.Constants
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.awaitResponse
import java.io.*
import kotlin.collections.HashMap


class ReportViewModel : BaseViewModel() {

    private lateinit var _jsonObject: JSONObject
    private lateinit var _innerObject: JSONObject
    val repModelList = MutableLiveData<List<RepModel>>()
    val repModelListGps3 = MutableLiveData<List<TempData>>()
    val reportsListGps3 = MutableLiveData<GeneratedReportModelRootGps3>()
    var isExecuteReport = MutableLiveData<Boolean>()
    private var downloadedFile: File? = null
    var responseBody = MutableLiveData<ResponseBody>()


    fun callApiForTemplateList() {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("itemsType", "avl_resource")
                _jsonObject.put("propName", "rel_is_account,sys_name")
                _jsonObject.put("propValueMask", "*")
                _jsonObject.put("sortType", "sys_name")
                _jsonObject.put("propType", "creatortree")

                _innerObject = JSONObject()
                _innerObject.put("spec", _jsonObject)
                _innerObject.put("force", 1)
                _innerObject.put("flags", 8192)
                _innerObject.put("from", 0)
                _innerObject.put("to", 1000)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "core/search_items"
            body["params"] = _innerObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody template=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val resp = client.getTemplateList(body)
                    DebugLog.d("ResponseBody template = $resp")

                    resp.body().let {
                        val respBody = resp.body()
                        val jsonObject = JSONObject(respBody.toString())
                        val itemArray: JSONArray = jsonObject.getJSONArray("items")
                        val firstObj = JSONObject(itemArray.get(0).toString())

                        val repObj = firstObj.getJSONObject("rep")

                        val repList = mutableListOf<RepModel>()

                        for (i in 1..repObj.length()) {
                            if (!repObj.isNull(i.toString())) {
                                val jObj = repObj.getJSONObject(i.toString())

                                val model = RepModel()
                                model.id = jObj.getString("id").toInt()
                                model.n = jObj.getString("n")
                                model.ct = jObj.getString("ct")
                                model.c = jObj.getString("c").toInt()

                                repList.add(model)
                            }
                        }

                        repModelList.postValue(repList)
                    }

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    private val TAG = "ReportViewModel"
    fun callApiForTemplateListGps3() {
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    val call = client.getTempListGps3(
                        "{\"service\":\"get_reports\",\"api_key\":\"" +
                                MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "") +
                                "\"}"
                    ).awaitResponse()
                    if (call.isSuccessful) {
                        val tempRoot = call.body()
                        if (tempRoot!!.isStatus) {
                            repModelListGps3.postValue(tempRoot.data)
                            _status.value = ApiStatus.DONE
                        } else {
                            errorMsg = tempRoot.msg
                            _status.value = ApiStatus.ERROR
                        }
                    } else {
                        errorMsg = call.message()
                        _status.value = ApiStatus.ERROR
                    }
                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForExecuteReport(
        selTemplateId: Long,
        objectId: String,
        fromDateInterval: Long,
        toDateInterval: Long
    ) {

        coroutineScope.launch {
            try {
                _innerObject = JSONObject()
                _innerObject.put("from", fromDateInterval)
                _innerObject.put("to", toDateInterval)
                _innerObject.put("flags", 0)

                _jsonObject = JSONObject()
                _jsonObject.put(
                    "reportResourceId",
                    MyPreference.getValueInt(Constants.USER_BACT_ID, 0).toLong()
                )
                _jsonObject.put("reportTemplateId", selTemplateId)
                _jsonObject.put("reportObjectId", objectId.toLong())
                _jsonObject.put("reportObjectSecId", 0)
                _jsonObject.put("interval", _innerObject)


            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "report/exec_report"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody template=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getExecuteReport(body)
                    DebugLog.d("0ResponseBody template = $response")

                    if (response.code() == 200) {
                        isExecuteReport.postValue(true)
//                        Utils.hideProgressBar()
                    } else {
                        Toast.makeText(
                            AppBase.instance.baseContext,
                            AppBase.instance.getString(R.string.this_report_cant_be_generated),
                            Toast.LENGTH_LONG
                        ).show()
                        Utils.hideProgressBar()
                    }
                    _status.value = ApiStatus.DONE
                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                    Log.e(TAG, "callApiForExecuteReport: CATCH ${error.message}")
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }

    fun callApiForExportReportResult(fileName: String) {

        coroutineScope.launch {
            try {
                _jsonObject = JSONObject()
                _jsonObject.put("format", 2)
                _jsonObject.put("pageOrientation", "landscap")
                _jsonObject.put("pageSize", "a4")
                _jsonObject.put("pageWidth", 0)
                _jsonObject.put("coding", "utf8")
                _jsonObject.put("delimiter", "semicolon")
                _jsonObject.put("headings", 1)
                _jsonObject.put("compress", 0)
                _jsonObject.put("attachMap", 1)
                _jsonObject.put("extendBounds", 0)
                _jsonObject.put("hideMapBasis", 0)
                _jsonObject.put("hideGoogleLinks", 0)
                _jsonObject.put("outputFileName", fileName)


            } catch (e: Exception) {
                e.printStackTrace()
            }

            val body = HashMap<String, String>()
            body["svc"] = "report/export_result"
            body["params"] = _jsonObject.toString()
            body["sid"] = MyPreference.getValueString(Constants.E_ID, "").toString()

            _status.value = ApiStatus.LOADING

            DebugLog.d("RequestBody export_report=$body")
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.getExportReport(body)
                    DebugLog.d("ResponseBody export_report = $response")

                    responseBody.postValue(response)
//                    writeResponseBodyToDisk(response)

                    _status.value = ApiStatus.DONE

                } catch (error: Throwable) {
                    errorMsg = error.toString()
                    _status.value = ApiStatus.ERROR
                }
            }
        }
    }


    fun callApiForExportReporGps3(
        fromDate: String,
        toDate: String,
        unitImeis: String,
        reportId: Int,
        name: String,
        format: String,
        context: Context
    ) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "generate_report",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "report_id" to reportId,
                "name" to name,
                "format" to format,
                "imeis" to unitImeis,
                "dtf" to fromDate,
                "dtt" to toDate
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            if (format == "html") {
                val intent = Intent(context, PdfViewerActivity::class.java).apply {
                    putExtra("FilePathName", parms)
                    putExtra("FileName", name)
                }
                context.startActivity(intent)
                Utils.hideProgressBar()
            } else {
                val response = client.getExportReportGps3(parms)
                responseBody.postValue(response)
            }
        }
    }

    fun callApiForViewGeneratedReporGps3(
        context: Context, reportId: String, reportName: String, reportType: String
    ) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_report",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "report_id" to reportId.toInt(),
            )
            Utils.showProgressBar(context)
            val parms = JSONObject(map as Map<*, *>).toString()
            if (reportType.equals("html")) {
                val intent = Intent(context, PdfViewerActivity::class.java).apply {
                    putExtra("FilePathName", parms)
                    putExtra("FileName", reportName)
                }
                context.startActivity(intent)
                Utils.hideProgressBar()
            } else {
                val response = client.getExportReportGps3(parms)
                responseBody.postValue(response)
                writeResponseBodyToDisk(response, reportName, context)
            }
        }
    }

    fun callApiForDeleteReportGps3(
        reportId: String
    ) {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "delete_report_generated",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!,
                "report_id" to reportId.toInt(),
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            val response = client.deleteReportGps3(parms)
            responseBody.postValue(response)
//            writeResponseBodyToDisk(response,reportName,context)
        }


    }

    fun callApiForGetGeneratedReportsGps3() {
        coroutineScope.launch {
            val map = mutableMapOf<String, Any?>(
                "service" to "get_reports_generated",
                "api_key" to MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")!!
            )
            val parms = JSONObject(map as Map<*, *>).toString()
            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
                _status.value = ApiStatus.NOINTERNET
            } else {
                val call = client.getReportsGenerated(parms)!!.awaitResponse()
                if (call.isSuccessful) reportsListGps3.postValue(call.body()!!)

            }
        }
    }


    private fun getPDFFile(response: ResponseBody) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "TempDir"
            )

            val mediaFile = File(
                mediaStorageDir.path + File.separator.toString() + "AddressTrackingReport " + ".pdf"
            )

            val fileReader = ByteArray(4096)
            val fileSize = response.contentLength()
            var fileSizeDownloaded: Long = 0
            inputStream = response.byteStream()
            outputStream = FileOutputStream(mediaFile)
//            while (true) {
            val read = inputStream.read(fileReader)
            /*if (read == -1) {
                 break
             }*/
            outputStream.write(fileReader, 0, read)
            fileSizeDownloaded += read.toLong()
//            }
            outputStream.flush()
            true
        } catch (e: IOException) {
            false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun writeResponseBodyToDisk(
        body: ResponseBody,
        reportName: String,
        context: Context
    ): Boolean {
        return try {
//            downloadPDFToFolder()
            downloadedFile = createPdfFilePath(reportName)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {

                val fileReader = ByteArray(4096)
//                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(downloadedFile!!)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
//                    DebugLog.e(" file download: $fileSizeDownloaded of $fileSize ")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()

                val filePath = downloadedFile!!.absolutePath
                Log.d(TAG, "writeResponseBodyToDisk: $filePath")

//                Utils.createInternalStorageFolder(context!!)
                Utils.hideProgressBar()
                val intent = Intent(context, PdfViewerActivity::class.java)
                intent.putExtra("FileName", reportName)
                intent.putExtra("FilePathName", filePath)
                intent.putExtra("file", downloadedFile)
                context.startActivity(intent)
            }
        } catch (e: IOException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createPdfFilePath(reportName: String): File {
        val root = File(AppBase.instance.cacheDir.path + "/" + reportName + ".pdf")
        if (root.exists()) {
            root.delete()

        }
        root.createNewFile()
        return root
    }
}