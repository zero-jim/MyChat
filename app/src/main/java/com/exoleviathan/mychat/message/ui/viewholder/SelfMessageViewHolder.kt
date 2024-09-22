package com.exoleviathan.mychat.message.ui.viewholder

import android.view.View
import android.widget.TextView
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.toFormattedDate
import com.google.firebase.Timestamp
import java.time.Instant

class SelfMessageViewHolder(view: View) : MessageViewHolder(view) {
    private val messageText = view.findViewById<TextView?>(R.id.message_text)
    private val timestamp = view.findViewById<TextView?>(R.id.message_time)

    override fun bind(message: MessageData?) {
        Logger.d(TAG, "bind", "message: $message", ModuleNames.MESSAGE.value)

        val formattedDate = message?.timeStamp?.toFormattedDate() ?: Timestamp(Instant.now()).toFormattedDate()
        messageText?.text = message?.message
        timestamp?.text = formattedDate
    }

    companion object {
        private const val TAG = "SelfMessageViewHolder"
    }
}