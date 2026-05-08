package com.humotron.app.ui.profile.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.BottomSheetSelectDeliveryBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.ui.profile.adapter.DeliveryMethodAdapter

class SelectDeliveryBottomSheet(
    private val currentMethodId: String?,
    private val deliveryOptions: List<GetCartResponse.DeliveryMethod>,
    private val onSave: (GetCartResponse.DeliveryMethod) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetSelectDeliveryBinding
    private var selectedMethod: GetCartResponse.DeliveryMethod? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.IOSBottomSheetTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetSelectDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = DeliveryMethodAdapter(currentMethodId) { method ->
            selectedMethod = method
        }
        
        binding.rvDeliveryMethods.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeliveryMethods.adapter = adapter
        adapter.setMethods(deliveryOptions, currentMethodId)
        
        // Find current method in the list to initialize selectedMethod
        selectedMethod = deliveryOptions.find { it.id == currentMethodId }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            selectedMethod?.let {
                onSave(it)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "SelectDeliveryBottomSheet"
    }
}
