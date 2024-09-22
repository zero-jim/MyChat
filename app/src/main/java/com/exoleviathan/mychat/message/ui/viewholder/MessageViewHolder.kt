package com.exoleviathan.mychat.message.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.exoleviathan.mychat.firebase.model.MessageData

abstract class MessageViewHolder(view: View) : ViewHolder(view) {
    abstract fun bind(message: MessageData?)
}