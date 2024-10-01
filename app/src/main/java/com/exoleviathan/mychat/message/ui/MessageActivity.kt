package com.exoleviathan.mychat.message.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.common.CommonAlertDialog
import com.exoleviathan.mychat.common.MyChatActivity
import com.exoleviathan.mychat.databinding.ActivityMessageBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.message.model.MessageIntent
import com.exoleviathan.mychat.message.model.MessageState
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_NAME
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_UID
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.hideKeyboard
import com.exoleviathan.mychat.utility.showKeyboard
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageActivity : MyChatActivity() {
    private lateinit var receiverId: String
    private lateinit var receiverName: String

    private lateinit var binding: ActivityMessageBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var networkAlertDialog: CommonAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.MESSAGE.value)

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        binding = ActivityMessageBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        networkAlertDialog = CommonAlertDialog(this)

        getIntentDataAndSetUpSupportActionBar()
    }

    private fun getIntentDataAndSetUpSupportActionBar() {
        Logger.d(TAG, "getIntentDataAndSetUpSupportActionBar", moduleName = ModuleNames.MESSAGE.value)

        receiverId = intent.getStringExtra(MESSAGE_RECEIVER_UID) ?: ""
        receiverName = intent.getStringExtra(MESSAGE_RECEIVER_NAME) ?: "Unknown User"

        title = receiverName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart", moduleName = ModuleNames.MESSAGE.value)

        messageAdapter = MessageAdapter(viewModel)
        binding.messageRecyclerView.adapter = messageAdapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        binding.messageRecyclerView.layoutManager = layoutManager

        sendButtonClickAction()
        handleMessageStates()
        observePhoneNetworkStates()

        lifecycleScope.launch {
            viewModel.messageIntent.send(MessageIntent.AddMessageUpdateListener(receiverId))
        }
    }

    private fun sendButtonClickAction() {
        Logger.d(TAG, "sendButtonClickAction", moduleName = ModuleNames.MESSAGE.value)

        binding.sendButton.setOnClickListener {
            Logger.i(TAG, "sendButtonClickAction", "sendButton click event", ModuleNames.MESSAGE.value)

            isSendButtonEnabled(false)
            binding.messageTextField.clearFocus()
            hideKeyboard(this)

            lifecycleScope.launch {
                viewModel.messageIntent.send(MessageIntent.SendMessage(this@MessageActivity, receiverId, receiverName))
            }
        }
    }

    private fun handleMessageStates() {
        Logger.d(TAG, "handleMessageStates", moduleName = ModuleNames.MESSAGE.value)

        lifecycleScope.launch {

            viewModel.messageState.asStateFlow().collect { state ->
                Logger.i(TAG, "handleMessageStates", "state: $state", ModuleNames.MESSAGE.value)

                when (state) {
                    is MessageState.FailedToLoadUserInformation -> {
                        Toast.makeText(this@MessageActivity, state.message, Toast.LENGTH_SHORT).show()

                        FirebaseAuthHelper.getInstance().signOutUser()

                        val intent = Intent(this@MessageActivity, MainActivity::class.java)
                        NavigationHelper.navigateToActivity(this@MessageActivity, intent)
                        finish()
                    }

                    MessageState.Initial -> {
                        Logger.i(TAG, "handleMessageStates", "initial state is loaded", ModuleNames.MESSAGE.value)
                        showProgress(true)

                        binding.messageTextField.requestFocus()
                        showKeyboard(this@MessageActivity)
                    }

                    is MessageState.IsSendButtonEnabled -> {
                        Logger.i(TAG, "handleMessageStates", "isSendButtonEnabled: ${state.isEnabled}", ModuleNames.MESSAGE.value)
                        isSendButtonEnabled(state.isEnabled)
                    }

                    is MessageState.MessageListUpdated -> {
                        Logger.i(TAG, "handleMessageStates", "messageList size: ${state.messageList.size}", ModuleNames.MESSAGE.value)
                        messageAdapter.updateDataItems(state.messageList)

                        showProgress(false)
                        scrollToFirstPosition()
                    }

                    is MessageState.SendMessageStatus -> {
                        if (state.isSuccessful.not()) {
                            Toast.makeText(this@MessageActivity, "Failed to send message to $receiverName.", Toast.LENGTH_SHORT).show()
                        }
                        binding.messageTextField.text = null
                    }

                    is MessageState.SetChatRoomDataStatus -> {
                        if (state.isSuccessful.not()) {
                            Toast.makeText(this@MessageActivity, "Failed to update chat room for $receiverName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is MessageState.SendNotificationStatus -> {
                        if (state.isSuccessful.not()) {
                            Toast.makeText(this@MessageActivity, "Failed to send notification to $receiverName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun isSendButtonEnabled(isEnabled: Boolean) {
        if (isEnabled.not()) {
            binding.sendButton.isEnabled = false
            binding.sendButton.alpha = 0.5f
        } else {
            binding.sendButton.isEnabled = true
            binding.sendButton.alpha = 1f
        }
    }

    private fun showProgress(isShown: Boolean) {
        if (isShown) {
            binding.progressBar.visibility = View.VISIBLE
            binding.messageRecyclerView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.messageRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun observePhoneNetworkStates() {
        Logger.d(TAG, "observePhoneNetworkStates", moduleName = ModuleNames.MESSAGE.value)

        lifecycleScope.launch {

            networkState.asStateFlow().collect {
                Logger.i(TAG, "observePhoneNetworkStates", "currentState: $it", ModuleNames.MESSAGE.value)

                if (it.not()) {
                    networkAlertDialog.showAlertDialogue("Network not available", "Please enable internet connection to continue using this app.", false) {
                        finish()
                    }
                } else {
                    networkAlertDialog.dismissAlert()
                }
            }
        }
    }

    private fun scrollToFirstPosition() {
        Logger.d(TAG, "scrollToFirstPosition", moduleName = ModuleNames.MESSAGE.value)

        binding.messageRecyclerView.layoutManager?.scrollToPosition( 0)
    }

    override fun onStop() {
        super.onStop()
        Logger.d(TAG, "onStop", moduleName = ModuleNames.MESSAGE.value)

        lifecycleScope.launch {
            viewModel.messageIntent.send(MessageIntent.RemoveMessageUpdateListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy", moduleName = ModuleNames.MESSAGE.value)

        viewModel.deInit()
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        Logger.d(TAG, "onSupportNavigationUp", moduleName = ModuleNames.MESSAGE.value)
        super.onBackPressed()

        return true
    }

    companion object {
        private const val TAG = "MessageActivity"
    }
}