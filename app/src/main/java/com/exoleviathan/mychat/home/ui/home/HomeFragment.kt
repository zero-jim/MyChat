package com.exoleviathan.mychat.home.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.databinding.FragmentHomeBinding
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.home.ui.newcontact.NewContactFragment
import com.exoleviathan.mychat.home.viewmodel.HomeViewModel
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        Logger.i(TAG, "requestPermissionLauncher::registerForActivityResult", "isPermissionGranted = $isGranted", ModuleNames.HOME.value)

        if (isGranted.not()) {
            Toast.makeText(requireContext(), "Notification permission is not granted.", Toast.LENGTH_LONG).show()
            requireActivity().finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.HOME.value)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.initialize(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.HOME.value)

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.HOME.value)

        binding.recyclerView.adapter = HomeRecyclerAdapter(viewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.fab.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_home, NewContactFragment())
                .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                .addToBackStack("new_contact_fragment")
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume", moduleName = ModuleNames.HOME.value)

        askNotificationPermission()
        (requireActivity() as? HomeActivity)?.customizeToolbar(resources.getString(R.string.app_name), false)
        viewModel.addOngoingConversationListener()
    }

    private fun askNotificationPermission() {
        Logger.d(TAG, "askNotificationPermission", moduleName = ModuleNames.HOME.value)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Logger.i(TAG, "askNotificationPermission", "phone SDK is >= 33", ModuleNames.HOME.value)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Logger.i(TAG, "askNotificationPermission", "permission not granted", ModuleNames.HOME.value)
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d(TAG, "onPause", moduleName = ModuleNames.HOME.value)

        viewModel.removeOngoingConversationListener()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}