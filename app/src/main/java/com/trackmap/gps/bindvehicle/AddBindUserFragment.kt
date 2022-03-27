package com.trackmap.gps.bindvehicle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.trackmap.gps.R
import com.trackmap.gps.base.AppBase
import com.trackmap.gps.base.BaseFragment
import com.trackmap.gps.bindvehicle.model.AddDriverRequest
import com.trackmap.gps.databinding.FragmentAddBindUserBinding
import com.trackmap.gps.network.ApiClientForBrainvire
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.DebugLog
import com.trackmap.gps.utils.NetworkUtil
import com.trackmap.gps.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class AddBindUserFragment : BaseFragment() {

    private var comingFrom: String = ""
    lateinit var binding: FragmentAddBindUserBinding
    private var addDriverRequest: AddDriverRequest? = null
    private var bioMethod: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_bind_user, container, false)
        val data = AddBindUserFragmentArgs.fromBundle(requireArguments())
        comingFrom = data.comingFrom
        viewVisibility()
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun viewVisibility() {
        when (comingFrom) {
            "code" -> {
                binding.txtCode.text = context?.getString(R.string.code)
                binding.imgCode.setImageResource(R.drawable.ic_blind_code)
                handleActionBarHidePlusIcon(R.string.add_driver)
                bioMethod = "Code"
                binding.textInputcode.visibility = View.VISIBLE
                binding.textverifyCode.visibility = View.VISIBLE
            }
            "fingerprint" -> {
                binding.txtCode.setText(R.string.fingerprint)
                binding.imgCode.setImageResource(R.drawable.ic_fingerprint)
                handleActionBarHidePlusIcon(R.string.fingerprint)
                bioMethod = "Finger"
                binding.textInputcode.visibility = View.GONE
                binding.textverifyCode.visibility = View.GONE
            }
            "face" -> {
                binding.txtCode.setText(R.string.face_detection)
                binding.imgCode.setImageResource(R.drawable.ic_face)
                handleActionBarHidePlusIcon(R.string.face_detection)
                bioMethod = "Face"
                binding.textInputcode.visibility = View.GONE
                binding.textverifyCode.visibility = View.GONE
            }
        }
        binding.btnRegister.setOnClickListener {
            if (isValidateFields()) {
                when (comingFrom) {
                    "code" -> {
                        addDriverRequest = AddDriverRequest()
                        addDriverRequest?.fullName = binding.etFullName.text.toString().trim()
                        addDriverRequest?.userType = "Driver"
                        addDriverRequest?.jobTitle = binding.etJobTitle.text.toString().trim()
                        addDriverRequest?.phoneNumber = binding.etMobNo.text.toString().trim()
                        addDriverRequest?.companyId = binding.etCompanyId.text.toString().trim()
                        addDriverRequest?.idNumber = binding.etIdNumber.text.toString().trim()
                        addDriverRequest?.code = binding.etCode.text.toString().trim()
                        addDriverRequest?.verifyCode = binding.etVerifyCode.text.toString().trim()
                        addDriverRequest?.bioMethod = bioMethod
                        addDriverRequest?.interfaces = "mobile"
                        addDriverRequest?.ownerId =
                            MyPreference.getValueString(PrefKey.OWNER_ID, "")
                        callApiForInsertData(addDriverRequest!!)
                    }
                    "fingerprint" -> {
                        // check if device has finger print authenticity or not
                        // if (checkAuthenticityForFinger()) {
                        addDriverRequest = AddDriverRequest()
                        addDriverRequest?.fullName = binding.etFullName.text.toString().trim()
                        addDriverRequest?.userType = "Driver"
                        addDriverRequest?.jobTitle = binding.etJobTitle.text.toString().trim()
                        addDriverRequest?.phoneNumber = binding.etMobNo.text.toString().trim()
                        addDriverRequest?.companyId = binding.etCompanyId.text.toString().trim()
                        addDriverRequest?.idNumber = binding.etIdNumber.text.toString().trim()
                        addDriverRequest?.bioMethod = bioMethod
                        addDriverRequest?.interfaces = "mobile"
                        addDriverRequest?.ownerId =
                            MyPreference.getValueString(PrefKey.OWNER_ID, "")
                        callApiForInsertData(addDriverRequest!!)
                        /*  } else {
                                    Toast.makeText(context!!, "Finger print not supported", Toast.LENGTH_SHORT).show()
                                }*/

                    }
                    "face" -> {

                        /*   if (!checkForBiometrics()) {
                                    Toast.makeText(context!!, "Face detection not supported", Toast.LENGTH_SHORT).show()
                                } else {
            */
                        addDriverRequest = AddDriverRequest()
                        addDriverRequest?.fullName = binding.etFullName.text.toString().trim()
                        addDriverRequest?.userType = "Driver"
                        addDriverRequest?.jobTitle = binding.etJobTitle.text.toString().trim()
                        addDriverRequest?.phoneNumber = binding.etMobNo.text.toString().trim()
                        addDriverRequest?.companyId = binding.etCompanyId.text.toString().trim()
                        addDriverRequest?.idNumber = binding.etIdNumber.text.toString().trim()
                        addDriverRequest?.bioMethod = bioMethod
                        addDriverRequest?.interfaces = "mobile"
                        addDriverRequest?.ownerId =
                            MyPreference.getValueString(PrefKey.OWNER_ID, "")

                        callApiForInsertData(addDriverRequest!!)
                        //                    }
                    }
                }

            }
        }
    }

    private fun isValidateFields(): Boolean {
        if (binding.etFullName.text.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_enter_your_full_name),
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (binding.etFullName.text.toString()
                .isNotEmpty() && binding.etFullName.text.toString().length < 3
        ) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_enter_full_name_length_3_to_20_character),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        /*  if (binding.etJobTitle.text.toString().isNotEmpty()) {
              Toast.makeText(requireContext(), "Please add data", Toast.LENGTH_SHORT).show()
              return false
          }*/
        else if (binding.etMobNo.text.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_enter_your_mobile_number),
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (binding.etMobNo.text.toString()
                .isNotEmpty() && binding.etMobNo.text.toString().length < 10
        ) {
            Toast.makeText(requireContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (binding.etCompanyId.text.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_enter_your_company_id),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        when (comingFrom) {
            "code" -> {
                if (binding.etCode.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_your_code),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                if (binding.etCode.text.toString()
                        .isNotEmpty() && binding.etCode.text!!.length < 6
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_code_length_6_character),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }

                if (binding.etVerifyCode.text.toString().isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_your_verify_code),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }

                if (!binding.etCode.text.toString().lowercase(Locale.US)
                        .equals(binding.etVerifyCode.text.toString(), true)
                ) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.your_code_and_verify_code_not_matched),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
        }
        return true
    }
    private fun callApiForInsertData(addDriverRequest: AddDriverRequest) {

        // Create a Coroutine scope using a job to be able to cancel when needed
        val viewModelJob = Job()
        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        val client = ApiClientForBrainvire.getApiInterface()

        Utils.showProgressBar(requireContext())
        coroutineScope.launch {
            if (NetworkUtil.getConnectionStatus(AppBase.instance) != NetworkUtil.NOT_CONNECTED) {
                try {
                    // this will run on a thread managed by Retrofit
                    val response = client.callAddDriver(addDriverRequest)

                    DebugLog.d("ResponseBody callSignIn = ${response.body()}")

                    if (response.body()?.meta?.code == 201) {
                        Utils.hideProgressBar()
                        Toast.makeText(
                            requireContext(),
                            response.body()?.meta?.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
        //                            findNavController().navigateUp()
        //                        findNavController().navigate(R.id.bindVehicleFragment)
                        requireActivity().onBackPressed()
                    } else {
                        Utils.hideProgressBar()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.some_problem_with_request_please_try_again),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (error: Throwable) {
                    DebugLog.d("ResponseBody callSignIn = $error")
                    Utils.hideProgressBar()
                }
            }
        }
    }

}
