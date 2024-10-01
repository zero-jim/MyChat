package com.exoleviathan.mychat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.auth.ui.AuthenticationActivity
import com.exoleviathan.mychat.databinding.ActivityMainBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.AUTH_FRAGMENT_NAME
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.NavigationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")

        context = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            navigateToLandingActivity()
        }
    }

    private suspend fun navigateToLandingActivity() {
        Logger.i(TAG, "navigateToLandingActivity")

        FirebaseAuthHelper.getInstance().getUserAuthInformation { authData ->
            Logger.d(TAG, "navigateToLandingActivity", "authData: $authData")

            if (authData == null) {
                Logger.i(TAG, "navigateToLandingActivity", "user information is not available")
                lifecycleScope.launch {
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    intent.putExtra(AUTH_FRAGMENT_NAME, AuthFragmentNames.LOGIN.fragment)
                    NavigationHelper.navigateToActivity(context, intent)
                    finish()
                }
            } else if (authData.isEmailVerified) {
                Logger.i(TAG, "navigateToLandingActivity", "user email is already verified")
                lifecycleScope.launch {
                    val intent = Intent(context, HomeActivity::class.java)
                    NavigationHelper.navigateToActivity(context, intent)
                    finish()
                }
            } else {
                Logger.i(TAG, "navigateToLandingActivity", "user email is not yet verified")
                lifecycleScope.launch {
                    val intent = Intent(context, AuthenticationActivity::class.java)
                    intent.putExtra(AUTH_FRAGMENT_NAME, AuthFragmentNames.EMAIL_VERIFICATION.fragment)
                    NavigationHelper.navigateToActivity(context, intent)
                    finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}