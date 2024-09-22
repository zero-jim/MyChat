package com.exoleviathan.mychat.auth.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.databinding.ActivityAuthenticationBinding
import com.exoleviathan.mychat.utility.AUTH_FRAGMENT_NAME
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentName = this.intent.getStringExtra(AUTH_FRAGMENT_NAME)
        val fragment = when (AuthFragmentNames.get(fragmentName)) {
            AuthFragmentNames.REGISTRATION -> {
                UserRegistrationFragment()
            }

            AuthFragmentNames.EMAIL_VERIFICATION -> {
                EmailAuthenticationFragment()
            }

            else -> {
                UserLoginFragment()
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(binding.container.id, fragment)
            .commitNow()
    }

    companion object {
        private const val TAG = "AuthenticationActivity"
    }
}