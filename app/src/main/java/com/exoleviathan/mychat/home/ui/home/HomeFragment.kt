package com.exoleviathan.mychat.home.ui.home

import android.Manifest
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.databinding.FragmentHomeBinding
import com.exoleviathan.mychat.home.model.home.HomeIntent
import com.exoleviathan.mychat.home.model.home.HomeState
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.home.ui.newcontact.NewContactFragment
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var homeAdapter: HomeRecyclerAdapter

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.HOME.value)

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.HOME.value)

        homeAdapter = HomeRecyclerAdapter(viewModel)
        binding.recyclerView.adapter = homeAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        floatingActionButtonAction()
        (requireActivity() as? HomeActivity)?.customizeToolbar(resources.getString(R.string.app_name), false)
        askNotificationPermission()
        handleHomeStates()

        lifecycleScope.launch {
            viewModel.homeIntent.send(HomeIntent.AddChatRoomListener)
        }
    }

    private fun floatingActionButtonAction() {
        Logger.d(TAG, "floatingActionButtonAction", moduleName = ModuleNames.HOME.value)

        binding.fab.setOnClickListener {
            Logger.d(TAG, "floatingActionButtonAction", "floating action button clicked", ModuleNames.HOME.value)

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container_home, NewContactFragment())
                .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                .addToBackStack("new_contact_fragment")
                .commit()
        }
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

    private fun handleHomeStates() {
        Logger.d(TAG, "handleHomeStates", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {

            viewModel.homeState.asStateFlow().collect { state ->
                Logger.i(TAG, "handleHomeStates", "state: $state", ModuleNames.HOME.value)

                when (state) {
                    HomeState.Initial -> {
                        Logger.i(TAG, "handleHomeStates", "initial state is loaded", ModuleNames.HOME.value)
                    }

                    is HomeState.FailedToLoadUserInformation -> {
                        lifecycleScope.launch {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, MainActivity::class.java)
                            NavigationHelper.navigateToActivity(context, intent)
                            requireActivity().finish()
                        }
                    }

                    is HomeState.OnChatRoomListUpdated -> {
                        Logger.d(TAG, "handleHomeState", "chatRoomList: ${state.chatRoomList}", ModuleNames.HOME.value)
                        homeAdapter.updateChatRoomData(state.chatRoomList)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d(TAG, "onDestroyView", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {
            viewModel.homeIntent.send(HomeIntent.RemoveChatRoomListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy", moduleName = ModuleNames.HOME.value)

        viewModel.deInit()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}