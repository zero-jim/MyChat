package com.exoleviathan.mychat.auth.ui.emailverification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.auth.models.emailverification.EmailVerificationIntent
import com.exoleviathan.mychat.auth.models.emailverification.EmailVerificationState
import com.exoleviathan.mychat.databinding.FragmentEmailVerificationBinding
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailVerificationFragment : Fragment() {
    private lateinit var binding: FragmentEmailVerificationBinding
    private lateinit var viewModel: EmailVerificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[EmailVerificationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        binding = FragmentEmailVerificationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        sendEmailVerification()
        sendButtonAction()
        continueButtonAction()
        handleEmailVerificationStates()
    }

    private fun sendEmailVerification() {
        Logger.i(TAG, "sendEmailVerification", moduleName = ModuleNames.AUTH.value)

        lifecycleScope.launch {
            viewModel.emailVerificationIntent.send(EmailVerificationIntent.SendUserEmailVerification)
        }
    }

    private fun sendButtonAction() {
        Logger.d(TAG, "sendButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.sendButton.setOnClickListener {
            Logger.i(TAG, "sendButtonAction", "send button is pressed", ModuleNames.AUTH.value)

            binding.progressLayout.visibility = View.VISIBLE
            binding.sendLayout.visibility = View.GONE
            binding.continueLayout.visibility = View.GONE

            sendEmailVerification()
        }
    }

    private fun continueButtonAction() {
        Logger.d(TAG, "continueButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.continueButton.setOnClickListener {
            Logger.i(TAG, "continueButtonAction", "continue button is pressed", ModuleNames.AUTH.value)

            binding.progressLayout.visibility = View.VISIBLE
            binding.sendLayout.visibility = View.GONE
            binding.continueLayout.visibility = View.GONE

            lifecycleScope.launch {
                viewModel.emailVerificationIntent.send(EmailVerificationIntent.CheckIfEmailVerified)
            }
        }
    }

    private fun handleEmailVerificationStates() {
        Logger.d(TAG, "handleEmailVerificationStates", moduleName = ModuleNames.AUTH.value)

        lifecycleScope.launch {
            viewModel.emailVerificationStates.asStateFlow().collect { state ->
                Logger.i(TAG, "handleEmailVerificationStates", "state: $state", ModuleNames.AUTH.value)

                when (state) {
                    EmailVerificationState.Initial -> {
                        Logger.i(TAG, "handleEmailVerificationStates", "view is in initial state", ModuleNames.AUTH.value)

                        binding.progressLayout.visibility = View.VISIBLE
                        binding.sendLayout.visibility = View.GONE
                        binding.continueLayout.visibility = View.GONE
                    }

                    is EmailVerificationState.SendEmailVerificationStatus -> {
                        Logger.i(TAG, "handleUserLoginStates", "isEmailSend: ${state.isEmailSend}", ModuleNames.AUTH.value)

                        if (state.isEmailSend) {
                            binding.progressLayout.visibility = View.GONE
                            binding.sendLayout.visibility = View.GONE
                            binding.continueLayout.visibility = View.VISIBLE
                        } else {
                            binding.progressLayout.visibility = View.GONE
                            binding.sendLayout.visibility = View.VISIBLE
                            binding.continueLayout.visibility = View.GONE

                            Toast.makeText(requireContext(), "Failed to send verification email, please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    is EmailVerificationState.UserEmailVerificationStatus -> {
                        if (state.isVerified) {
                            Logger.i(TAG, "handleEmailVerificationStates", "user email is verified", ModuleNames.AUTH.value)

                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            NavigationHelper.navigateToActivity(requireContext(), intent)
                            requireActivity().finish()
                        } else {
                            Logger.i(TAG, "handleEmailVerificationStates", "user email is not verified yet", ModuleNames.AUTH.value)

                            binding.progressLayout.visibility = View.GONE
                            binding.sendLayout.visibility = View.GONE
                            binding.continueLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailVerificationFragment"
    }
}