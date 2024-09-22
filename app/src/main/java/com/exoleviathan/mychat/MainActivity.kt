package com.exoleviathan.mychat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.auth.ui.AuthenticationActivity
import com.exoleviathan.mychat.databinding.ActivityMainBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.AUTH_FRAGMENT_NAME
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.NavigationHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToActivityBasedOnUserInfo()
    }

    private fun navigateToActivityBasedOnUserInfo() {
        Logger.i(TAG, "navigateToActivityBasedOnUserInfo")

        FirebaseAuthenticationHelper.getInstance()?.getUserInformation()?.let {
            Logger.d(TAG, "navigateToActivityBasedOnUserInfo", "userData: $it")

            if (it.isEmailVerified == true) {
                Logger.i(TAG, "navigateToActivityBasedOnUserInfo", "user email is verified")

                val intent = Intent(this, HomeActivity::class.java)
                NavigationHelper.navigateToActivity(this, intent)
                finish()
            } else {
                Logger.i(TAG, "navigateToActivityBasedOnUserInfo", "user email is not verified")

                val intent = Intent(this, AuthenticationActivity::class.java)
                intent.putExtra(AUTH_FRAGMENT_NAME, AuthFragmentNames.EMAIL_VERIFICATION.fragment)
                NavigationHelper.navigateToActivity(this, intent)
                finish()
            }
        } ?: run {
            Logger.i(TAG, "navigateToActivityBasedOnUserInfo", "user information is not available")

            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.putExtra(AUTH_FRAGMENT_NAME, AuthFragmentNames.LOGIN.fragment)
            NavigationHelper.navigateToActivity(this, intent)
            finish()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}