package com.exoleviathan.mychat.auth.ui

import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.exoleviathan.mychat.R
import com.exoleviathan.mychat.auth.callbacks.IAuthActionCallback
import com.exoleviathan.mychat.auth.models.AuthFragmentNames
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.viewmodel.UserRegistrationViewModel
import com.exoleviathan.mychat.databinding.FragmentUserRegistrationBinding
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.hideKeyboard

class UserRegistrationFragment : Fragment() {
    private lateinit var binding: FragmentUserRegistrationBinding
    private lateinit var viewModel: UserRegistrationViewModel
    private var registrationAlertDialog: AuthAlertDialog? = null

    private var registerActionCb = object : IAuthActionCallback {

        override fun onSuccess() {
            Logger.i(TAG, "registerActionCb::onSuccess", "account registration completed", ModuleNames.AUTH.value)

            registrationAlertDialog?.showAlertProgress(false)

            registrationAlertDialog?.showAlertDialogue(ValidationMessages.REGISTRATION_SUCCESSFUL.msg, ValidationMessages.REGISTRATION_SUCCESSFUL.msgDetails, true) {
                Logger.i(TAG, "loginActionCb::onSuccess", "user email is not verified", ModuleNames.AUTH.value)

                viewModel.sendUserEmailVerification()

                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, EmailAuthenticationFragment())
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                    .addToBackStack(AuthFragmentNames.EMAIL_VERIFICATION.fragment)
                    .commit()
            }
        }

        override fun onError(msg: String?, msgDetails: String?) {
            Logger.e(TAG, "registerActionCb::onError", "msg: $msg msgDetails: $msgDetails", ModuleNames.AUTH.value)

            registrationAlertDialog?.showAlertProgress(false)
            registrationAlertDialog?.showAlertDialogue(msg, msgDetails, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        binding = FragmentUserRegistrationBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        registrationAlertDialog = AuthAlertDialog(requireContext())
        binding.containerView.setOnClickListener {
            hideKeyboard(requireActivity())
        }

        passwordToggleButtonAction()
        confirmPasswordToggleButtonAction()
        registerButtonAction()
        signInButtonAction()
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
            viewModel.registerButtonAction(registerActionCb)
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