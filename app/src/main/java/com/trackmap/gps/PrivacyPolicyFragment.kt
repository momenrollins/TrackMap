package com.trackmap.gps


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentAboutUsBinding
import com.trackmap.gps.utils.Utils

/**
 * A simple [Fragment] subclass.
 */
class PrivacyPolicyFragment : BaseFragment() {

    lateinit var binding: FragmentAboutUsBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_about_us, container, false)
        handleActionBarAString(getString(R.string.drawer_privacy_policy))
        binding.pdf.fromAsset("build/privacy_policy.pdf").show();


        return binding.root
    }
}
