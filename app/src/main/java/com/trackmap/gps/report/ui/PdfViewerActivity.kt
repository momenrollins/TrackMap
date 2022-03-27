package com.trackmap.gps.report.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.trackmap.gps.BuildConfig
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseActivity
import java.io.File
import java.util.*


class PdfViewerActivity : BaseActivity() {

    var toolbar: Toolbar? = null
    var toolbarTitle: TextView? = null
    var backImg: AppCompatImageView? = null
    var rvReport: AppCompatImageView? = null
    var pdfView: WebView? = null
    var fileName = ""
    var filePathName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        initElements()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initElements() {
        pdfView = findViewById(R.id.webView)
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        rvReport = findViewById(R.id.rvReport)

        fileName = intent.getStringExtra("FileName")!!
        toolbarTitle!!.text = fileName.replace("-", "/")
        toolbarTitle!!.isSelected = true
        backImg = findViewById(R.id.img_back)
        if (Locale.getDefault().displayLanguage == "العربية") backImg!!.rotation = 180f
        backImg!!.setOnClickListener {
            finish()
        }
        filePathName = intent.getStringExtra("FilePathName")!!
        pdfView!!.webViewClient = WebViewClient()
        pdfView!!.settings.setSupportZoom(true)
        pdfView!!.settings.allowFileAccessFromFileURLs = true
        pdfView!!.settings.javaScriptEnabled = true
        pdfView!!.settings.domStorageEnabled = true
        pdfView!!.settings.allowFileAccess = true
        if (filePathName.contains("{\"service\":")) {
            pdfView!!.loadUrl("http://gps3.tawasolmap.com/new_api/?data=$filePathName")
            rvReport!!.visibility = View.INVISIBLE
        } else pdfView!!.loadUrl("file:///android_asset/web/viewer.html?file=$filePathName")

        rvReport!!.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriFromFile(this, File(filePathName)))
            startActivity(Intent.createChooser(shareIntent, "Share it"))
        }

        //readReportFile()
    }

    private fun uriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}