package com.exoleviathan.mychat.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.viewmodel.EmailAuthenticationViewModel
import com.exoleviathan.mychat.databinding.FragmentEmailAuthenticationBinding
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.NavigationHelper

class EmailAuthenticationFragment : Fragment() {
    private lateinit var binding: FragmentEmailAuthenticationBinding
    private lateinit var viewModel: EmailAuthenticationViewModel
    private var emailAuthenticationAlert: AuthAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[EmailAuthenticationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        emailAuthenticationAlert = AuthAlertDialog(requireContext())
        binding = FragmentEmailAuthenticationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        binding.continueButton.setOnClickListener {
            emailAuthenticationAlert?.showAlertProgress(true)
            viewModel.reloadUser {
                if (it) {
                    emailAuthenticationAlert?.showAlertProgress(false)
                    if (viewModel.isUserEmailVerified()) {
                        Logger.i(TAG, "onViewCreated", "user email is verified", ModuleNames.AUTH.value)
                        emailAuthenticationAlert?.dismissAlert()
                        val intent = Intent(requireContext(), HomeActivity::class.java)
                        NavigationHelper.navigateToActivity(requireContext(), intent)
                        requireActivity().finish()
                    } else {
                        Logger.i(TAG, "onViewCreated", "user email is not verified yet", ModuleNames.AUTH.value)
                        emailAuthenticationAlert?.showAlertDialogue(ValidationMessages.EMAIL_VERIFICATION_NEEDED.msg, ValidationMessages.EMAIL_VERIFICATION_NEEDED.msgDetails, false)
                    }
                } else {
                    emailAuthenticationAlert?.showAlertProgress(false)
                    emailAuthenticationAlert?.showAlertDialogue(ValidationMessages.RELOAD_FAILED.msg, ValidationMessages.RELOAD_FAILED.msgDetails, false)
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailAuthenticationFragment"
    }
}