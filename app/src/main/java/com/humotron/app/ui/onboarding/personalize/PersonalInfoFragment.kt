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
    private var apiBirthDate: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPersonalInfoBinding.bind(view)

        prefillUserInfo()
        setClicks()
    }

    private fun prefillUserInfo() {
        val user = prefUtils.getLoginResponse()

        // Prefill Name
        if (!user.firstName.isNullOrEmpty()) {
            binding.etFirstName.setText(user.firstName)
        } else if (!user.name.isNullOrEmpty()) {
            val parts = user.name.split(" ")
            binding.etFirstName.setText(parts.firstOrNull() ?: "")
        }

        if (!user.lastName.isNullOrEmpty()) {
            binding.etLastName.setText(user.lastName)
        } else if (!user.name.isNullOrEmpty()) {
            val parts = user.name.split(" ")
            if (parts.size > 1) {
                binding.etLastName.setText(parts.subList(1, parts.size).joinToString(" "))
            }
        }

        // Prefill Gender
        if (!user.gender.isNullOrEmpty()) {
            val displayGender = user.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            binding.etGender.setText(displayGender)
        }

        // Prefill Birthdate
        if (!user.birthDate.isNullOrEmpty()) {
            apiBirthDate = user.birthDate
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", Locale.UK)
                val date = inputFormat.parse(user.birthDate)
                if (date != null) {
                    binding.etBirthday.setText(outputFormat.format(date))
                    selectedDate.time = date
                }
            } catch (e: Exception) {
                binding.etBirthday.setText(user.birthDate)
            }
        }

        // Prefill Country (default: United Kingdom)
        binding.etCountry.setText("United Kingdom")
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

        binding.etCountry.setOnClickListener {
            showCountry {
                binding.etCountry.setText(it)
            }
        }

        binding.btnSubmit.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            if (firstName.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please Enter First Name", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lastName.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please Enter Last Name", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (apiBirthDate.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please select birth date", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val today = java.util.Calendar.getInstance()
            var age = today.get(java.util.Calendar.YEAR) - selectedDate.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < selectedDate.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--
            }
            if (age < 18) {
                android.widget.Toast.makeText(requireContext(), "You must be at least 18", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.etGender.text.toString().trim().isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please select gender", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitPersonalInfo(
                SubmitPersonalInfoParam(
                    "$firstName $lastName".trim(),
                    apiBirthDate,
                    binding.etGender.text.toString().trim().lowercase(Locale.ROOT)
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

                // API format: yyyy-MM-dd
                apiBirthDate = String.format(
                    Locale.UK,
                    "%04d-%02d-%02d",
                    pickedYear,
                    pickedMonth + 1,
                    pickedDay
                )

                // Display format: d MMM yyyy (e.g. 24 Aug 1984)
                val outputFormat = java.text.SimpleDateFormat("d MMM yyyy", Locale.UK)
                binding.etBirthday.setText(outputFormat.format(selectedDate.time))

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

        val currentGender = binding.etGender.text.toString()
        val index = units.indexOf(currentGender)
        if (index >= 0) {
            numberPicker.value = index
        }

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

    private fun showCountry(onCountrySelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val countries = arrayOf("United Kingdom", "United States", "Germany", "France", "Canada", "Australia", "India")

        numberPicker.minValue = 0
        numberPicker.maxValue = countries.size - 1
        numberPicker.displayedValues = countries
        numberPicker.wrapSelectorWheel = true

        val currentCountry = binding.etCountry.text.toString()
        val index = countries.indexOf(currentCountry)
        if (index >= 0) {
            numberPicker.value = index
        }

        var selectedCountry = countries[numberPicker.value]
        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            selectedCountry = countries[newVal]
        }

        btnDone.setOnClickListener {
            onCountrySelected(selectedCountry)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

}