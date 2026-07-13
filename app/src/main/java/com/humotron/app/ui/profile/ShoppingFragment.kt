package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentShoppingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingFragment : BaseFragment(R.layout.fragment_shopping) {

    private lateinit var binding: FragmentShoppingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShoppingBinding.bind(view)

        // Header Title
        binding.header.title.text = "Shopping"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnApplyCoupon.setOnClickListener {
            Toast.makeText(context, "Apply coupon clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddVat.setOnClickListener {
            Toast.makeText(context, "Add VAT number clicked", Toast.LENGTH_SHORT).show()
        }

        binding.switchEmailInvoices.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "Enabled" else "Disabled"
            Toast.makeText(context, "Email invoices $status", Toast.LENGTH_SHORT).show()
        }
    }
}
