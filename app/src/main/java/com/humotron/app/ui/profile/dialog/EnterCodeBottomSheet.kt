package com.humotron.app.ui.profile.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.humotron.app.R
import com.humotron.app.core.base.BaseBottomSheetDialogFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.DialogEnterCodeBinding
import com.humotron.app.domain.modal.response.GetCartResponse
import com.humotron.app.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterCodeBottomSheet : BaseBottomSheetDialogFragment() {

    private var _binding: DialogEnterCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by activityViewModels()

    private var title: String? = null
    private var hint: String? = null
    private var prefillText: String? = null
    private var isVat: Boolean = false
    private var onSave: ((String) -> Unit)? = null
    private var onCouponApplied: (() -> Unit)? = null

    override fun getTheme(): Int = R.style.IOSBottomSheetTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            hint = it.getString(ARG_HINT)
            prefillText = it.getString(ARG_PREFILL)
            isVat = it.getBoolean(ARG_IS_VAT, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEnterCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = title
        binding.etCode.hint = hint
        val prefill = prefillText
        if (!prefill.isNullOrEmpty()) {
            binding.etCode.setText(prefill)
            binding.etCode.setSelection(prefill.length)
        }

        binding.ivClose.setOnClickListener { dismiss() }

        viewModel.clearPromoCodeDetailsLiveData()
        binding.tvError.visibility = View.GONE

        binding.etCode.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvError.visibility = View.GONE
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.isNotEmpty()) {
                if (isVat) {
                    val userId = prefUtils.getLoginResponse().id ?: ""
                    val map = HashMap<String, Any>()
                    map["vatNumber"] = code
                    viewModel.updateUserById(userId, map)
                } else {
                    viewModel.getPromoCodeDetailsByPromoCode(code)
                }
            } else {
                binding.tvError.text = getString(R.string.cannot_be_empty)
                binding.tvError.visibility = View.VISIBLE
            }
        }

        initObservers()
    }

    private fun initObservers() {
        viewModel.getPromoCodeDetailsLiveData().observe(viewLifecycleOwner) { resource: com.humotron.app.data.network.Resource<com.humotron.app.domain.modal.response.PromoCodeDetailsResponse>? ->
            if (resource == null) return@observe
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val response = resource.data
                    val status = response?.status ?: ""
                    val apiMessage = response?.message ?: response?.error ?: getString(R.string.invalid_promo_code)
                    

                    
                    if (status.equals("fail", ignoreCase = true)) {
                        showError(apiMessage)
                    } else {
                        val promoCodeId = try {
                            val dataElement = response?.coupon ?: response?.data
                            if (dataElement != null && dataElement.isJsonObject) {
                                val couponObj = com.google.gson.Gson().fromJson(dataElement, GetCartResponse.CouponDetails::class.java)
                                couponObj.id
                            } else null
                        } catch (e: Exception) {
                            null
                        }

                        if (promoCodeId != null) {
                            val userId = prefUtils.getLoginResponse().id ?: ""
                            val map = HashMap<String, Any>()
                            map["couponId"] = promoCodeId
                            viewModel.updateUserById(userId, map)
                            viewModel.clearPromoCodeDetailsLiveData()
                        } else {
                            showError(apiMessage)
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    showError(resource.error?.errorMessage ?: getString(R.string.something_went_wrong))
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.getUpdateUserLiveData().observe(viewLifecycleOwner) { resource: com.humotron.app.data.network.Resource<com.humotron.app.domain.modal.response.CommonResponse>? ->
            if (resource == null) {
                hideProgress()
                return@observe
            }
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    binding.tvError.visibility = View.GONE
                    onSave?.invoke(binding.etCode.text.toString().trim())
                    if (!isVat) {
                        onCouponApplied?.invoke()
                    }
                    viewModel.fetchDefaultConfiguration()
                    viewModel.clearUpdateUserLiveData()
                    dismiss()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    showError(resource.error?.errorMessage ?: getString(R.string.something_went_wrong))
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EnterCodeBottomSheet"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_HINT = "arg_hint"
        private const val ARG_PREFILL = "arg_prefill"
        private const val ARG_IS_VAT = "arg_is_vat"

        fun forVat(currentVat: String?, onSave: (String) -> Unit): EnterCodeBottomSheet {
            val fragment = EnterCodeBottomSheet()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, "Enter VAT Number")
                putString(ARG_HINT, "VAT Number")
                putString(ARG_PREFILL, currentVat)
                putBoolean(ARG_IS_VAT, true)
            }
            fragment.onSave = onSave
            return fragment
        }

        fun forCoupon(currentCoupon: String?, onCouponApplied: () -> Unit, onSave: (String) -> Unit): EnterCodeBottomSheet {
            val fragment = EnterCodeBottomSheet()
            fragment.arguments = Bundle().apply {
                putString(ARG_TITLE, "Enter Coupon Code")
                putString(ARG_HINT, "Coupon Number")
                putString(ARG_PREFILL, currentCoupon)
                putBoolean(ARG_IS_VAT, false)
            }
            fragment.onCouponApplied = onCouponApplied
            fragment.onSave = onSave
            return fragment
        }
    }
}
