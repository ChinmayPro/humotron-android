package com.humotron.app.ui.decode

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.FragmentDecodeChatMessageDetailsBinding
import com.humotron.app.domain.modal.response.ConversationData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeChatMessageDetailsFragment : DialogFragment() {

    private var _binding: FragmentDecodeChatMessageDetailsBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecodeChatMessageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val item = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_CONVERSATION, ConversationData::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_CONVERSATION)
        }
        
        item?.let { setupUi(it) }
        
        binding.ivBack.setOnClickListener {
            dismiss()
        }
        
        binding.viewDismiss.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels
            
            val bottomNavHeight = (42 * resources.displayMetrics.density).toInt()
            
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                screenHeight - bottomNavHeight
            )
            setGravity(Gravity.TOP)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            // Allow covering status bar
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            
            // Set status bar icons to white
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightStatusBars = false
            }
            
            setWindowAnimations(R.style.FragmentOpenAnimation)
        }
    }

    private fun setupUi(item: ConversationData) {
        binding.tvUserMsg.text = item.userMessage
//        binding.tvAiResponse.text = item.botResponse?.message
        binding.tvDate.text = formatToTime(item.createdAt)
    }

    private fun formatToTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        val inputFormats = arrayOf(
            "MMM dd, yyyy h:mm a",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (format in inputFormats) {
            try {
                val input = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                if (format.endsWith("'Z'")) input.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = input.parse(dateStr)
                if (date != null) {
                    return java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)
                }
            } catch (e: Exception) { }
        }
        return dateStr
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONVERSATION = "arg_conversation"

        fun newInstance(item: ConversationData): DecodeChatMessageDetailsFragment {
            return DecodeChatMessageDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONVERSATION, item)
                }
            }
        }
    }
}
