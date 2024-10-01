package com.exoleviathan.mychat.home.ui.newcontact

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.home.model.newcontact.NewContactData
import com.exoleviathan.mychat.message.ui.MessageActivity
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_NAME
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_UID
import com.exoleviathan.mychat.utility.NavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("NotifyDataSetChanged")
class ContactsListAdapter(private val viewModel: NewContactViewModel) : RecyclerView.Adapter<ViewHolder>() {
    private val items = arrayListOf<Pair<String?, String?>>()

    init {
        CoroutineScope(Dispatchers.Main).launch {

            viewModel.newContactData.collect { data ->
                when (data) {
                    NewContactData.Initial -> {
                        items.clear()
                        notifyDataSetChanged()
                    }

                    is NewContactData.Contact -> {
                        items.add(data.info)
                        notifyItemInserted(items.size - 1)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = parent.context.getSystemService(LayoutInflater::class.java)
        val view = layoutInflater.inflate(R.layout.layout_home_contacts, parent, false)
        return ContactsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as? ContactsListViewHolder)?.bind(items[position], position)
    }

    internal inner class ContactsListViewHolder(private val view: View) : ViewHolder(view) {
        private val divider = view.findViewById<View?>(R.id.divider)
        private val nameText = view.findViewById<TextView?>(R.id.contact_name)

        fun bind(friend: Pair<String?, String?>?, position: Int) {
            if (position == 0) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            nameText.text = friend?.second

            view.setOnClickListener {
                val intent = Intent(view.context, MessageActivity::class.java).apply {
                    putExtra(MESSAGE_RECEIVER_UID, friend?.first)
                    putExtra(MESSAGE_RECEIVER_NAME, friend?.second)
                }
                NavigationHelper.navigateToActivity(view.context, intent)
            }

            view.setOnLongClickListener {

                return@setOnLongClickListener true
            }
        }
    }
}