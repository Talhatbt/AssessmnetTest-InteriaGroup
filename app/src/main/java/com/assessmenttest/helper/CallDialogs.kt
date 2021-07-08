package com.assessmenttest.helper

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.assessmenttest.R


enum class CallDialogs {
    INSTANCE;

    private var mDialog: Dialog? = null
    private val mDialogLocation: Dialog? = null
    private val progress = 0
    private val timer: CountDownTimer? = null
    var counter = 0
    var totalTime = 0
    fun dismissDialog() {
        try {
            if (null != mDialog && isShowing) mDialog!!.dismiss()
            timer?.cancel()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun dismissDialogLocation() {
        try {
            if (null != mDialogLocation && isShowingLocation) mDialogLocation.dismiss()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    val isShowing: Boolean
        get() = null != mDialog && mDialog!!.isShowing
    private val isShowingLocation: Boolean
        private get() = null != mDialogLocation && mDialogLocation.isShowing

    /**
     * Shows Counter dialog while booking request is in progress
     *
     * @param context               Calling context
     * @param onCancelClickListener callback interface for cancel button click
     */
    fun showDistanceDialog(
        context: Context, distance: String, location: String,
        onAttemptClickListener: View.OnClickListener?
    ) {
        dismissDialog()
        mDialog = Dialog(context, R.style.actionSheetTheme)
        mDialog!!.setContentView(R.layout.layout_dialog_show_info)
        val btnOk: AppCompatButton = mDialog!!.findViewById(R.id.btnOk)
        val tvLocation: AppCompatTextView = mDialog!!.findViewById(R.id.tvLocation)
        val tvDistance: AppCompatTextView = mDialog!!.findViewById(R.id.tvDistance)

        tvDistance.text = distance
        tvLocation.text = location
        btnOk.setOnClickListener(onAttemptClickListener)
        mDialog!!.setCancelable(false)
        showDialog()
    }

    /**
     * Dismisses dialog
     *
     * @param dialog current dialog instance
     */
    fun dismissDialog(dialog: Dialog?) {
        try {
            if (null != dialog && dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showDialog() {
        try {
            if (null != mDialog) {
                if (mDialog!!.isShowing) {
                    mDialog?.dismiss()
                }
                mDialog?.show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun showLoader(context: Context?) {
        if (null != mDialog && mDialog!!.isShowing) return
        dismissDialog()
        mDialog = Dialog(context!!, R.style.actionSheetTheme)
        mDialog?.setContentView(R.layout.loading)
        mDialog?.setCancelable(false)
        showDialog()
    }
}