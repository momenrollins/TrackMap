package com.houseofdevelopment.gps.report.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.houseofdevelopment.gps.R;

import java.util.Locale;

public class HTMLViewerActivityGps3 extends AppCompatActivity {

    private WebView webViewId;
    private String url;
    private String reportName;
    private ImageView arrowImageView;
    private TextView textView;
    private final String BaseUrl = "http://gps3.tawasolmap.com/new_api/?data=";
    private static String TAG = "Cannot invoke method length() on null object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_gps3);
        savedInstanceState = getIntent().getExtras();
        url = savedInstanceState.getString("url");
        reportName = savedInstanceState.getString("name");


        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        webViewId = findViewById(R.id.web_view_id);
        arrowImageView = findViewById(R.id.backArrow);
        textView = findViewById(R.id.txt_toolbar_title);
        textView.setText(reportName);
        textView.setSelected(true);
        textView.setTextSize(17f);
        arrowImageView.setVisibility(View.VISIBLE);
        if (Locale.getDefault().getDisplayLanguage().equals("العربية")) arrowImageView.setRotation(180F);

        arrowImageView.setOnClickListener(view -> HTMLViewerActivityGps3.super.onBackPressed());

        Log.d(TAG, "initView:  " + url);
        webViewId.getSettings().setJavaScriptEnabled(true);
        webViewId.loadUrl(BaseUrl + url);

    }

}