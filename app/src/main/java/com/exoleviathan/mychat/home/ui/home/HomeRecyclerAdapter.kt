package com.exoleviathan.mychat.home.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.firebase.model.ChatRoomData
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.message.ui.MessageActivity
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_NAME
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_UID
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.toFormattedDate

@SuppressLint("NotifyDataSetChanged")
class HomeRecyclerAdapter(private val viewModel: HomeViewModel) : RecyclerView.Adapter<ViewHolder>() {
    private var items = arrayListOf<ChatRoomData?>()

    fun updateChatRoomData(chatRoomList: List<ChatRoomData?>) {
        Logger.i(TAG, "updateChatRoomData", "chatRoom size: ${chatRoomList.size}", ModuleNames.HOME.value)

        items.clear()
        items.addAll(chatRoomList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Logger.d(TAG, "onCreateViewHolder", moduleName = ModuleNames.HOME.value)

        val layoutInflater = parent.context.getSystemService(LayoutInflater::class.java)
        val view = layoutInflater.inflate(R.layout.layout_home_chat, parent, false)
        return HomeChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = items[position]

        viewModel.getChatRoomInformation(data?.participantIdList) {
            (holder as? HomeChatViewHolder)?.bind(position, data, it)
        }
    }

    internal inner class HomeChatViewHolder(private val view: View) : ViewHolder(view) {
        private val userName = view.findViewById<TextView>(R.id.user_name)
        private val message = view.findViewById<TextView>(R.id.message)
        private val timeStamp = view.findViewById<TextView>(R.id.time_stamp)
        private val divider = view.findViewById<View>(R.id.divider)
        private val messageSender = view.findViewById<TextView>(R.id.message_sender)

        fun bind(position: Int, data: ChatRoomData?, authData: UserAuthData?) {
            Logger.d(TAG, "HomeChatViewHolder::bind", "data: $data", ModuleNames.HOME.value)

            if (position == 0) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            userName.text = authData?.displayName

            if (viewModel.getChatReadStatus(data).not()) {
                userName.setTextColor(view.context.getColor(R.color.color_primary))
            } else {
                userName.setTextColor(view.context.getColor(R.color.title_text_color))
            }

            message.text = data?.lastMessage
            timeStamp.text = data?.timestamp?.toFormattedDate()
            messageSender.text = data?.lastMessageSenderName

            view.setOnClickListener {
                val intent = Intent(view.context, MessageActivity::class.java).apply {
                    putExtra(MESSAGE_RECEIVER_UID, authData?.uid)
                    putExtra(MESSAGE_RECEIVER_NAME, authData?.displayName)
                }
                NavigationHelper.navigateToActivity(view.context, intent)
            }
        }
    }

    companion object {
        private const val TAG = "HomeRecyclerAdapter"
    }
}