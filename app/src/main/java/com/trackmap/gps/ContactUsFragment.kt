package com.trackmap.gps


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.databinding.FragmentContactUsBinding
import com.trackmap.gps.network.ApiClientForBrainvire
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import com.trackmap.gps.vehicallist.model.SubmitContactRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class ContactUsFragment : BaseFragment() {

    lateinit var binding: FragmentContactUsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_contact_us, container, false)

        handleActionBarHidePlusIcon(R.string.drawer_contact_us)

        binding.btnSubmit.setOnClickListener {
            when {
                binding.etFullName.text?.trim().toString().isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.please)} ${getString(R.string.enter)} ${
                            getString(
                                R.string.full_name
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etMobNo.text?.trim().toString().isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_valid_mobile_number),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etEmail.text?.trim().toString().isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.please)} ${getString(R.string.enter)} ${
                            getString(
                                R.string.email
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(
                    binding.etEmail.text?.trim().toString()
                ).matches() -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.please)} ${getString(R.string.enter_valid)} ${
                            getString(
                                R.string.email
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etIdNumber.text?.trim().toString().isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.please)} ${getString(R.string.enter)} ${
                            getString(
                                R.string.subject
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etMessage.text?.trim().toString().isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.please)} ${getString(R.string.enter)} ${
                            getString(
                                R.string.message
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.etMessage.text?.trim()!!.length < 10 -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.message_should_be_more_than_10_characters),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    callApi()
                }
            }
        }
        binding.webSite.setOnClickListener {
            val myIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tawasolmap.com/"))
            startActivity(myIntent)
        }
        binding.contactNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + 920003972)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                requireActivity().startActivity(intent)
            }
        }
        return binding.root
    }

    private fun callApi() {
        Utils.showProgressBar(requireContext())
        val signInRequest = SubmitContactRequest()
        signInRequest.contact_details = binding.etEmail.text.toString()
        signInRequest.message = binding.etMessage.text!!.trim().toString()
        signInRequest.mobile_no = binding.etMobNo.text!!.trim().toString()
        signInRequest.subject = binding.etIdNumber.text!!.trim().toString()
        signInRequest.name = binding.etFullName.text!!.trim().toString()

        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()
        coroutineScope.launch {

            if (NetworkUtil.getConnectionStatus(requireContext()) == NetworkUtil.NOT_CONNECTED) {
                Utils.hideProgressBar()
            } else {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.submitContact(signInRequest)
                    val rsp = response.string()
                    val jsonObject = JSONObject(rsp)

                    val metaObj = jsonObject.getJSONObject("meta")
                    val statusCode = metaObj.getString("status_code")
                    var message = metaObj.getString("message")
                    Utils.hideProgressBar()
                    if (statusCode.equals("201", true)) {
                        Toast.makeText(
                            requireContext(),
                            "Data submitted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.some_problem_with_request_please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (error: Throwable) {
                    Utils.hideProgressBar()
                    error.printStackTrace()
                }
            }
        }
    }
}
