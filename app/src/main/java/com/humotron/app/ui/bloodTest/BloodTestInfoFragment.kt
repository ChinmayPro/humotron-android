package com.humotron.app.ui.bloodTest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.view.animation.AnimationUtils
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBloodTestInfoBinding
import com.humotron.app.ui.bloodTest.dialog.ChooseUploadMethodBottomSheet

class BloodTestInfoFragment : Fragment() {

    private var _binding: FragmentBloodTestInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBloodTestInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        applyInsets()
        initClicks()
    }

    private fun setupStatusBar() {
        requireActivity().window.apply {
            statusBarColor = Color.BLACK
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun initClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnStartFetching.setOnClickListener {
            val bottomSheet = ChooseUploadMethodBottomSheet()
            bottomSheet.setMethodSelectionListener { key ->
                if (key == "email") {
                    findNavController().navigate(R.id.action_fragmentBloodTestInfo_to_fragmentBloodTestGmailInfo)
                }
            }
            bottomSheet.show(childFragmentManager, "choose_upload_method")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
