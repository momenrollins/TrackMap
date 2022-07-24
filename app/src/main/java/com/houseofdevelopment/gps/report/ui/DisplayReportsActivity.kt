package com.houseofdevelopment.gps.report.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.report.adapter.ReportAdapter
import com.houseofdevelopment.gps.report.model.GeneratedReportModelGps3
import com.houseofdevelopment.gps.report.viewmodel.ReportViewModel
import com.houseofdevelopment.gps.utils.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_display_reports.*
import java.util.*

class DisplayReportsActivity : AppCompatActivity() {
    var reportAdapter: ReportAdapter? = null
    var mItemList: ArrayList<GeneratedReportModelGps3>? = null
    var reportViewModel: ReportViewModel? = null
    var textView: TextView? = null
    var arrowImageView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        var savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_reports)
        textView = findViewById(R.id.txt_toolbar_title)
        arrowImageView = findViewById(R.id.backArrow)
        if (Locale.getDefault().getDisplayLanguage().equals("العربية")) arrowImageView!!.rotation =
            180F

        textView!!.text = getString(R.string.generated_reports)
        textView!!.isSelected = true
        arrowImageView!!.visibility = View.VISIBLE
        arrowImageView!!.setOnClickListener { onBackPressed() }
        reportViewModel = ReportViewModel()
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        savedInstanceState = intent.extras
        mItemList =
            savedInstanceState!!.getSerializable("list") as ArrayList<GeneratedReportModelGps3>?
        var onItemClick = object : ReportAdapter.OnItemClickListener {
            override fun onItemClick(
                position: Int,
                generatedReportModelGps3s: ArrayList<GeneratedReportModelGps3>?
            ) {
                reportViewModel!!.callApiForViewGeneratedReporGps3(
                    this@DisplayReportsActivity,
                    generatedReportModelGps3s!![position].report_id,
                    generatedReportModelGps3s[position].name,
                    generatedReportModelGps3s[position].format
                )
            }

        }

        val handlerAlertDialog = object : CommonAlertDialog.DeleteCarHandler {
            @SuppressLint("NotifyDataSetChanged")
            override fun onYesClick(selected: Int, selectedPosition: Int) {
                reportViewModel!!.callApiForDeleteReportGps3(
                    Objects.requireNonNull(selected.toString())
                )
                val filteredList = reportAdapter!!.getFilteredList()
                Log.d(TAG, "onYesClick: ${filteredList.size} - ${mItemList?.size}")

                if (filteredList.size != mItemList?.size)
                    mItemList?.remove(filteredList[selectedPosition])
                filteredList.remove(filteredList[selectedPosition])
                reportAdapter!!.notifyDataSetChanged()
            }
        }
        reportAdapter =
            ReportAdapter(this@DisplayReportsActivity, handlerAlertDialog, mItemList!!, onItemClick)
        recyclerview.adapter = reportAdapter

        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val filterList: ArrayList<GeneratedReportModelGps3> =
                    ArrayList<GeneratedReportModelGps3>()
                for (item in mItemList!!.indices) {
                    if (mItemList!![item].name.lowercase()
                            .contains(editable.toString().lowercase())
                    ) {
                        filterList.add(mItemList!![item])
                    }
                    reportAdapter!!.updateList(filterList)
                }
            }
        })
    }

    private val TAG = "DisplayReportsActivity"
}