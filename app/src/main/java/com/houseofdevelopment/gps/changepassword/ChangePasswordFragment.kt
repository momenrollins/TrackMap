package com.houseofdevelopment.gps.changepassword

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.AppBase
import com.houseofdevelopment.gps.base.BaseFragment
import com.houseofdevelopment.gps.databinding.FragmentChangePasswordBinding
import com.houseofdevelopment.gps.login.ui.LoginActivity
import com.houseofdevelopment.gps.network.ApiClientForBrainvire
import com.houseofdevelopment.gps.network.client.ApiStatus
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey
import com.houseofdevelopment.gps.usersettings.model.SignoutRequest
import com.houseofdevelopment.gps.utils.CommonAlertDialog
import com.houseofdevelopment.gps.utils.DebugLog
import com.houseofdevelopment.gps.utils.NetworkUtil
import com.houseofdevelopment.gps.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class ChangePasswordFragment : BaseFragment() {

    lateinit var binding: FragmentChangePasswordBinding
    lateinit var viewmodel: ChangePasswordViewModel
    private var serverData: String = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.fragment_change_password, container, false
                )
        handleActionBarHidePlusIcon(R.string.change_password)
        serverData = MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA, "").toString()

        viewmodel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        binding.btnReset.setOnClickListener {
            val newPass = binding.etPassword.text.toString()
            val confirmPass = binding.etConfirmpassword.text.toString()
            val oldPass = binding.etUsername.text.toString()

            if (oldPass.trim().isEmpty()) {
                Toast.makeText(
                        context,
                        getString(R.string.please_enter_your_current_password),
                        Toast.LENGTH_SHORT
                ).show()
            } else if (newPass.trim().isEmpty()) {
                Toast.makeText(
                        context,
                        getString(R.string.please_enter_your_new_password),
                        Toast.LENGTH_SHORT
                ).show()
            } else if (newPass.trim().length<4) {
                Toast.makeText(
                        context,
                        getString(R.string.password_length_error),
                        Toast.LENGTH_SHORT
                ).show()
            } else if (!newPass.equals(confirmPass, false)) {
                Toast.makeText(
                        context,
                        getString(R.string.new_password_and_confirm_password_does_not_match),
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                if (serverData.contains("s3"))
                    viewmodel.callApiForChangePasswordGp3(newPass, oldPass)
                else viewmodel.callApiForChangePassword(newPass, oldPass)
            }
        }


        viewmodel.status.observe(viewLifecycleOwner) {
            when (it!!) {
                ApiStatus.LOADING -> progressDialog.show()
                ApiStatus.DONE -> {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.password_changed_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    callApiForSignOut()
                }
                ApiStatus.ERROR -> {
                    progressDialog.dismiss()
                    showError()
                }
                ApiStatus.NOINTERNET -> {
                    progressDialog.dismiss()
                    CommonAlertDialog.showConnectionAlert(requireContext())
                }
                ApiStatus.SUCCESSFUL -> {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.password_changed_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()

                }
            }
        }
        return binding.root
    }

    private fun showError() {
        if (viewmodel.errorMsg.isNotEmpty()) {
            Utils.showToast(requireContext(), viewmodel.errorMsg, Toast.LENGTH_SHORT)
            viewmodel.errorMsg = ""
        }
    }

    private fun callApiForSignOut() {
        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Utils.showProgressBar(requireContext())

        val signoutRequest = SignoutRequest()
        signoutRequest.deviceToken = MyPreference.getValueString(PrefKey.FCM_TOKEN, "")

        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(AppBase.instance) == NetworkUtil.NOT_CONNECTED) {
            } else {

                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callSignOut(signoutRequest)

//                    DebugLog.d("ResponseBody callSignIn = ${response.string()}")

                    var rsp = response.string()
                    var jsonObject = JSONObject(rsp)

                    var metaObj = jsonObject.getJSONObject("meta")
                    var message = metaObj.getString("message")
                    var code = metaObj.getInt("status_code")
                    var status = metaObj.getBoolean("status")
                    var messageCode = metaObj.getString("message_code")
                    if (status) {
                        Utils.hideProgressBar()
                        MyPreference.clearAllData()
                        val myIntent = Intent(activity, LoginActivity::class.java)
                        myIntent.putExtra("logoutFromApp", true)
                        startActivity(myIntent)
                        requireActivity().finish()
                    }

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody callSignIn = $error")
                    Utils.hideProgressBar()
                    MyPreference.clearAllData()
                    val myIntent = Intent(activity, LoginActivity::class.java)
                    myIntent.putExtra("logoutFromApp", true)
                    startActivity(myIntent)
                    requireActivity().finish()
                }
            }
        }
    }
}
