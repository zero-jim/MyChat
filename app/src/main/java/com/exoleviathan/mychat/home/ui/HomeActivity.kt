package com.exoleviathan.mychat.home.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.common.CommonAlertDialog
import com.exoleviathan.mychat.common.MyChatActivity
import com.exoleviathan.mychat.databinding.ActivityHomeBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.firestore.fields.UserProfileInfoDataFields
import com.exoleviathan.mychat.firebase.model.UserAuthData
import com.exoleviathan.mychat.home.ui.home.HomeFragment
import com.exoleviathan.mychat.utility.FCM_MESSAGE_PREFERENCE_ID
import com.exoleviathan.mychat.utility.FCM_TOKEN_KEY
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.SharedPreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeActivity : MyChatActivity() {
    private lateinit var context: Context
    private lateinit var binding: ActivityHomeBinding
    private lateinit var networkAlertDialog: CommonAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.HOME.value)

        context = this
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        networkAlertDialog = CommonAlertDialog(this)

        handleGetUserAuthInformation()

        supportFragmentManager.beginTransaction()
            .replace(binding.containerHome.id, HomeFragment())
            .commitNow()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.appBarHome.toolbarHome)
        supportActionBar?.title = ""
    }

    private fun handleGetUserAuthInformation() {
        lifecycleScope.launch(Dispatchers.IO) {

            FirebaseAuthHelper.getInstance().getUserAuthInformation { userAuthData ->
                Logger.d(TAG, "onStart", "userAuthData: $userAuthData", ModuleNames.HOME.value)

                userAuthData?.let {
                    getUserProfileInformation(it)
                } ?: run {
                    FirebaseAuthHelper.getInstance().signOutUser()

                    lifecycleScope.launch {
                        val intent = Intent(context, MainActivity::class.java)
                        NavigationHelper.navigateToActivity(context, intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun getUserProfileInformation(data: UserAuthData) {
        Logger.d(TAG, "getUserProfileInformation", "data: $data", ModuleNames.HOME.value)

        lifecycleScope.launch(Dispatchers.IO) {

            FirebaseFirestoreHelper.getInstance().getUserProfileInformation(data.uid, UserProfileInfoDataFields.USER_ID.fieldName) { task ->
                Logger.i(TAG, "getUserProfileInformation", "taskStatus: ${task.isSuccessful}", ModuleNames.HOME.value)

                task.addOnSuccessListener {
                    if (it == null || it.isEmpty) {
                        Logger.i(TAG, "getUserProfileInformation", "user profile is not created yet.", ModuleNames.HOME.value)
                        createNewUserProfile(data)
                    } else {
                        Logger.i(TAG, "getUserProfileInformation", "user profile is already created", ModuleNames.HOME.value)
                    }
                }

                task.addOnFailureListener {
                    Logger.e(TAG, "getUserProfileInformation", "failed to get user information", ModuleNames.HOME.value)
                    Toast.makeText(context, task.exception?.message ?: "Failed to get user information", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createNewUserProfile(data: UserAuthData) {
        Logger.d(TAG, "createNewUserProfile", "data: $data", ModuleNames.HOME.value)

        lifecycleScope.launch(Dispatchers.IO) {

            FirebaseFirestoreHelper.getInstance().createUserProfileInformation(data) { task ->
                Logger.i(TAG, "createUserProfile", "response: ${task.isSuccessful}", ModuleNames.HOME.value)

                if (task.isSuccessful) {
                    Toast.makeText(context, "User profile created successfully", Toast.LENGTH_SHORT).show()
                    saveFCMTokenToServer(data.uid)
                } else {
                    Toast.makeText(context, task.exception?.message ?: "User profile creation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveFCMTokenToServer(userId: String) {
        Logger.d(TAG, "saveFCMTokenToServer", "userID: $userId", ModuleNames.HOME.value)

        lifecycleScope.launch(Dispatchers.IO) {
            val serverToken = SharedPreferenceHelper(context, FCM_MESSAGE_PREFERENCE_ID).getItem(FCM_TOKEN_KEY, "")

            FirebaseFirestoreHelper.getInstance().saveFCMToken(userId, serverToken) { task ->
                Logger.i(TAG, "saveFCMTokenToServer", "response: ${task.isSuccessful}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Logger.d(TAG, "onCreateOptionsMenu", moduleName = ModuleNames.HOME.value)

        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Logger.d(TAG, "onOptionsItemSelected", "item: ${item.itemId}", ModuleNames.HOME.value)

        // TODO: create settings activity and navigate to settings activity
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Logger.d(TAG, "onStart", moduleName = ModuleNames.HOME.value)

        observePhoneNetworkStates()
    }

    private fun observePhoneNetworkStates() {
        Logger.d(TAG, "observePhoneNetworkStates", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {

            networkState.asStateFlow().collect {
                Logger.i(TAG, "observePhoneNetworkStates", "currentState: $it", ModuleNames.HOME.value)

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

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        Logger.d(TAG, "onSupportNavigateUp", moduleName = ModuleNames.HOME.value)

        return true
    }

    fun customizeToolbar(title: String?, isHomeAsUpEnabled: Boolean) {
        Logger.d(TAG, "customizeToolbar", "title: $title isHomeAsUpEnabled: $isHomeAsUpEnabled", ModuleNames.HOME.value)

        binding.appBarHome.title.text = title
        supportActionBar?.setDisplayHomeAsUpEnabled(isHomeAsUpEnabled)
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}