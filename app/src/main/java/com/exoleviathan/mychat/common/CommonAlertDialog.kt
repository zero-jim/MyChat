package com.exoleviathan.mychat.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.IconCompat
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.utility.Logger

class CommonAlertDialog(private val context: Context) {
    private var alertDialog: AlertDialog? = null

    private fun initializeAlertDialogue() {
        Logger.d(TAG, "initializeAlertDialogue")

        if (alertDialog == null) {
            Logger.i(TAG, "initializeAlertDialogue", "alert dialog is not instantiated")

            alertDialog = AlertDialog.Builder(context).create()
            val layoutInflater = context.getSystemService(LayoutInflater::class.java)
            val view = layoutInflater.inflate(R.layout.layout_common_alert_dialogue, null, false)
            alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog?.setView(view)
            alertDialog?.setCancelable(false)
            alertDialog?.show()
        }
    }

    fun showAlertDialogue(title: String?, message: String?, isSuccess: Boolean, alertAction: () -> Unit = {}) {
        Logger.i(TAG, "showAlertDialogue", "title: $title message: $message isSuccess: $isSuccess")

        initializeAlertDialogue()

        val progressBar = alertDialog?.findViewById<View?>(R.id.progress_bar)
        val alertLayout = alertDialog?.findViewById<View?>(R.id.alert_layout)

        progressBar?.visibility = View.GONE
        alertLayout?.visibility = View.VISIBLE

        val titleText = alertDialog?.findViewById<TextView?>(R.id.title_text)
        titleText?.text = title

        val messageText = alertDialog?.findViewById<TextView?>(R.id.message_text)
        messageText?.text = message

        val icon = if (isSuccess) {
            val ic = IconCompat.createWithResource(context, R.drawable.icon_tick).toIcon(context)
            ic.setTint(context.resources.getColor(R.color.green, context.theme))
            ic
        } else {
            val ic = IconCompat.createWithResource(context, R.drawable.icon_failed).toIcon(context)
            ic.setTint(context.resources.getColor(R.color.red, context.theme))
            ic
        }
        val authIcon = alertDialog?.findViewById<ImageView?>(R.id.auth_icon)
        authIcon?.setImageIcon(icon)

        val okayButton = alertDialog?.findViewById<Button>(R.id.okay_button)
        okayButton?.setOnClickListener {
            Logger.i(TAG, "showAlertDialogue", "okay button click event")
            alertDialog?.dismiss()
            alertAction.invoke()
        }

        if (alertDialog?.isShowing != true) {
            alertDialog?.show()
        }
    }

    fun showAlertProgress(isProgressing: Boolean) {
        Logger.i(TAG, "showAlertProgress", "isProgressing: $isProgressing")

        initializeAlertDialogue()

        val progressBar = alertDialog?.findViewById<View?>(R.id.progress_bar)
        val alertLayout = alertDialog?.findViewById<View?>(R.id.alert_layout)

        if (isProgressing) {
            progressBar?.visibility = View.VISIBLE
            alertLayout?.visibility = View.GONE
        } else {
            progressBar?.visibility = View.GONE
            alertLayout?.visibility = View.VISIBLE
        }

        if (alertDialog?.isShowing != true) {
            alertDialog?.show()
        }
    }

    fun dismissAlert() {
        Logger.i(TAG, "dismissAlert")

        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    companion object {
        private const val TAG = "CommonAlertDialog"
    }
}