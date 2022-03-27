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
        Utils.showProgressBar(requireContext())
        binding.webView.visibility = View.GONE
        handleActionBarHidePlusIcon(R.string.privacy_fragment)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.allowFileAccessFromFileURLs = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.allowFileAccess = true
        binding.webView.loadUrl("http://tawasolmap.com/en/privacy-policy-2/")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                binding.webView.visibility = View.VISIBLE
                Utils.hideProgressBar()
                return true // then it is not handled by default action
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webView.visibility = View.VISIBLE
                Utils.hideProgressBar()
                //binding.layProgress.progressbar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError
            ) {
                //Your code to do
                binding.webView.visibility = View.VISIBLE
                Utils.hideProgressBar()
            }
        }
        return binding.root
    }
}
