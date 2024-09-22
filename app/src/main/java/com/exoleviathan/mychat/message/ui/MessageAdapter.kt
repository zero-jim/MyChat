package com.exoleviathan.mychat.message.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.firebase.model.MessageData
import com.exoleviathan.mychat.message.model.MessageViewHolders
import com.exoleviathan.mychat.message.ui.viewholder.MessageViewHolder
import com.exoleviathan.mychat.message.ui.viewholder.OtherMessageViewHolder
import com.exoleviathan.mychat.message.ui.viewholder.SelfMessageViewHolder
import com.exoleviathan.mychat.message.viewmodel.MessageViewModel
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

@SuppressLint("NotifyDataSetChanged")
class MessageAdapter(viewModel: MessageViewModel) : RecyclerView.Adapter<MessageViewHolder>() {
    private var items = arrayListOf<MessageData>()

    init {
        viewModel.messageData.observeForever {
            Logger.i(TAG, "init", "current item size: ${it.size}", ModuleNames.MESSAGE.value)

            items = arrayListOf()
            items.addAll(it)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        Logger.d(TAG, "onCreateViewHolder", "viewType: $viewType", ModuleNames.MESSAGE.value)

        val layoutInflater = parent.context.getSystemService(LayoutInflater::class.java)
        return when (viewType) {
            MessageViewHolders.SELF.value -> {
                val view = layoutInflater.inflate(R.layout.layout_message_self, parent, false)
                SelfMessageViewHolder(view)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.layout_message_other, parent, false)
                OtherMessageViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].messageType
    }

    companion object {
        private const val TAG = "MessageAdapter"
    }
}