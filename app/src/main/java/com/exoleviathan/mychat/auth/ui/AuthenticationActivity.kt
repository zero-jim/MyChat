package com.exoleviathan.mychat.auth.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.auth.ui.emailverification.EmailVerificationFragment
import com.exoleviathan.mychat.auth.ui.forgotpassword.ForgotPasswordFragment
import com.exoleviathan.mychat.auth.ui.login.UserLoginFragment
import com.exoleviathan.mychat.auth.ui.registration.UserRegistrationFragment
import com.exoleviathan.mychat.common.CommonAlertDialog
import com.exoleviathan.mychat.common.MyChatActivity
import com.exoleviathan.mychat.databinding.ActivityAuthenticationBinding
import com.exoleviathan.mychat.utility.AUTH_FRAGMENT_NAME
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationActivity : MyChatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var networkAlertDialog: CommonAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkAlertDialog = CommonAlertDialog(this)

        setFragmentContainerBasedOnIntentValue()
    }

    private fun setFragmentContainerBasedOnIntentValue() {
        val fragmentName = this.intent.getStringExtra(AUTH_FRAGMENT_NAME)
        Logger.i(TAG, "setFragmentContainerBasedOnIntentValue", "fragmentName: $fragmentName", ModuleNames.AUTH.value)

        val fragment = when (AuthFragmentNames.get(fragmentName)) {
            AuthFragmentNames.REGISTRATION -> {
                UserRegistrationFragment()
            }

            AuthFragmentNames.EMAIL_VERIFICATION -> {
                EmailVerificationFragment()
            }

            AuthFragmentNames.FORGOT_PASSWORD -> {
                ForgotPasswordFragment()
            }

            else -> {
                UserLoginFragment()
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, fragment)
            .commitNow()
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart", moduleName = ModuleNames.AUTH.value)

        observePhoneNetworkStates()
    }

    private fun observePhoneNetworkStates() {
        Logger.d(TAG, "observePhoneNetworkStates", moduleName = ModuleNames.AUTH.value)

        lifecycleScope.launch {

            networkState.asStateFlow().collect {
                Logger.i(TAG, "observePhoneNetworkStates", "currentState: $it", ModuleNames.AUTH.value)

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

    companion object {
        private const val TAG = "AuthenticationActivity"
    }
}