package com.humotron.app.ui.support

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentContactSupportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactSupportFragment : BaseFragment(R.layout.fragment_contact_support) {

    private lateinit var binding: FragmentContactSupportBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactSupportBinding.bind(view)

        binding.header.title.text = "Contact Support"
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            val subject = binding.etSubject.text?.toString()?.trim() ?: ""
            val message = binding.etMessage.text?.toString()?.trim() ?: ""

            if (subject.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter details for your request", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Support request submitted successfully", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }
}
