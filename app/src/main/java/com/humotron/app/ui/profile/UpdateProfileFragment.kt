package com.humotron.app.ui.profile

import android.app.DatePickerDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentUpdateProfileBinding
import com.shawnlin.numberpicker.NumberPicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class UpdateProfileFragment : BaseFragment(R.layout.fragment_update_profile) {

    private lateinit var binding: FragmentUpdateProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    private var selectedGender: String = "Male"
    private var selectedDate: Calendar = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
    private var apiBirthDate: String = ""
    private var selectedCountry: String = "UK"
    private var selectedHeightUnit: String = "ft in"
    private var selectedHeightValue: String = "6' 1\""
    private var selectedWeightUnit: String = "kg"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUpdateProfileBinding.bind(view)

        prefillUserData()
        setupKeyboardInsetsHandling()
        setupListeners()
        observeViewModel()
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    @Suppress("DEPRECATION")
    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun setupKeyboardInsetsHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomPadding = if (imeHeight > 0) imeHeight else systemBars.bottom

            binding.nestedScrollView.setPadding(
                binding.nestedScrollView.paddingLeft,
                binding.nestedScrollView.paddingTop,
                binding.nestedScrollView.paddingRight,
                bottomPadding + 32
            )

            if (imeHeight > 0) {
                binding.nestedScrollView.postDelayed({
                    val focusedView = binding.root.findFocus()
                    if (focusedView != null) {
                        val rect = Rect()
                        focusedView.getDrawingRect(rect)
                        binding.nestedScrollView.offsetDescendantRectToMyCoords(focusedView, rect)
                        binding.nestedScrollView.smoothScrollTo(0, rect.bottom + 140)
                    }
                }, 100)
            }

            insets
        }

        val focusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.nestedScrollView.postDelayed({
                    val rect = Rect()
                    v.getDrawingRect(rect)
                    binding.nestedScrollView.offsetDescendantRectToMyCoords(v, rect)
                    binding.nestedScrollView.smoothScrollTo(0, rect.bottom + 160)
                }, 150)
            }
        }
        binding.etFirstName.onFocusChangeListener = focusListener
        binding.etLastName.onFocusChangeListener = focusListener
        binding.etWeightValue.onFocusChangeListener = focusListener
    }

    private fun prefillUserData() {
        val user = prefUtils.getLoginResponse()

        // Name
        val firstName = user.firstName?.takeIf { it.isNotBlank() } ?: user.name?.split(" ")?.firstOrNull() ?: "Chinmay"
        val lastName = user.lastName?.takeIf { it.isNotBlank() } ?: user.name?.split(" ")?.drop(1)?.joinToString(" ") ?: "Bhatt"

        binding.etFirstName.setText(firstName)
        binding.etLastName.setText(lastName)

        // Date of Birth
        if (!user.birthDate.isNullOrEmpty()) {
            apiBirthDate = user.birthDate
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.UK)
                val date = inputFormat.parse(user.birthDate)
                if (date != null) {
                    binding.tvDateOfBirth.text = outputFormat.format(date)
                    selectedDate.time = date
                } else {
                    binding.tvDateOfBirth.text = user.birthDate
                }
            } catch (e: Exception) {
                binding.tvDateOfBirth.text = user.birthDate
            }
        } else {
            binding.tvDateOfBirth.text = "24 Sep 1983"
            apiBirthDate = "1983-09-24"
        }

        // Gender
        val gender = user.gender?.takeIf { it.isNotBlank() } ?: "Male"
        updateGenderUI(gender)

        // Country
        selectedCountry = "UK"
        binding.tvCountry.text = selectedCountry

        // Height & Weight
        val userHeight = user.height
        if (!userHeight.isNullOrBlank()) {
            if (userHeight.contains("cm", ignoreCase = true)) {
                selectedHeightUnit = "cm"
                selectedHeightValue = userHeight.replace("cm", "", ignoreCase = true).trim()
            } else {
                selectedHeightUnit = "ft in"
                selectedHeightValue = userHeight
            }
        }
        binding.tvHeightUnit.text = selectedHeightUnit
        binding.tvHeightValue.text = selectedHeightValue
        updateHeightRangeText()

        val userWeight = user.weight
        if (!userWeight.isNullOrBlank()) {
            val weightVal = userWeight.replace("kg", "", ignoreCase = true).replace("lbs", "", ignoreCase = true).trim()
            binding.etWeightValue.setText(weightVal)
            if (userWeight.contains("lbs", ignoreCase = true)) {
                selectedWeightUnit = "lbs"
            } else {
                selectedWeightUnit = "kg"
            }
        } else {
            binding.etWeightValue.setText("80")
            selectedWeightUnit = "kg"
        }
        binding.tvWeightUnit.text = selectedWeightUnit
        updateWeightRangeText()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDateOfBirth.setOnClickListener {
            showDatePicker()
        }

        binding.tvGenderMale.setOnClickListener { updateGenderUI("Male") }
        binding.tvGenderFemale.setOnClickListener { updateGenderUI("Female") }
        binding.tvGenderOther.setOnClickListener { updateGenderUI("Other") }

        binding.btnCountry.setOnClickListener {
            showCountryPicker()
        }

        binding.btnHeightUnit.setOnClickListener {
            showHeightUnitPicker()
        }

        binding.btnHeightValue.setOnClickListener {
            showHeightValuePicker()
        }

        binding.btnWeightUnit.setOnClickListener {
            showWeightUnitPicker()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun updateGenderUI(gender: String) {
        selectedGender = gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        // Clear all
        binding.tvGenderMale.background = null
        binding.tvGenderMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextGrey))
        binding.tvGenderFemale.background = null
        binding.tvGenderFemale.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextGrey))
        binding.tvGenderOther.background = null
        binding.tvGenderOther.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextGrey))

        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_gender_active)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.colorBgBtn)

        when (selectedGender.lowercase(Locale.ROOT)) {
            "male" -> {
                binding.tvGenderMale.background = activeBg
                binding.tvGenderMale.setTextColor(activeTextColor)
            }
            "female" -> {
                binding.tvGenderFemale.background = activeBg
                binding.tvGenderFemale.setTextColor(activeTextColor)
            }
            else -> {
                binding.tvGenderOther.background = activeBg
                binding.tvGenderOther.setTextColor(activeTextColor)
            }
        }
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(requireContext(), { _, y, m, d ->
            selectedDate.set(y, m, d)
            val displayFormat = SimpleDateFormat("d MMM yyyy", Locale.UK)
            val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
            binding.tvDateOfBirth.text = displayFormat.format(selectedDate.time)
            apiBirthDate = apiFormat.format(selectedDate.time)
        }, year, month, day)

        val maxCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        dialog.datePicker.maxDate = maxCalendar.timeInMillis

        dialog.show()
    }

    private fun showCountryPicker() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val countries = arrayOf("UK", "United States", "Canada", "Australia", "India", "Germany")

        numberPicker.minValue = 0
        numberPicker.maxValue = countries.size - 1
        numberPicker.displayedValues = countries
        numberPicker.wrapSelectorWheel = false

        val index = countries.indexOf(selectedCountry)
        if (index >= 0) {
            numberPicker.value = index
        }

        var chosen = countries[numberPicker.value]
        numberPicker.setOnValueChangedListener { _, _, newVal ->
            chosen = countries[newVal]
        }

        btnDone.setOnClickListener {
            selectedCountry = chosen
            binding.tvCountry.text = selectedCountry
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showHeightUnitPicker() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val units = arrayOf("ft in", "cm")

        numberPicker.minValue = 0
        numberPicker.maxValue = units.size - 1
        numberPicker.displayedValues = units
        numberPicker.wrapSelectorWheel = false

        val index = units.indexOf(selectedHeightUnit)
        if (index >= 0) {
            numberPicker.value = index
        }

        var chosen = units[numberPicker.value]
        numberPicker.setOnValueChangedListener { _, _, newVal ->
            chosen = units[newVal]
        }

        btnDone.setOnClickListener {
            selectedHeightUnit = chosen
            binding.tvHeightUnit.text = selectedHeightUnit
            if (selectedHeightUnit == "cm") {
                selectedHeightValue = "180 cm"
            } else {
                selectedHeightValue = "6' 1\""
            }
            binding.tvHeightValue.text = selectedHeightValue
            updateHeightRangeText()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showHeightValuePicker() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        if (selectedHeightUnit == "ft in") {
            val view = layoutInflater.inflate(R.layout.bottomsheet_unit_value_picker, null)
            bottomSheetDialog.setContentView(view)

            val firstPicker = view.findViewById<NumberPicker>(R.id.firstPicker)
            val secondPicker = view.findViewById<NumberPicker>(R.id.secondPicker)
            val btnDone = view.findViewById<TextView>(R.id.btnDone)

            val ft = arrayOf("1'", "2'", "3'", "4'", "5'", "6'", "7'", "8'")
            val inch = arrayOf("0\"", "1\"", "2\"", "3\"", "4\"", "5\"", "6\"", "7\"", "8\"", "9\"", "10\"", "11\"")

            firstPicker.minValue = 0
            firstPicker.maxValue = ft.size - 1
            firstPicker.displayedValues = ft
            firstPicker.value = 5 // 6'

            secondPicker.minValue = 0
            secondPicker.maxValue = inch.size - 1
            secondPicker.displayedValues = inch
            secondPicker.value = 1 // 1"

            btnDone.setOnClickListener {
                val f = ft[firstPicker.value]
                val i = inch[secondPicker.value]
                selectedHeightValue = "$f $i"
                binding.tvHeightValue.text = selectedHeightValue
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        } else {
            val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
            bottomSheetDialog.setContentView(view)

            val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
            val btnDone = view.findViewById<TextView>(R.id.btnDone)

            val heightsCm = (100..250).map { "$it cm" }.toTypedArray()

            numberPicker.minValue = 0
            numberPicker.maxValue = heightsCm.size - 1
            numberPicker.displayedValues = heightsCm
            numberPicker.wrapSelectorWheel = false

            val currentCm = selectedHeightValue.replace("cm", "").trim()
            val index = heightsCm.indexOfFirst { it.startsWith(currentCm) }
            if (index >= 0) {
                numberPicker.value = index
            } else {
                numberPicker.value = 80 // 180 cm
            }

            var chosen = heightsCm[numberPicker.value]
            numberPicker.setOnValueChangedListener { _, _, newVal ->
                chosen = heightsCm[newVal]
            }

            btnDone.setOnClickListener {
                selectedHeightValue = chosen
                binding.tvHeightValue.text = selectedHeightValue
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }
    }

    private fun updateHeightRangeText() {
        if (selectedHeightUnit == "cm") {
            binding.tvHeightRangeSublabel.text = "45 cm - 272 cm"
        } else {
            binding.tvHeightRangeSublabel.text = "1'6\" ft in - 8'11\" ft in"
        }
    }

    private fun showWeightUnitPicker() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_unit_picker, null)
        bottomSheetDialog.setContentView(view)

        val numberPicker = view.findViewById<NumberPicker>(R.id.number_picker)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)

        val units = arrayOf("kg", "lbs")

        numberPicker.minValue = 0
        numberPicker.maxValue = units.size - 1
        numberPicker.displayedValues = units
        numberPicker.wrapSelectorWheel = false

        val index = units.indexOf(selectedWeightUnit)
        if (index >= 0) {
            numberPicker.value = index
        }

        var chosen = units[numberPicker.value]
        numberPicker.setOnValueChangedListener { _, _, newVal ->
            chosen = units[newVal]
        }

        btnDone.setOnClickListener {
            selectedWeightUnit = chosen
            binding.tvWeightUnit.text = selectedWeightUnit
            updateWeightRangeText()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateWeightRangeText() {
        if (selectedWeightUnit == "lbs") {
            binding.tvWeightRangeSublabel.text = "4.4 lbs - 1400 lbs"
        } else {
            binding.tvWeightRangeSublabel.text = "2.0 kg - 635.0 kg"
        }
    }

    private fun saveChanges() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val weightVal = binding.etWeightValue.text.toString().trim()

        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter first name", Toast.LENGTH_SHORT).show()
            return
        }

        val user = prefUtils.getLoginResponse()
        val formattedHeight = selectedHeightValue.trim()
        val formattedWeight = "$weightVal $selectedWeightUnit".trim()

        val updatedUser = user.copy(
            firstName = firstName,
            lastName = lastName,
            name = "$firstName $lastName",
            gender = selectedGender,
            birthDate = if (apiBirthDate.isNotBlank()) apiBirthDate else user.birthDate,
            height = formattedHeight,
            weight = formattedWeight
        )
        prefUtils.setLoginResponse(updatedUser)

        val userId = updatedUser.id ?: ""
        if (userId.isNotBlank()) {
            val params = HashMap<String, Any>()
            params["firstName"] = firstName
            params["lastName"] = lastName
            params["name"] = "$firstName $lastName"
            params["gender"] = selectedGender
            if (apiBirthDate.isNotBlank()) {
                params["birthDate"] = apiBirthDate
            }
            params["height"] = formattedHeight
            params["weight"] = formattedWeight
            viewModel.updateUserById(userId, params)
        } else {
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.getUpdateUserLiveData().observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource.status) {
                Status.SUCCESS -> {
                    viewModel.clearUpdateUserLiveData()
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    val msg = resource.error?.errorMessage ?: "Failed to update profile"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
