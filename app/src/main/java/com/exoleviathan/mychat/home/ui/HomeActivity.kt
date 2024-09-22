package com.exoleviathan.mychat.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.databinding.ActivityHomeBinding
import com.exoleviathan.mychat.firebase.auth.FirebaseAuthenticationHelper
import com.exoleviathan.mychat.firebase.firestore.FirebaseFirestoreHelper
import com.exoleviathan.mychat.firebase.model.UserData
import com.exoleviathan.mychat.home.ui.home.HomeFragment
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.HOME.value)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.containerHome.id, HomeFragment())
            .commitNow()

        setSupportActionBar(binding.appBarHome.toolbarHome)
        supportActionBar?.title = ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Logger.d(TAG, "onOptionsItemSelected", "item: ${item.itemId}", ModuleNames.HOME.value)

        // TODO: go to settings activity
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume", moduleName = ModuleNames.HOME.value)

        val data = FirebaseAuthenticationHelper.getInstance()?.getUserInformation()
        data?.let {
            createUserProfile(it)
        } ?: run {
            FirebaseAuthenticationHelper.getInstance()?.signOutUser()

            val intent = Intent(this, MainActivity::class.java)
            NavigationHelper.navigateToActivity(this, intent)
            finish()
        }
    }

    private fun createUserProfile(data: UserData) {
        Logger.d(TAG, "createUserProfile", "data: $data", ModuleNames.HOME.value)

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestoreHelper.getInstance()?.isUserProfileAlreadyCreated(data.uid) {
                if (it.not()) {
                    FirebaseFirestoreHelper.getInstance()?.createUserProfile(data) { response, msg, msgDetails ->
                        Logger.i(TAG, "createUserProfile", "response: $response, msg: $msg, msgDetails: $msgDetails", ModuleNames.HOME.value)
                        if (response) {
                            Toast.makeText(this@HomeActivity, "User profile created successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@HomeActivity, msgDetails ?: "User profile creation failed", Toast.LENGTH_SHORT).show()
                            createUserProfile(data)
                        }
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()

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