package com.exoleviathan.mychat.auth.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.IconCompat
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class AuthAlertDialog(private val context: Context) {
    private var alertDialog: AlertDialog? = null

    fun showAlertDialogue(title: String?, message: String?, isSuccess: Boolean, alertAction: () -> Unit = {}) {
        if (alertDialog == null) {
            Logger.i(TAG, "showAlertProgress", "alert dialog is null", ModuleNames.AUTH.value)

            alertDialog = AlertDialog.Builder(context).create()

            val layoutInflater = context.getSystemService(LayoutInflater::class.java)
            val view = layoutInflater.inflate(R.layout.layout_auth_alert_dialogue, null, false)

            alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog?.setView(view)
        }

        val titleText = alertDialog?.findViewById<TextView?>(R.id.title_text)
        titleText?.text = title

        val messageText = alertDialog?.findViewById<TextView?>(R.id.message_text)
        messageText?.text = message

        val authIcon = alertDialog?.findViewById<ImageView?>(R.id.auth_icon)
        val icon: Icon
        if (isSuccess) {
            icon = IconCompat.createWithResource(context, R.drawable.icon_tick).toIcon(context)
            icon.setTint(context.resources.getColor(R.color.green, context.theme))
        } else {
            icon = IconCompat.createWithResource(context, R.drawable.icon_failed).toIcon(context)
            icon.setTint(context.resources.getColor(R.color.red, context.theme))
        }
        authIcon?.setImageIcon(icon)

        val okayButton = alertDialog?.findViewById<Button>(R.id.okay_button)
        okayButton?.setOnClickListener {
            Logger.i(TAG, "showAlertDialogue", "okay button is clicked", ModuleNames.AUTH.value)
            alertDialog?.dismiss()
            alertAction.invoke()
        }

        if (alertDialog?.isShowing != true) {
            alertDialog?.show()
        }
    }

    fun showAlertProgress(isProgressing: Boolean) {
        Logger.i(TAG, "showAlertProgress", "isProgressing: $isProgressing", ModuleNames.AUTH.value)

        if (alertDialog == null) {
            Logger.i(TAG, "showAlertProgress", "alert dialog is null", ModuleNames.AUTH.value)

            alertDialog = AlertDialog.Builder(context).create()

            val layoutInflater = context.getSystemService(LayoutInflater::class.java)
            val view = layoutInflater.inflate(R.layout.layout_auth_alert_dialogue, null, false)

            alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog?.setView(view)
        }

        val progressBar = alertDialog?.findViewById<ProgressBar?>(R.id.progress_bar)
        val alertLayout = alertDialog?.findViewById<LinearLayout?>(R.id.alert_layout)

        alertDialog?.setCancelable(false)

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
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    companion object {
        private const val TAG = "AuthAlertDialog"
    }
}