package com.exoleviathan.mychat.home.ui.newcontact

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.MainActivity
import com.exoleviathan.mychat.databinding.FragmentNewContactBinding
import com.exoleviathan.mychat.home.model.newcontact.NewContactData
import com.exoleviathan.mychat.home.model.newcontact.NewContactIntent
import com.exoleviathan.mychat.home.model.newcontact.NewContactState
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.hideKeyboard
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewContactFragment : Fragment() {
    private lateinit var binding: FragmentNewContactBinding
    private lateinit var viewModel: NewContactViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.HOME.value)

        viewModel = ViewModelProvider(this)[NewContactViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.HOME.value)

        binding = FragmentNewContactBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.HOME.value)

        (requireActivity() as? HomeActivity)?.customizeToolbar("Add new contact", true)

        binding.contactsRecyclerView.adapter = ContactsListAdapter(viewModel)
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        handleNewContactStates()
        handleNewContactData()
        addNewContactButtonAction()
        addUserButtonAction()

        lifecycleScope.launch {
            viewModel.newContactIntent.send(NewContactIntent.FetchUserAuthData)
        }
    }

    private fun handleNewContactStates() {
        Logger.d(TAG, "handleNewContactStates", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {

            viewModel.newContactState.asStateFlow().collect { state ->
                Logger.i(TAG, "handleNewContactStates", "state: $state", ModuleNames.HOME.value)

                when (state) {
                    NewContactState.Initial -> {
                        Logger.i(TAG, "handleNewContactStates", "initial state", ModuleNames.HOME.value)
                        binding.contactsLayout.visibility = View.GONE
                        binding.addUserLayout.visibility = View.GONE
                    }

                    is NewContactState.AddNewContactStatus -> {
                        Logger.i(TAG, "handleNewContactStates", "addNewContactStatus: ${state.isAdded}", ModuleNames.HOME.value)
                        binding.addUserButton.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                        if (state.isAdded) {
                            binding.addUserLayout.visibility = View.GONE
                        }
                    }

                    is NewContactState.UserAuthDataStatus -> {
                        Logger.i(TAG, "handleNewContactStates", "userAuthStatus: ${state.isAvailable}")

                        if (state.isAvailable) {
                            viewModel.newContactIntent.send(NewContactIntent.AddContactListListener)
                        } else {
                            Toast.makeText(requireContext(), "User information not available.", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, MainActivity::class.java)
                            NavigationHelper.navigateToActivity(context, intent)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

    private fun handleNewContactData() {
        Logger.d(TAG, "handleNewContactData", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {

            viewModel.newContactData.collect { data ->
                Logger.i(TAG, "handleNewContactData", "data: $data", ModuleNames.HOME.value)

                when (data) {
                    NewContactData.Initial -> {
                        binding.contactsLayout.visibility = View.GONE
                    }

                    is NewContactData.Contact -> {
                        binding.contactsLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun addNewContactButtonAction() {
        Logger.d(TAG, "addNewContactButtonAction", moduleName = ModuleNames.HOME.value)

        binding.addNewContact.setOnClickListener {
            Logger.i(TAG, "addNewContactButtonAction", "new contact button clicked", ModuleNames.HOME.value)

            binding.addUserLayout.visibility = View.VISIBLE
        }
    }

    private fun addUserButtonAction() {
        Logger.d(TAG, "addUserButtonAction", moduleName = ModuleNames.HOME.value)

        binding.addUserButton.setOnClickListener {
            Logger.i(TAG, "addUserButtonAction", "add user button clicked", ModuleNames.HOME.value)

            lifecycleScope.launch {
                viewModel.newContactIntent.send(NewContactIntent.AddNewContact)
            }

            hideKeyboard(requireActivity())
            binding.addUserButton.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d(TAG, "onDestroyView", moduleName = ModuleNames.HOME.value)

        lifecycleScope.launch {
            viewModel.newContactIntent.send(NewContactIntent.RemoveContactListListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy", moduleName = ModuleNames.HOME.value)

        viewModel.deInit()
    }

    companion object {
        private const val TAG = "NewContactFragment"
    }
}