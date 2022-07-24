package com.houseofdevelopment.gps.report.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.DataValues
import com.houseofdevelopment.gps.DataValues.showSpeed
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentReportBinding
import com.houseofdevelopment.gps.report.model.CarListingDialogGps3
import com.houseofdevelopment.gps.vehicallist.model.ItemGps3
import com.houseofdevelopment.gps.report.model.TempData
import com.houseofdevelopment.gps.homemap.model.Item
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.report.model.GeneratedReportModelGps3
import com.houseofdevelopment.gps.report.model.RepModel
import com.houseofdevelopment.gps.report.viewmodel.ReportViewModel
import com.houseofdevelopment.gps.utils.CarGroupListingDialog
import com.houseofdevelopment.gps.utils.CarGroupListingDialogGps3
import com.houseofdevelopment.gps.utils.CarListingDialog
import com.houseofdevelopment.gps.utils.Utils
import com.houseofdevelopment.gps.vehicallist.model.GroupListDataModel
import com.houseofdevelopment.gps.vehicallist.model.ItemGroupDataModelGps3
import com.houseofdevelopment.gps.vehicallist.viewmodel.VehiclesListViewModel
import kotlinx.android.synthetic.main.layout_custom_action_bar.*
import okhttp3.ResponseBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ReportFragment : BaseFragment(), View.OnClickListener {

    private var objValues: String = ""
    lateinit var binding: FragmentReportBinding
    lateinit var viewmodel: ReportViewModel
    lateinit var viewModelGps3: VehiclesListViewModel
    var templateList: List<RepModel>? = null
    var templateListGps3: List<TempData>? = null
    private var downloadedFile: File? = null
    private var serverData: String = ""
    private var reportsList: ArrayList<GeneratedReportModelGps3>? = null
    private lateinit var formattList: List<String>
    private var reportFormat: String = ""

    private var carDetailList: MutableList<Item>? = null
    private var carDetailListGps3: ArrayList<ItemGps3>? = null
    private var groupDetailListGps3: ArrayList<ItemGroupDataModelGps3>? = null
    private var groupDetailList: MutableList<GroupListDataModel.Item>? = null
    private var inputFormat = "dd/MM/yyyy HH:mm:ss"
    var selTemplateId = ""
    var selTemplateName = ""
    var objectId = ""
    var reportImeis = StringBuilder()
    var userSelObjectName = ""
    lateinit var responseBody: ResponseBody
    var reportName = ""
    private val dateFormat = SimpleDateFormat(inputFormat, Locale.US)
    private var isApiReportClicked = false
    private val TAG = "ReportFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkReadAndWriteStoragePermissions()
        objValues = resources.getStringArray(R.array.type_array).toList()[0]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report, container, false)
        viewmodel = ViewModelProvider(this)[ReportViewModel::class.java]
        viewModelGps3 = ViewModelProvider(this)[VehiclesListViewModel::class.java]
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        groupDetailList = ArrayList()
        carDetailListGps3 = Utils.getCarListingDataGps3(requireContext()).items
        if (Utils.getCarListingData(requireContext()).items.size > 0)
            carDetailList = Utils.getCarListingData(requireContext()).items
        if (DataValues.serverData.contains("s3")) {
            binding.txtPdf.visibility = View.GONE
            binding.formatSpinner.visibility = View.VISIBLE
            formattList = arrayOf("pdf", "html").toList()
            val arrayAdapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_dropdown_item_1line, formattList
            )
            binding.formatSpinner.adapter = arrayAdapter
            binding.formatSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        reportFormat = formattList[position]
                    }
                }
        } else {
            binding.txtPdf.visibility = View.VISIBLE
            binding.formatSpinner.visibility = View.GONE
        }
        getCurrentDate()
        addObserver()
        initListeners()

        /*binding.etDate.setText(""+sdf2.format(System.currentTimeMillis())+" "+"00:00:00")
        binding.txtDate.setText(sdf1.format(System.currentTimeMillis()))*/

        return binding.root
    }

    fun getCurrentDate() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        binding.startDate.text = dateFormat.format(cal.time)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        binding.endDate.text = dateFormat.format(cal.time)

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun addObserver() {
        val offset = TimeZone.getDefault().rawOffset
        val timeOffset = TimeUnit.MILLISECONDS.toSeconds(offset.toLong())

        (activity as MainActivity).report_history.setOnClickListener {
            viewmodel.callApiForGetGeneratedReportsGps3()
        }
        viewmodel.reportsListGps3.observe(this) {
            reportsList = it.data as ArrayList<GeneratedReportModelGps3>?
            val intent = Intent(context, DisplayReportsActivity::class.java).apply {
                putExtra("list", reportsList)
            }
            startActivity(intent)
        }
        viewmodel.callApiForSetLocale(timeOffset, 2)
        if (serverData.contains("s3")) {
            viewmodel.callApiForTemplateListGps3()
            viewmodel.repModelListGps3.observe(this) {
                templateListGps3 = it
                setValues()
            }
        } else {
            viewmodel.callApiForTemplateList()
            viewmodel.repModelList.observe(this) {
                templateList = it
                setValues()
            }
        }

        if (serverData.contains("s3")) {
            viewModelGps3.callApiForGroupListDataGps3()
        } else {
            viewModelGps3.callApiForGroupListData()
        }
        viewModelGps3.groupDataGps3.observe(this) {
            groupDetailListGps3 = it.data as ArrayList<ItemGroupDataModelGps3>?
        }

        viewModelGps3.groupData.observe(this) {
            if (groupDetailList.isNullOrEmpty()) {
                groupDetailList = it as MutableList<GroupListDataModel.Item>?
            }
        }
        viewmodel.isExecuteReport.observe(this) {
            val isExecuted = it
            if (isExecuted) {
                setReportFileName()
            }
        }
        /* viewmodel._status.observe(requireActivity(), Observer {
             if (it.equals(ApiStatus.LOADING)) {
                 Utils.showProgressBar(requireContext())
             } else {
                 Utils.hideProgressBar()
             }
         })*/
        viewmodel.responseBody.observe(this) {
            responseBody = it
            writeResponseBodyToDisk(it)
        }
    }

    private fun setValues() {
        binding.txtObject.text = objValues
        if (serverData.contains("s3") && templateListGps3 != null) {
            for (md in templateListGps3!!) {
                if (objValues.equals(getString(R.string.`object`), true)) {
                    if (md.category.equals("Object")) {
                        binding.txtReportName.text = md.name.toString()
                        selTemplateId = md.report_id.toString()
                        selTemplateName = md.name.toString()
                        break
                    }
                } else if (objValues.equals(getString(R.string.group), true)) {
                    if (md.category.equals("Group")) {
                        binding.txtReportName.text = md.name.toString()
                        selTemplateId = md.report_id.toString()
                        selTemplateName = md.name.toString()
                        break
                    }
                }
            }

        } else if (templateList != null) {
            for (md in templateList!!) {
                if (objValues.equals(getString(R.string.`object`), true)) {
                    if (md.ct.equals("avl_unit")) {
                        binding.txtReportName.text = md.n.toString()
                        selTemplateId = md.id.toString()
                        break
                    }
                } else if (objValues.equals(getString(R.string.group), true)) {
                    if (md.ct.equals("avl_unit_group")) {
                        binding.txtReportName.text = md.n.toString()
                        selTemplateId = md.id.toString()
                        break
                    }
                }
            }
        }
        setSpinner()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        return try {
//            downloadPDFToFolder()
            downloadedFile = createPdfFilePath()
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

//                Utils.createInternalStorageFolder(context!!)
                Utils.hideProgressBar()
                val intent = Intent(context, PdfViewerActivity::class.java)
                intent.putExtra("FileName", reportName)
                intent.putExtra("FilePathName", filePath)
                intent.putExtra("file", downloadedFile)
                startActivity(intent)
            }
        } catch (e: IOException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createPdfFilePath(): File {
        val root = File(requireContext().cacheDir.path + "/" + reportName + ".pdf")
        if (root.exists()) {
            root.delete()
        }
        root.createNewFile()
        return root
    }

    private fun checkReadAndWriteStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED || requireContext().checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
//                downloadAndOpenPDF()
                callApi()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    108
                )
            }
        } else {
            callApi()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 108) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                callApi()
            }
        }
    }

    private fun setReportFileName() {
        val nm = userSelObjectName + "_" + Utils.convertDateFormat(
            binding.startDate.text.toString(),
            inputFormat,
            "dd-MM-yyyy"
        )
        reportName = nm
        if (!serverData.contains("s3")) viewmodel.callApiForExportReportResult(nm)

    }

    private fun initListeners() {
        binding.startLayout.setOnClickListener {
            val year =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "yyyy")
                    .toInt()
            var month =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "MM")
                    .toInt()
            when (month > 0) {
                true -> month -= 1
            }
            val day = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "dd")
                .toInt()
            val hour = Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "HH")
                .toInt()
            val minute =
                Utils.mGetFormattedDate(binding.startDate.text.toString(), inputFormat, "mm")
                    .toInt()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->

                    val timePickerDialog =
                        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->

                            binding.startDate.text = Utils.convertDateFormat(
                                dayOfMonth.toString() + "/" + (month + 1) + "/" + year.toString() + " " + hourOfDay + ":" + minute + ":" + "00",
                                inputFormat,
                                inputFormat
                            )

                        }, hour, minute, true)
                    timePickerDialog.show()
                }, year, month, day
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog.show()
        }
        binding.endLayout.setOnClickListener {
            val year = Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "yyyy")
                .toInt()
            var month =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "MM").toInt()
            when (month > 0) {
                true -> month -= 1
            }
            val day =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "dd").toInt()
            val hour =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "HH").toInt()
            val minute =
                Utils.mGetFormattedDate(binding.endDate.text.toString(), inputFormat, "mm").toInt()


            val datePickerDialog1 = DatePickerDialog(
                requireContext(),
                { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->

                    val timePickerDialog =
                        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->

                            binding.endDate.text = Utils.convertDateFormat(
                                dayOfMonth.toString() + "/" + (month + 1) + "/" + year.toString() + " " + hourOfDay + ":" + minute + ":" + "00",
                                inputFormat,
                                inputFormat
                            )

                        }, hour, minute, DateFormat.is24HourFormat(context))
                    timePickerDialog.show()
                },
                year,
                month,
                day
            )
            datePickerDialog1.datePicker.maxDate = System.currentTimeMillis();
            datePickerDialog1.show()
        }
        binding.typeLayout.setOnClickListener(this)
        binding.txtReport.setOnClickListener(this)
        binding.btnExecute.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        val intent = Intent(context, TemplateActivity::class.java)

        if (v == binding.typeLayout) {
            intent.putExtra("cate_name", "type")
            startActivityForResult(intent, 11)
        }

        if (v == binding.txtReport) {
            intent.putExtra("cate_name", "template")
            intent.putExtra("selectedItem", binding.txtObject.text.toString())
            if (serverData.contains("s3")) {
                if (templateListGps3 != null && templateListGps3?.size != 0) {
                    if (serverData.contains("s3")) {
                        intent.putExtra("repModelData", templateListGps3 as Serializable)
                    }
                    startActivityForResult(intent, 12)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.no_template_to_select),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (templateList != null && templateList?.size != 0) {
                    intent.putParcelableArrayListExtra(
                        "repModelData",
                        templateList as ArrayList<out Parcelable>
                    )
                    startActivityForResult(intent, 12)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.no_template_to_select),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        if (v == binding.btnExecute) {

            convertDateToMilli()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra("result_from")) {
                        val value1 = data.getStringExtra("selectedItem")

                        binding.txtObject.text = value1
                        objValues = value1!!
                        setValues()
                    }
                }
            }
        } else if (requestCode == 12) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.hasExtra("result_from")) {
                        val value = data.getStringExtra("selectedItem")
                        selTemplateId = data.getStringExtra("objectId")!!
                        selTemplateName = value.toString()

                        binding.txtReportName.text = value
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleActionBarHidePlusIcon(R.string.reports)
        if (serverData.contains("s3"))
            (activity as MainActivity).report_history.visibility = View.VISIBLE
    }

    private fun convertDateToMilli() {
        isApiReportClicked = true
        if (isValidateFields()) {
            checkReadAndWriteStoragePermissions()

        }
    }

    private fun callApi() {
        if (isApiReportClicked) {
            if (binding.startDate.text != null && binding.endDate.text != null) {
                val fromDateInterval =
                    Utils.getMillisFromDate(binding.startDate.text.toString(), inputFormat)
                val toDateInterval =
                    Utils.getMillisFromDate(binding.endDate.text.toString(), inputFormat)

                if (selTemplateId.isEmpty() && objectId.isEmpty()) {
                    Toast.makeText(context, "Please select data", Toast.LENGTH_SHORT).show()
                } else {
                    Utils.showProgressBar(requireContext())
                    if (serverData.contains("s3")) {

                        if (objValues == getString(R.string.group)) {
                            objectId = reportImeis.toString()
                        }

                        val fromdate = binding.startDate.text.toString().replace("/", "-")
                        val todate = binding.endDate.text.toString().replace("/", "-")
                        setReportFileName()
                        viewmodel.callApiForExportReporGps3(
                            fromdate,
                            todate,
                            objectId,
                            selTemplateId.toInt(),
                            "${selTemplateName.trim()} - ${userSelObjectName.trim()}",
                            reportFormat,
                            requireContext()
                        )
                    } else {
                        viewmodel.callApiForExecuteReport(
                            selTemplateId.toLong(),
                            objectId,
                            fromDateInterval,
                            toDateInterval
                        )
                    }
                }
            }
        }
    }

    private fun isValidateFields(): Boolean {
        if (binding.startDate.text.trim().toString().isEmpty()) {
            Toast.makeText(
                context,
                getString(R.string.Please_select_start_date),
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (binding.endDate.text.trim().toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.Please_select_end_date), Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (binding.txtObject.text.trim().toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.Please_select_type), Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (binding.txtReportName.text.trim().toString().isEmpty()) {
            Toast.makeText(context, getString(R.string.please_select_template), Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (userSelObjectName.trim().isEmpty()) {
            Toast.makeText(context, getString(R.string.Please_select_vehicle), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true

    }

    private fun setSpinner() {
        if (binding.txtObject.text.toString().equals(getString(R.string.`object`), true)) {
            try {
                if (serverData.contains("s3")) {
                    userSelObjectName = carDetailListGps3!![0].name
                    objectId = carDetailListGps3!![0].imei
                    binding.spSpinner.text = carDetailListGps3!![0].name
                } else {
                    if (carDetailList?.size!! > 0) {
                        userSelObjectName = carDetailList!![0].nm.toString()
                        objectId = carDetailList!![0].id.toString()
                        binding.spSpinner.text = carDetailList!![0].nm
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "setSpinner: CATCH1 ${e.message}")
            }
        } else if (binding.txtObject.text.toString().equals(getString(R.string.group), true)) {
            try {
                if (serverData.contains("s3")) {
                    if (groupDetailListGps3 != null && groupDetailListGps3!!.size > 0) {
                        userSelObjectName = groupDetailListGps3!![0].group_name
                        objectId = groupDetailListGps3!![0].group_id
                        viewModelGps3.callApiForGetGroupDataGps3(objectId.toInt(), false)
                        viewModelGps3.itemGroupDataGps3.observe(viewLifecycleOwner) {
                            try {
                                if (it.isStatus) {
                                    reportImeis.clear()
                                    for (item in it.data.indices) {
                                        if (item == it.data.size - 1) reportImeis.append(it.data[item])
                                        else reportImeis.append("${it.data[item]},")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "setSpinner: CATCH3 ${e.message}")
                            }

                        }
                        binding.spSpinner.text = groupDetailListGps3!![0].group_name
                    }
                } else {
                    try {
                        if (groupDetailList != null && groupDetailList!!.size > 0) {
                            userSelObjectName = groupDetailList!![0].nm.toString()
                            objectId = groupDetailList!![0].id.toString()
                            binding.spSpinner.text = groupDetailList!![0].nm
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "setSpinner: CATCH4 ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "setSpinner: CATCH2 ${e.message}")
            }
        }
        binding.selectCarLL.setOnClickListener {
            if (binding.txtObject.text.toString()
                    .equals(getString(R.string.`object`), true)
            ) {
                showSpeed = false
                if (serverData.contains("s3")) {
                    val dialog =
                        CarListingDialogGps3(object : CarListingDialogGps3.CarClickListener {
                            override fun selectCarGps3(model: ItemGps3) {
                                userSelObjectName = model.name
                                objectId = model.imei
                                binding.spSpinner.text = model.name
                            }
                        }, requireContext())
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                } else {
                    val dialog =
                        CarListingDialog(object : CarListingDialog.CarClickListener {
                            override fun selectCar(model: Item) {
                                userSelObjectName = model.nm.toString()
                                objectId = model.id.toString()
                                binding.spSpinner.text = model.nm
                            }
                        }, requireContext())
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                }
            } else {

                if (serverData.contains("s3")) {
                    val dialog =
                        CarGroupListingDialogGps3(
                            groupDetailListGps3!!,
                            object : CarGroupListingDialogGps3.CarGroupClickListener {
                                override fun selectCarGps3(model: ItemGroupDataModelGps3) {
                                    userSelObjectName = model.group_name
                                    objectId = model.group_id
                                    viewModelGps3.callApiForGetGroupDataGps3(
                                        objectId.toInt(),
                                        false
                                    )
                                    viewModelGps3.itemGroupDataGps3.observe(viewLifecycleOwner) {
                                        if (it.isStatus) {
                                            reportImeis.clear()
                                            for (item in it.data.indices) {
                                                if (item == it.data.size - 1) reportImeis.append(it.data[item])
                                                else reportImeis.append("${it.data[item]},")
                                            }
                                        }

                                    }
                                    binding.spSpinner.text = model.group_name
                                }
                            },
                            requireContext()
                        )
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                } else {
                    val dialog =
                        CarGroupListingDialog(
                            groupDetailList!!,
                            object : CarGroupListingDialog.CarGroupClickListener {
                                override fun selectCar(model: GroupListDataModel.Item) {
                                    userSelObjectName = model.nm.toString()
                                    objectId = model.id.toString()
                                    binding.spSpinner.text = model.nm
                                }
                            },
                            requireContext()
                        )
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "SelectMealPlanDateDialog"
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).report_history.visibility = View.INVISIBLE
    }
}
