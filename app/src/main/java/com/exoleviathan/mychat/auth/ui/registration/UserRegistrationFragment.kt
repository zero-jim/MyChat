package com.exoleviathan.mychat.auth.ui.registration

import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.auth.models.registration.UserRegistrationIntent
import com.exoleviathan.mychat.auth.models.registration.UserRegistrationState
import com.exoleviathan.mychat.auth.ui.emailverification.EmailVerificationFragment
import com.exoleviathan.mychat.common.CommonAlertDialog
import com.exoleviathan.mychat.databinding.FragmentUserRegistrationBinding
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.hideKeyboard
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserRegistrationFragment : Fragment() {
    private lateinit var binding: FragmentUserRegistrationBinding
    private lateinit var viewModel: UserRegistrationViewModel
    private var registrationAlertDialog: CommonAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        binding = FragmentUserRegistrationBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        registrationAlertDialog = CommonAlertDialog(requireContext())
        binding.containerView.setOnClickListener {
            hideKeyboard(requireActivity())
        }

        handleUserRegistrationStates()
        passwordToggleButtonAction()
        confirmPasswordToggleButtonAction()
        registerButtonAction()
        signInButtonAction()
    }

    private fun handleUserRegistrationStates() {
        Logger.d(TAG, "handleUserRegistrationStates", moduleName = ModuleNames.AUTH.value)

        lifecycleScope.launch {

            viewModel.userRegistrationState.asStateFlow().collect { state ->
                Logger.i(TAG, "handleUserRegistrationStates", "state: $state", ModuleNames.AUTH.value)

                when (state) {
                    UserRegistrationState.Initial -> {
                        Logger.i(TAG, "handleUserRegistrationStates", "view is in initial state")
                    }

                    is UserRegistrationState.UserRegistrationStatus -> {
                        registrationAlertDialog?.showAlertDialogue(state.data.registrationMessage, state.data.registrationMessageDetails, state.data.isSuccessful) {
                            Logger.i(TAG, "handleUserRegistrationStates", "data: ${state.data}", ModuleNames.AUTH.value)

                            if (state.data.isSuccessful) {
                                requireActivity().supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, EmailVerificationFragment())
                                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                                    .addToBackStack(AuthFragmentNames.EMAIL_VERIFICATION.fragment)
                                    .commit()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun passwordToggleButtonAction() {
        Logger.d(TAG, "passwordToggleButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.passwordToggle.setOnClickListener {
            Logger.i(TAG, "passwordToggleButtonAction", "password inputType: ${binding.passwordTextField.inputType}", ModuleNames.AUTH.value)

            if (binding.passwordTextField.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                Logger.i(TAG, "passwordToggleButtonAction", "set input type visible", ModuleNames.AUTH.value)
                binding.passwordToggle.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.icon_invisible))
                binding.passwordTextField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                Logger.i(TAG, "passwordToggleButtonAction", "set input type invisible", ModuleNames.AUTH.value)
                binding.passwordToggle.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.icon_visible))
                binding.passwordTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    private fun confirmPasswordToggleButtonAction() {
        Logger.d(TAG, "confirmPasswordToggleButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.confirmPasswordToggle.setOnClickListener {
            Logger.i(TAG, "confirmPasswordToggleButtonAction", "confirm password inputType: ${binding.confirmPasswordTextField.inputType}", ModuleNames.AUTH.value)

            if (binding.confirmPasswordTextField.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                Logger.i(TAG, "confirmPasswordToggleButtonAction", "set input type visible", ModuleNames.AUTH.value)
                binding.confirmPasswordToggle.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.icon_invisible))
                binding.confirmPasswordTextField.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                Logger.i(TAG, "confirmPasswordToggleButtonAction", "set input type invisible", ModuleNames.AUTH.value)
                binding.confirmPasswordToggle.setImageIcon(Icon.createWithResource(requireContext(), R.drawable.icon_visible))
                binding.confirmPasswordTextField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    private fun registerButtonAction() {
        Logger.d(TAG, "registerButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.registerButton.setOnClickListener {
            Logger.i(TAG, "registerButtonAction", "registration button is clicked", moduleName = ModuleNames.AUTH.value)

            registrationAlertDialog?.showAlertProgress(true)
            lifecycleScope.launch {
                viewModel.userRegistrationIntent.send(UserRegistrationIntent.RegisterNewUserWithEmailAndPassword)
            }
        }
    }

    private fun signInButtonAction() {
        Logger.d(TAG, "signInButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.signInButton.setOnClickListener {
            Logger.i(TAG, "signInButtonAction", "sign in button is clicked", moduleName = ModuleNames.AUTH.value)

            requireActivity().supportFragmentManager
                .popBackStack()
        }
    }

    companion object {
        private const val TAG = "UserRegistrationFragment"
    }
}