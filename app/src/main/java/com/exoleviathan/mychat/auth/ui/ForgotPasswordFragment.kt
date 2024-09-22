package com.exoleviathan.mychat.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.exoleviathan.mychat.auth.callbacks.IAuthActionCallback
import com.exoleviathan.mychat.auth.models.ValidationMessages
import com.exoleviathan.mychat.auth.viewmodel.ForgotPasswordViewModel
import com.exoleviathan.mychat.databinding.FragmentForgotPasswordBinding
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames

class ForgotPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel
    private var sendEmailLinkAlert: AuthAlertDialog? = null

    private var forgotPasswordCb = object : IAuthActionCallback {

        override fun onSuccess() {
            Logger.i(TAG, "forgotPasswordCb::onSuccess", "email sent successfully", ModuleNames.AUTH.value)

            sendEmailLinkAlert?.showAlertProgress(false)
            sendEmailLinkAlert?.showAlertDialogue(ValidationMessages.EMAIL_SEND_SUCCESS.msg, ValidationMessages.EMAIL_SEND_SUCCESS.msgDetails, true) {
                requireActivity().supportFragmentManager
                    .popBackStack()
            }
        }

        override fun onError(msg: String?, msgDetails: String?) {
            Logger.i(TAG, "forgotPasswordCb::onSuccess", "not send", ModuleNames.AUTH.value)

            sendEmailLinkAlert?.showAlertProgress(false)
            sendEmailLinkAlert?.showAlertDialogue(msg, msgDetails, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate", moduleName = ModuleNames.AUTH.value)

        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView", moduleName = ModuleNames.AUTH.value)

        sendEmailLinkAlert = AuthAlertDialog(requireContext())

        binding = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated", moduleName = ModuleNames.AUTH.value)

        binding.sendCode.setOnClickListener {
            sendEmailLinkAlert?.showAlertProgress(true)
            viewModel.sendResetPasswordEmail(forgotPasswordCb)
        }
    }

    companion object {
        private const val TAG = "ForgotPasswordFragment"
    }
}