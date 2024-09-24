package com.exoleviathan.mychat.message.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.databinding.ActivityMessageBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.message.viewmodel.MessageViewModel
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_NAME
import com.exoleviathan.mychat.utility.MESSAGE_RECEIVER_UID
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.hideKeyboard
import com.exoleviathan.mychat.utility.showKeyboard

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var receiverUid: String
    private lateinit var receiverName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.MESSAGE.value)

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        binding = ActivityMessageBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        getIntentDataAndSetUpSupportActionBar()
    }

    private fun getIntentDataAndSetUpSupportActionBar() {
        Logger.d(TAG, "getIntentDataAndSetUpSupportActionBar", moduleName = ModuleNames.MESSAGE.value)

        receiverUid = intent.getStringExtra(MESSAGE_RECEIVER_UID) ?: ""
        receiverName = intent.getStringExtra(MESSAGE_RECEIVER_NAME) ?: "Unknown User"

        title = receiverName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart", moduleName = ModuleNames.MESSAGE.value)

        binding.messageRecyclerView.adapter = MessageAdapter(viewModel)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        binding.messageRecyclerView.layoutManager = layoutManager

        isSendButtonEnabled(false)

        binding.messageTextField.addTextChangedListener {
            if (TextUtils.isEmpty(it?.trim())) {
                Logger.i(TAG, "onStart", "editTextField is empty", ModuleNames.MESSAGE.value)
                isSendButtonEnabled(false)
            } else {
                Logger.i(TAG, "onStart", "editTextField is not empty", ModuleNames.MESSAGE.value)
                isSendButtonEnabled(true)
            }
        }

        binding.sendButton.setOnClickListener {
            isSendButtonEnabled(false)

            viewModel.sendMessage(this, receiverUid, receiverName, binding.messageTextField.text.toString()) { isSuccess, message ->
                if (isSuccess) {
                    binding.messageTextField.text.clear()
                    hideKeyboard(this)
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                isSendButtonEnabled(true)
            }
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

    private fun isSendButtonEnabled(isEnabled: Boolean) {
        if (isEnabled.not()) {
            binding.sendButton.isEnabled = false
            binding.sendButton.alpha = 0.5f
        } else {
            binding.sendButton.isEnabled = true
            binding.sendButton.alpha = 1f
        }
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        Logger.d(TAG, "onSupportNavigationUp", moduleName = ModuleNames.MESSAGE.value)
        super.onBackPressed()

        return true
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume", moduleName = ModuleNames.MESSAGE.value)

        val data = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()
        if (data == null) {
            FirebaseAuthenticationHelper.getInstance()?.signOutUser()

            val intent = Intent(this, MainActivity::class.java)
            NavigationHelper.navigateToActivity(this, intent)
            finish()
        }

        showProgress(true)

        binding.messageTextField.requestFocus()
        showKeyboard(this)

        viewModel.addMessageSnapshotListener(receiverUid) {
            scrollToFirstPosition()
            showProgress(false)
        }
    }

    private fun scrollToFirstPosition() {
        Logger.d(TAG, "scrollToFirstPosition", moduleName = ModuleNames.MESSAGE.value)

        binding.messageRecyclerView.layoutManager?.scrollToPosition( 0)
    }

    override fun onPause() {
        super.onPause()
        Logger.d(TAG, "onPause", moduleName = ModuleNames.MESSAGE.value)

        viewModel.removeMessageSnapshotListener(receiverUid)
    }

    companion object {
        private const val TAG = "MessageActivity"
    }
}