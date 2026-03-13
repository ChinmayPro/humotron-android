package com.humotron.app.ui.onboarding.personalize

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentPersonalInfoBinding
import com.humotron.app.domain.modal.param.SubmitPersonalInfoParam
import com.shawnlin.numberpicker.NumberPicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class PersonalInfoFragment : BaseFragment(R.layout.fragment_personal_info) {

    private lateinit var binding: FragmentPersonalInfoBinding
    private var selectedDate: Calendar = Calendar.getInstance()
    private val viewModel: OnboardingViewModel by viewModels()
    private val pagerViewModel: PagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPersonalInfoBinding.bind(view)

        setClicks()
    }

    private fun setClicks() {
        binding.etBirthday.setOnClickListener {
            showDatePicker()
        }

        binding.etGender.setOnClickListener {
            showGender {
                binding.etGender.setText(it)
            }
        }

        binding.btnSubmit.setOnClickListener {
            viewModel.submitPersonalInfo(
                SubmitPersonalInfoParam(
                    binding.etName.text.toString().trim(),
                    binding.etBirthday.text.toString().trim(),
                    binding.etGender.text.toString().trim()
                )
            )
        }
        subscriberToObserver()
    }

    private fun subscriberToObserver() {
        viewModel.onBoardingData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = networkStatus.data ?: return@observe
                    data.data?.user?.let { prefUtils.setLoginResponse(it) }
                    pagerViewModel.moveToPage(1)

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

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, pickedYear, pickedMonth, pickedDay ->
                selectedDate.set(pickedYear, pickedMonth, pickedDay)

                val formattedDate =
                    String.format(
                        Locale.UK,
                        "%04d-%02d-%02d",
                        pickedYear,
                        pickedMonth + 1,
                        pickedDay
                    )
                binding.etBirthday.setText(formattedDate)

            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showGender(onGenderSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val units = arrayOf("Male", "Female")

        numberPicker.minValue = 0
        numberPicker.maxValue = units.size - 1
        numberPicker.displayedValues = units
        numberPicker.wrapSelectorWheel = true

        var selectedUnit = units[numberPicker.value]
        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedUnit = units[newVal]
        }

        btnDone.setOnClickListener {
            onGenderSelected(selectedUnit)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


}