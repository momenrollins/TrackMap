package com.houseofdevelopment.gps.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.houseofdevelopment.gps.MainActivity
import com.houseofdevelopment.gps.utils.CommonAlertDialog
import com.houseofdevelopment.gps.utils.DebugLog
import kotlinx.android.synthetic.main.layout_custom_action_bar.*

abstract class BaseFragment : Fragment() {

    lateinit var progressDialog: Dialog
    private val TAG = "BaseFragment"

    companion object {
        private const val MY_REQUEST_CODE = 1111
    }

    private var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForInAppUpdate()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        progressDialog = CommonAlertDialog.createProgressDialog(context)
    }

    fun handleActionBarAString(addUnit: String) {
        (activity as MainActivity).txt_toolbar_title.text = addUnit
        (activity as MainActivity).txt_toolbar_title.isSelected = true
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
    }

    fun handleActionBarAStringUpdateUnit(addUnit: String) {
        (activity as MainActivity).txt_toolbar_title.text = addUnit
        (activity as MainActivity).txt_toolbar_title.isSelected = true
        (activity as MainActivity).add_vehicle.visibility = View.VISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
    }

    fun handleActionBar(addUnit: Int) {
        try {
            (activity as MainActivity).txt_toolbar_title.text = getString(addUnit)
            (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
            (activity as MainActivity).chk_check.visibility = View.INVISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "handleActionBar: ${e.message}")
        }
    }

    fun handleActionBarHidePlusIcon(addUnit: Int) {
        (activity as MainActivity).txt_toolbar_title.text = getString(addUnit)
        (activity as MainActivity).txt_toolbar_title.isSelected = true
        (activity as MainActivity).add_vehicle.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE
        (activity as MainActivity).chk_check.visibility = View.INVISIBLE

    }

    fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        return ConnectionResult.SUCCESS == status
    }

    /**
     * This method is used to check an In App Update feature.
     */
    private fun checkForInAppUpdate() {

        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager?.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    requireActivity(),
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    override fun onResume() {
        super.onResume()
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        requireActivity(),
                        MY_REQUEST_CODE
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                DebugLog.e("Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }
}