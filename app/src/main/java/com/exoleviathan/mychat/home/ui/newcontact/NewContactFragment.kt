package com.exoleviathan.mychat.home.ui.newcontact

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.exoleviathan.mychat.databinding.FragmentNewContactBinding
import com.exoleviathan.mychat.home.ui.HomeActivity
import com.exoleviathan.mychat.home.viewmodel.NewContactViewModel
import com.exoleviathan.mychat.utility.Logger
import com.exoleviathan.mychat.utility.ModuleNames
import com.exoleviathan.mychat.utility.hideKeyboard

class NewContactFragment : Fragment() {
    private lateinit var binding: FragmentNewContactBinding
    private lateinit var viewModel: NewContactViewModel

    @SuppressLint("NotifyDataSetChanged")
    private val updatedContactListObserver = Observer<ArrayList<Pair<String, String>>> {
        Logger.i(TAG, "updatedContactListObserver::observe", "item size: ${it.size}")

        if (it.size == 0) {
            binding.contactsLayout.visibility = View.GONE
        } else {
            binding.contactsLayout.visibility = View.VISIBLE
            binding.contactsRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

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

        binding.addNewContact.setOnClickListener {
            binding.addUserLayout.visibility = View.VISIBLE
        }

        binding.addUserButton.setOnClickListener {
            hideKeyboard(requireActivity())
            binding.addUserButton.isEnabled = false

            viewModel.addNewUser { res, msg ->
                binding.addUserButton.isEnabled = true
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                if (res) {
                    binding.addUserLayout.visibility = View.GONE
                }
            }
        }

        binding.contactsRecyclerView.adapter = ContactsListAdapter(viewModel)
        binding.contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        (requireActivity() as? HomeActivity)?.customizeToolbar("Add new contact", true)
        binding.contactsRecyclerView.adapter?.notifyDataSetChanged()
        viewModel.updatedFriendList.observe(requireActivity(), updatedContactListObserver)
    }

    override fun onPause() {
        super.onPause()

        viewModel.updatedFriendList.removeObserver(updatedContactListObserver)
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.deInit()
    }

    companion object {
        private const val TAG = "NewContactFragment"
    }
}