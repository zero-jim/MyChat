package com.exoleviathan.mychat.auth.ui

import android.content.Intent
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
import com.exoleviathan.mychat.auth.viewmodel.UserLoginViewModel
import com.exoleviathan.mychat.databinding.FragmentUserLoginBinding
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper
import com.exoleviathan.mychat.utility.hideKeyboard

class UserLoginFragment : Fragment() {
    private lateinit var binding: FragmentUserLoginBinding
    private lateinit var viewModel: UserLoginViewModel
    private var loginAlertDialog: AuthAlertDialog? = null

    private val loginActionCb = object : IAuthActionCallback {

        override fun onSuccess() {
            Logger.d(TAG, "loginActionCb::onSuccess", moduleName = ModuleNames.AUTH.value)

            loginAlertDialog?.showAlertProgress(false)

            loginAlertDialog?.showAlertDialogue(ValidationMessages.LOGIN_SUCCESSFUL.msg, ValidationMessages.LOGIN_SUCCESSFUL.msgDetails, true) {
                Logger.i(TAG, "loginActionCb::onSuccess", "logged in successfully", ModuleNames.AUTH.value)

                if (viewModel.isUserEmailVerified()) {
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    NavigationHelper.navigateToActivity(requireContext(), intent)
                    requireActivity().finish()
                } else {
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
        }

        override fun onError(msg: String?, msgDetails: String?) {
            Logger.e(TAG, "loginActionCb::onError", "error msg: $msg error details: $msgDetails", ModuleNames.AUTH.value)

            loginAlertDialog?.showAlertProgress(false)
            loginAlertDialog?.showAlertDialogue(msg, msgDetails, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[UserLoginViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        binding = FragmentUserLoginBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        loginAlertDialog = AuthAlertDialog(requireContext())
        binding.containerView.setOnClickListener {
            hideKeyboard(requireActivity())
        }

        passwordToggleButtonAction()
        forgotPasswordButtonAction()
        signInButtonAction()
        registrationButtonAction()
    }

    private fun passwordToggleButtonAction() {
        Logger.d(TAG, "passwordToggleButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.passwordToggle.setOnClickListener {
            Logger.i(TAG, "passwordToggleButtonAction", "inputType: ${binding.passwordTextField.inputType}", ModuleNames.AUTH.value)

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

    private fun forgotPasswordButtonAction() {
        Logger.d(TAG, "forgotPasswordButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.forgotPassword.setOnClickListener {
            Logger.i(TAG, "forgotPasswordButtonAction", "forgot password button pressed", moduleName = ModuleNames.AUTH.value)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, ForgotPasswordFragment())
                .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                .addToBackStack(AuthFragmentNames.FORGOT_PASSWORD.fragment)
                .commit()
        }
    }

    private fun signInButtonAction() {
        Logger.d(TAG, "signInButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.signInButton.setOnClickListener {
            Logger.i(TAG, "signInButtonAction", "sign in button is clicked", ModuleNames.AUTH.value)
            loginAlertDialog?.showAlertProgress(true)
            viewModel.signInButtonAction(loginActionCb)
        }
    }

    private fun registrationButtonAction() {
        Logger.d(TAG, "registrationButtonAction", moduleName = ModuleNames.AUTH.value)

        binding.registerButton.setOnClickListener {
            Logger.i(TAG, "registrationButtonAction", "registration button is clicked", ModuleNames.AUTH.value)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, UserRegistrationFragment())
                .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)
                .addToBackStack(AuthFragmentNames.REGISTRATION.fragment)
                .commit()
        }
    }

    companion object {
        private const val TAG = "UserLoginFragment"
    }
}