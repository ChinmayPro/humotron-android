package com.humotron.app.ui.onboarding.personalize

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWidthHeightBinding
import com.humotron.app.domain.modal.param.WeightHeightParam
import com.shawnlin.numberpicker.NumberPicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidthHeightFragment : BaseFragment(R.layout.fragment_width_height) {

    private lateinit var binding: FragmentWidthHeightBinding
    private val viewModel: OnboardingViewModel by viewModels()
    private val pagerViewModel: PagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWidthHeightBinding.bind(view)

        binding.apply {
            tvTitle.text = getString(R.string.nice_to_meet_you, prefUtils.getLoginResponse().let {
                it.firstName.takeIf { name -> !name.isNullOrEmpty() } ?: it.name?.split(" ")
                    ?.firstOrNull() ?: ""
            })
            etHeight.setOnClickListener {
                showUnitPickerBottomSheet()
            }

            etWeight.setOnClickListener {
                showWeightPickerBottomSheet()
            }

            btnSubmit.setOnClickListener {
                if (validateInputs()) {
                    viewModel.submitWeightHeight(
                        WeightHeightParam(
                            binding.etHeight.text.toString().trim(),
                            binding.etHeightSelect.text.toString().trim(),
                            binding.etWeight.text.toString().trim(),
                            binding.etWeightSelect.text.toString().trim(),

                            )
                    )
                }
            }

        }

        binding.tvHeightInfo.text = "50 cm - 272 cm"
        binding.tvWeightInfo.text = "2 kg - 365 kg"
        binding.etHeight.setText("cm")
        binding.etWeight.setText("kg")
        subscriberToObserver()
    }

    private fun subscriberToObserver() {
        viewModel.onBoardingData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    data.data?.user?.let { prefUtils.setLoginResponse(it) }
                    pagerViewModel.moveToPage(2)

                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun showUnitPickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val units = arrayOf("cm", "ft in", "in", "m", "mm")

        numberPicker.minValue = 0
        numberPicker.maxValue = units.size - 1
        numberPicker.displayedValues = units

        var selectedUnit = units[numberPicker.value]

        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedUnit = units[newVal]
        }

        btnDone.setOnClickListener {
            setInfoText(selectedUnit)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun setInfoText(selectedUnit: String) {
        binding.etHeightSelect.setText("")
        binding.etHeight.setText(selectedUnit)
        binding.tvHeightInfo.text = when (selectedUnit) {
            "cm" -> "50 cm - 272 cm"
            "ft in" -> "1'6\" - 8'11\""
            "in" -> "19.7 in - 107.9 in"
            "m" -> "0.5 m - 2.72 m"
            "mm" -> "500 mm - 2720 mm"
            else -> ""
        }
        if (selectedUnit == "ft in") {
            binding.etHeightSelect.isFocusableInTouchMode = false
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_et_arrow_down)
            binding.etHeightSelect.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                drawable,
                null
            )
            binding.etHeightSelect.setOnClickListener {
                showFtInchBottomSheet()
            }
        } else {
            binding.etHeightSelect.isFocusableInTouchMode = true
            binding.etHeightSelect.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.etHeightSelect.setOnClickListener(null)
        }
    }

    private fun showWeightPickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val units = arrayOf("kg", "lbs", "st lbs")

        numberPicker.minValue = 0
        numberPicker.maxValue = units.size - 1
        numberPicker.displayedValues = units

        var selectedUnit = units[numberPicker.value]
        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedUnit = units[newVal]
        }

        btnDone.setOnClickListener {
            setWeightInfoText(selectedUnit)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun setWeightInfoText(selectedUnit: String) {
        binding.etWeightSelect.setText("")
        binding.etWeight.setText(selectedUnit)
        binding.tvWeightInfo.text = when (selectedUnit) {
            "kg" -> "2 kg - 365 kg"
            "lbs" -> "4.4 lbs - 1400.0 lbs"
            "st lbs" -> "0 st 4 lbs - 100 st 0 lbs"
            else -> ""
        }

        if (selectedUnit == "st lbs") {
            binding.etWeightSelect.isFocusableInTouchMode = false
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_et_arrow_down)
            binding.etWeightSelect.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                drawable,
                null
            )
            binding.etWeightSelect.setOnClickListener {
                showStLbsBottomSheet()
            }
        } else {
            binding.etWeightSelect.isFocusableInTouchMode = true
            binding.etWeightSelect.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showFtInchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_value_picker, null)
        bottomSheetDialog.setContentView(view)

        val firstPicker = view.findViewById<NumberPicker>(R.id.firstPicker)
        val secondPicker = view.findViewById<NumberPicker>(R.id.secondPicker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val ft = arrayOf("1'", "2'", "3'", "4'", "5'", "6'", "7'", "8'")
        val inch = arrayOf(
            "1\"",
            "2\"",
            "3\"",
            "4\"",
            "5\"",
            "6\"",
            "7\"",
            "8\"",
            "9\"",
            "10\"",
            "11\"",
            "12\""
        )

        firstPicker.minValue = 0
        firstPicker.maxValue = ft.size - 1
        firstPicker.displayedValues = ft

        var selectedFt = ft[firstPicker.value]

        firstPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedFt = ft[newVal]
        }

        var selectedInch = inch[secondPicker.value]
        secondPicker.minValue = 0
        secondPicker.maxValue = inch.size - 1
        secondPicker.displayedValues = inch

        secondPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedInch = inch[newVal]
        }

        btnDone.setOnClickListener {
            binding.etHeightSelect.setText("$selectedFt $selectedInch")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun showStLbsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_value_picker, null)
        bottomSheetDialog.setContentView(view)

        val firstPicker = view.findViewById<NumberPicker>(R.id.firstPicker)
        val secondPicker = view.findViewById<NumberPicker>(R.id.secondPicker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val st = arrayListOf<String>()
        for (item in 0..100) {
            st.add("$item st")
        }

        val lbs = arrayListOf<String>()
        for (item in 0..13) {
            lbs.add("$item lb")
        }


        firstPicker.minValue = 0
        firstPicker.maxValue = st.size - 1
        firstPicker.displayedValues = st.toTypedArray()

        var selectedFt = st[firstPicker.value]

        firstPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            if (newVal == 0) {
                secondPicker.value = 4
            } else if (newVal == st.size - 1) {
                secondPicker.value = 0
            }
            selectedFt = st[newVal]
        }

        var selectedInch = lbs[secondPicker.value]
        secondPicker.minValue = 0
        secondPicker.maxValue = lbs.size - 1
        secondPicker.displayedValues = lbs.toTypedArray()

        secondPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            if (firstPicker.value == 0) {
                if (newVal < 4) {
                    picker.value = 4
                }
            } else if (firstPicker.value == st.size - 1) {
                if (newVal > 0) {
                    picker.value = 0
                }
            }
            selectedInch = lbs[newVal]
        }

        btnDone.setOnClickListener {
            binding.etWeightSelect.setText("$selectedFt $selectedInch")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun validateInputs(): Boolean {
        val heightUnit = binding.etHeight.text.toString()
        val heightValue = binding.etHeightSelect.text.toString().trim()
        val weightUnit = binding.etWeight.text.toString()
        val weightValue = binding.etWeightSelect.text.toString().trim()

        // Height validation
        val isHeightValid = when (heightUnit) {
            "cm" -> heightValue.toFloatOrNull()?.let { it in 50f..272f } ?: false
            "m" -> heightValue.toFloatOrNull()?.let { it in 0.5f..2.72f } ?: false
            "mm" -> heightValue.toFloatOrNull()?.let { it in 500f..2720f } ?: false
            "in" -> heightValue.toFloatOrNull()?.let { it in 19.7f..107.9f } ?: false
            "ft in" -> {
                val parts = heightValue.split("'").map { it.trim().replace("\"", "") }
                if (parts.size == 2) {
                    val feet = parts[0].toIntOrNull() ?: return false
                    val inches = parts[1].toIntOrNull() ?: return false
                    val totalInches = feet * 12 + inches
                    totalInches in 18..107 // approx 1'6" to 8'11"
                } else false
            }

            else -> false
        }

        if (!isHeightValid) {
            Toast.makeText(requireContext(), "Please enter correct Height", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        // Weight validation
        val isWeightValid = when (weightUnit) {
            "kg" -> weightValue.toFloatOrNull()?.let { it in 2f..365f } ?: false
            "lbs" -> weightValue.toFloatOrNull()?.let { it in 4.4f..1400f } ?: false
            "st lbs" -> {
                val parts = weightValue.split("st").map { it.trim() }
                if (parts.size == 2) {
                    val st = parts[0].toIntOrNull() ?: return false
                    val lbs = parts[1].replace("lbs", "").trim().toIntOrNull() ?: return false
                    val totalLbs = st * 14 + lbs
                    totalLbs in 4..1400 // approx 0st 4lbs to 100st
                } else false
            }

            else -> false
        }

        if (!isWeightValid) {
            Toast.makeText(requireContext(), "Please enter correct Weight", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }


}