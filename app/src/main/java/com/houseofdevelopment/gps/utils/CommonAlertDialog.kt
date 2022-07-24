package com.houseofdevelopment.gps.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.databinding.LayoutBindInsertCodeDialogBinding
import com.houseofdevelopment.gps.databinding.LayoutBindVehicleDialogBinding
import com.houseofdevelopment.gps.databinding.LayoutDeleteCarBinding


 object CommonAlertDialog {


    /*fun showCustomAlertview(
        context: Context,
        title: String?,
        message: String?,
        positiveActionText: String?,
        negativeActionText: String?,
        handler: (Boolean) -> Unit?
    ) {
        val mDialog = Dialog(context)
        mDialog.setContentView(R.layout.dialog_custom_alerts)
        mDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val binding: DialogCustomAlertsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_custom_alerts,
            null,
            false
        )
        mDialog.setContentView(binding.getRoot())
        mDialog.setCancelable(false)
        binding.title = title
        binding.subtitle = message
        binding.negetivebtntext = negativeActionText
        binding.positivebtntext = positiveActionText
        binding.executePendingBindings()
        try {
            if (!mDialog.isShowing)
                mDialog.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }

    }
*/

    /*   fun cancelable(status: Boolean) {
           mDialog!!.setCancelable(status)
       }

       fun setLeftButton(leftButtonName: String, mOnLeftButtonClickListener: () -> Unit) {
           binding!!.btnCancel.setText(leftButtonName)
           binding!!.btnCancel.setVisibility(View.VISIBLE)
           this.mOnLeftButtonClickListener = mOnLeftButtonClickListener

           binding!!.btnCancel.setOnClickListener({ v ->
               mOnLeftButtonClickListener.invoke()
               mDialog!!.dismiss()
           })
       }

       fun setRightButton(rightButtonName: String, mOnRightButtonClickListener: () -> Unit) {
           binding!!.btnOk.setText(rightButtonName)
           binding!!.btnOk.setVisibility(View.VISIBLE)
           this.mOnRightButtonClickListener = mOnRightButtonClickListener

           binding!!.btnOk.setOnClickListener({ v ->
               mOnRightButtonClickListener.invoke()
               mDialog!!.dismiss()
           })
       }*/


    private var connectionBuilder: AlertDialog? = null

    /**
     * Present alert with one button.
     *
     * @param title              = title for the alert dialog.
     * @param message            = message body for the alert dialog.
     * @param positiveActionText = text for positive action button. If null, takes the default value 'OK'.
     * @param negativeActionText = text for negative action button. If null, takes the default value 'Cancel'.
     * @param context            = context to create alert dialog.
     * @param handler            = handler to get onclick event of action button.
     */
    fun showAlert(
        title: String?,
        message: String?,
        positiveActionText: String?,
        negativeActionText: String?,
        context: Context,
        handler: (Boolean) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        if (null != title) {
            builder.setTitle(title)
        }
        if (null != message) {
            builder.setMessage(message)
        }
        var posText = context.resources.getString(android.R.string.ok)
        if (positiveActionText != null) {
            posText = positiveActionText
        }
        var negText = context.resources.getString(android.R.string.cancel)
        if (negativeActionText != null) {
            negText = negativeActionText
        }
        builder.setPositiveButton(posText) { dialog, which ->
            handler.invoke(true)
        }
        builder.setNegativeButton(negText) { dialog, which ->
            handler.invoke(false)
        }
        builder.setCancelable(false)
        //builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show()
    }


    /**
     * Method for showing no interent connection alert
     * @param context : context of screen where alert box need to show
     */
    fun showConnectionAlert(context: Context) {
        if (connectionBuilder != null && connectionBuilder!!.isShowing) {
            return
        }
        connectionBuilder = AlertDialog.Builder(context).create()
        connectionBuilder!!.setTitle(context.getString(R.string.app_name))
        connectionBuilder!!.setMessage(context.getString(R.string.internet_connection_slow_or_disconnected))
        connectionBuilder!!.setButton(
            AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok)
        ) { dialog, which -> dialog.dismiss() }
        connectionBuilder!!.setCancelable(false)
        connectionBuilder!!.show()
    }


    /*TODO shubham*/
    fun showAlertWithMessage(context: Context, message: String) {
        if (connectionBuilder != null && connectionBuilder!!.isShowing) {
            return
        }
        connectionBuilder = AlertDialog.Builder(context).create()
        connectionBuilder!!.setTitle(context.getString(R.string.app_name))
        connectionBuilder!!.setMessage(message)
        connectionBuilder!!.setButton(
            AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok)
        ) { dialog, which -> dialog.dismiss() }
        connectionBuilder!!.setCancelable(false)
        connectionBuilder!!.show()
    }

    /*TODO shubham for custom on click */
    fun showAlertWithMessageAndHandler(
        context: Context,
        message: String,
        handler: TicketTimerDone
    ) {
        if (connectionBuilder != null && connectionBuilder!!.isShowing) {
            return
        }
        connectionBuilder = AlertDialog.Builder(context).create()
        connectionBuilder!!.setTitle(context.getString(R.string.app_name))
        connectionBuilder!!.setMessage(message)
        connectionBuilder!!.setButton(
            AlertDialog.BUTTON_POSITIVE,
            context.getString(android.R.string.ok)
        ) { _, _ ->
            handler.onSubmitClick()
        }
        connectionBuilder!!.setCancelable(false)
        connectionBuilder!!.show()
    }


    /**
     * Custom listener that handles clicks on Close Click
     * associated with the current item to the [onClick] function.
     */
    class OnCloseClickListener(val clickListener: () -> Unit) {
        private fun onClick() = clickListener()
    }


    @SuppressLint("ObsoleteSdkInt")
    fun createProgressDialog(context: Context): Dialog {
      /*  val mDialog = Dialog(context)

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDialog.window!!.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
        mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCancelable(false)
        mDialog.setContentView(R.layout.layout_progress_dialog)
        return mDialog*/
        val dialog = Dialog(context/*, android.R.style.Theme_Translucent*/)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewChild: View = inflater.inflate(R.layout.layout_progress_dialog, null)
        val imgLoader = viewChild.findViewById<ImageView>(R.id.loader)

        Glide.with(context).load(R.drawable.loader).into(imgLoader)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(viewChild)

        return dialog
    }


    fun showYesNoAlerter(
        context: Context,
        handler: DeleteCarHandler,
        itemId: Int,
        position: Int,
        title: String,
        msg:String
    ): Dialog {
        val mDialog = Dialog(context, R.style.DialogTheme)
        val binding: LayoutDeleteCarBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_delete_car,
            null,
            false
        )
        mDialog.setContentView(binding.root)

        binding.lblDeleteCar.setText(title)
        binding.lblDescriptiveMsg.setText(msg)
        binding.btnYes.setOnClickListener {
            handler.onYesClick(itemId, position)
            mDialog.dismiss()
        }

        binding.btnNo.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mDialog.setCancelable(false)

        binding.executePendingBindings()
        try {
            if (!mDialog.isShowing)
                mDialog.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }
        return mDialog

    }

    fun showBindUserAlerter(
        context: Context?,
        handler: BindUserInterface,
        checkForBiometrics: Boolean,
        hasFaceBiometric: Boolean
    ): Dialog {
        val mDialog = Dialog(context!!, R.style.DialogTheme)
        val binding: LayoutBindVehicleDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_bind_vehicle_dialog,
            null,
            false
        )
        mDialog.setContentView(binding.root)

        if (checkForBiometrics) {
            binding.constFingerPrint.visibility = View.VISIBLE
        } else {
            binding.constFingerPrint.visibility = View.GONE
        }
        if (hasFaceBiometric) {
            binding.constFaceDetection.visibility = View.VISIBLE
        } else {
            binding.constFaceDetection.visibility = View.GONE
        }


        binding.constCode.setOnClickListener {
            handler.onbinditemClick("code")
            mDialog.dismiss()
        }
        binding.constFingerPrint.setOnClickListener {
            handler.onbinditemClick("fingerprint")
            mDialog.dismiss()
        }

        binding.constFaceDetection.setOnClickListener {
            handler.onbinditemClick("face")
            mDialog.dismiss()
        }

        mDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mDialog.setCancelable(true)

        binding.executePendingBindings()
        try {
            if (!mDialog.isShowing)
                mDialog.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }
        return mDialog

    }

    fun showInsertCodeAlerter(
        context: Context?,
        handler: InsertCodeInterface,
        id: Int
    ): Dialog {
        val mDialog = Dialog(context!!, R.style.DialogTheme)
        val binding: LayoutBindInsertCodeDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_bind_insert_code_dialog,
            null,
            false
        )
        mDialog.setContentView(binding.root)

        binding.btnBind.setOnClickListener {
            val value = binding.etCode.text.toString()
            handler.onbindClick(value, id)
            mDialog.dismiss()
        }

        mDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        mDialog.setCancelable(true)

        binding.executePendingBindings()
        try {
            if (!mDialog.isShowing)
                mDialog.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }
        return mDialog

    }

    interface DeleteCarHandler {
        fun onYesClick(selected: Int, selectedPosition: Int)
    }
    interface BindUserInterface {
        fun onbinditemClick(selected: String)
    }

    interface InsertCodeInterface {
        fun onbindClick(selected: String, id: Int)
    }

    interface TicketTimerDone {
        fun onSubmitClick()
    }
}
