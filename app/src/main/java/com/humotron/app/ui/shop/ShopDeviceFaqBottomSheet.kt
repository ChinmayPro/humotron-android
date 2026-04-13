package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.databinding.LayoutBottomSheetFaqBinding
import com.humotron.app.domain.modal.response.DeviceFaqResponse
import com.humotron.app.ui.shop.adapter.ShopFaqAdapter
import java.util.ArrayList

class ShopDeviceFaqBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetFaqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure the bottom sheet is expanded by default to see the contents properly
        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        val productName = arguments?.getString(ARG_PRODUCT_NAME) ?: ""
        val faqs = arguments?.getParcelableArrayList<DeviceFaqResponse.FaqData>(ARG_FAQS) ?: emptyList()

        binding.tvProductName.text = productName

        val adapter = ShopFaqAdapter(faqs)
        binding.rvFaqs.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_FAQS = "faqs"

        fun newInstance(productName: String, faqs: List<DeviceFaqResponse.FaqData>): ShopDeviceFaqBottomSheet {
            val fragment = ShopDeviceFaqBottomSheet()
            val args = Bundle()
            args.putString(ARG_PRODUCT_NAME, productName)
            args.putParcelableArrayList(ARG_FAQS, ArrayList(faqs))
            fragment.arguments = args
            return fragment
        }
    }
}
