package com.humotron.app.ui.profile

import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDataSourceDetailBinding
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataSourceDetailFragment : BaseFragment(R.layout.fragment_data_source_detail) {

    private lateinit var binding: FragmentDataSourceDetailBinding
    private val args: DataSourceDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDataSourceDetailBinding.bind(view)

        // Setup Header
        binding.header.title.text = args.sourceName
        binding.header.title.typeface = resources.getFont(R.font.manrope_bold)
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Source Info Card
        binding.tvDeviceName.text = args.sourceName
        binding.tvDeviceDesc.text = args.sourceDesc
        binding.ivDeviceIcon.setImageResource(args.sourceIcon)

        // Parse custom color and set opacity tint for icon box
        val colorHex = args.sourceColor
        try {
            val colorInt = Color.parseColor(colorHex)
            binding.ivDeviceIcon.imageTintList = ColorStateList.valueOf(colorInt)
            
            // 22% background opacity tint
            val bgTint = Color.argb(
                (255 * 0.22).toInt(),
                Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt)
            )
            binding.llDeviceIcon.backgroundTintList = ColorStateList.valueOf(bgTint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Setup Status Badge Pill
        binding.tvStatus.text = args.sourceStatus
        
        // Setup control descriptions
        binding.tvPauseDesc.text = "Temporarily stop using ${args.sourceName}"
        binding.tvDeleteDesc.text = "Permanently remove everything from this source"

        // Setup controls clicks
        binding.llPauseSource.setOnClickListener {
            ToastUtils.showShort(requireContext(), "Paused ${args.sourceName}")
        }

        binding.llDeleteSource.setOnClickListener {
            ToastUtils.showShort(requireContext(), "Deleted all data for ${args.sourceName}")
        }
    }
}
