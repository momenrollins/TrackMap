package com.trackmap.gps.base

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.trackmap.gps.utils.CommonAlertDialog

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var progressDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppBase.instance.setCurrentActivity(this)
        progressDialog = CommonAlertDialog.createProgressDialog(this@BaseActivity)
    }

    override fun onResume() {
        super.onResume()
        AppBase.instance.setCurrentActivity(this)
    }
}