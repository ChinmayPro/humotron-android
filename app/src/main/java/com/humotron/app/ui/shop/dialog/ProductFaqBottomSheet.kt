package com.humotron.app.ui.shop.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.ItemProductFaqBinding
import com.humotron.app.databinding.LayoutBottomsheetProductFaqBinding
import com.humotron.app.domain.modal.response.ProductDetailResponse

class ProductFaqBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomsheetProductFaqBinding? = null
    private val binding get() = _binding!!

    private var productName: String? = null
    private var faqList: List<ProductDetailResponse.ProductFaq>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productName = arguments?.getString(ARG_PRODUCT_NAME)
        faqList = arguments?.getParcelableArrayList(ARG_FAQ_LIST)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomsheetProductFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            
            bottomSheet?.let { sheet ->
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.isFitToContents = false
                behavior.expandedOffset = 0
                behavior.skipCollapsed = true
                behavior.isDraggable = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            dismiss()
        }

        binding.tvProductName.text = productName
        
        faqList?.let {
            binding.rvFaqs.adapter = FaqAdapter(it)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(it)
            behavior.isFitToContents = false
            behavior.peekHeight = resources.displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class FaqAdapter(private val items: List<ProductDetailResponse.ProductFaq>) :
        RecyclerView.Adapter<FaqAdapter.ViewHolder>() {

        private var expandedPosition = -1

        inner class ViewHolder(val binding: ItemProductFaqBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemProductFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val isExpanded = position == expandedPosition

            holder.binding.tvQuestion.text = "Q${position + 1}. ${item.question}"
            holder.binding.tvAnswer.text = "A${position + 1}. ${item.answer}"
            
            holder.binding.tvAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.binding.ivArrow.rotation = if (isExpanded) 90f else 0f

            holder.binding.clQuestionHeader.setOnClickListener {
                val prevExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else position
                
                if (prevExpanded != -1) notifyItemChanged(prevExpanded)
                if (expandedPosition != -1) notifyItemChanged(expandedPosition)
            }
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        private const val ARG_PRODUCT_NAME = "arg_product_name"
        private const val ARG_FAQ_LIST = "arg_faq_list"

        fun newInstance(productName: String, faqList: List<ProductDetailResponse.ProductFaq>): ProductFaqBottomSheet {
            val fragment = ProductFaqBottomSheet()
            val args = Bundle()
            args.putString(ARG_PRODUCT_NAME, productName)
            args.putParcelableArrayList(ARG_FAQ_LIST, ArrayList(faqList))
            fragment.arguments = args
            return fragment
        }
    }
}
