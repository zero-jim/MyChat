package com.exoleviathan.mychat.auth.ui.forgotpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.auth.models.forgotpassword.ForgotPasswordIntent
import com.exoleviathan.mychat.auth.models.forgotpassword.ForgotPasswordState
import com.exoleviathan.mychat.common.CommonAlertDialog
import com.exoleviathan.mychat.databinding.FragmentForgotPasswordBinding
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel
    private var forgotPasswordAlert: CommonAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        forgotPasswordAlert = CommonAlertDialog(requireContext())

        binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        handleForgotPasswordStates()
        sendCodeButtonAction()
    }

    private fun sendCodeButtonAction() {
        Logger.d(TAG, "sendCodeButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.sendCode.setOnClickListener {
            Logger.i(TAG, "sendCodeButtonAction", "send code button is clicked", ModuleNames.AUTH.value)

            forgotPasswordAlert?.showAlertProgress(true)
            lifecycleScope.launch {
                viewModel.forgotPasswordIntent.send(ForgotPasswordIntent.SendResetPasswordEmail)
            }
        }
    }

    private fun handleForgotPasswordStates() {
        lifecycleScope.launch {

            viewModel.forgotPasswordState.asStateFlow().collect { state ->
                Logger.i(TAG, "handleForgotPasswordStates", "state: $state", ModuleNames.AUTH.value)

                when (state) {
                    ForgotPasswordState.Initial -> {
                        Logger.i(TAG, "handleForgotPasswordStates", "view is in initial state", ModuleNames.AUTH.value)
                    }

                    is ForgotPasswordState.ForgotPasswordEmailSendStatus -> {
                        Logger.i(TAG, "handleForgotPasswordStates", "isEmailSent: ${state.data.isEmailSendSuccess}", ModuleNames.AUTH.value)

                        forgotPasswordAlert?.showAlertDialogue(state.data.emailSendMsg, state.data.emailSendMsgDetails, state.data.isEmailSendSuccess) {
                            requireActivity().supportFragmentManager
                                .popBackStack()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ForgotPasswordFragment"
    }
}