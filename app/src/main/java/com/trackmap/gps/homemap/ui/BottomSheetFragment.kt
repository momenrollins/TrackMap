package com.trackmap.gps.homemap.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trackmap.gps.R
import com.trackmap.gps.databinding.NotificationBottomSheetBinding


class BottomSheetFragment : BottomSheetDialogFragment() {

    private var comingFrom: String? = ""
    private lateinit var view_layout: View

    private var noti_title: String? = ""
    private var noti_time: String? = ""
    private var noti_msg: String? = ""

    companion object {
        fun newInstance(
                comingFrom: String
        ) = BottomSheetFragment().apply {
            this.comingFrom = comingFrom
        }
    }

    private lateinit var binding_notification: NotificationBottomSheetBinding
    private lateinit var dialog: BottomSheetDialog
    private lateinit var behavior: BottomSheetBehavior<View>
    private var isVisibleView: Boolean = false
    private var isSensorVisible: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val sheet = d.findViewById<View>(R.id.design_bottom_sheet)
            behavior = BottomSheetBehavior.from(sheet!!)
            behavior.isHideable = false
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        }
        dialog.setOnDismissListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, 0)
        //dialog cancel when touches outside (Optional)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        when {
            comingFrom.equals("notification", true) -> {
                binding_notification =
                        DataBindingUtil.inflate(
                                inflater,
                                R.layout.notification_bottom_sheet,
                                container,
                                false
                        )
                view_layout = binding_notification.root
            }
        }

        return view_layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.click = MyHandler()

        when {
            comingFrom.equals("notification", true) -> {
                binding_notification.txtMyCar.text = noti_title
                binding_notification.txtDateTime.text = noti_time
                binding_notification.txtNotificationDetails.text = noti_msg

            }
        }
    }

    fun passNotificationData(title: String?, time: String?, msg: String?) {
        this.noti_title = title
        this.noti_time = time
        this.noti_msg = msg
    }

}