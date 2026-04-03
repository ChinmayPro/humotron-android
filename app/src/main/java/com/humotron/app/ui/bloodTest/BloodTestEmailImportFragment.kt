package com.humotron.app.ui.bloodTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.humotron.app.R
import com.humotron.app.databinding.FragmentBloodTestEmailImportBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BloodTestEmailImportFragment : Fragment() {

    private var _binding: FragmentBloodTestEmailImportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BloodTestViewModel by activityViewModels()
    private lateinit var adapter: PdfImportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBloodTestEmailImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, if (ime.bottom > bars.bottom) ime.bottom else bars.bottom)
            insets
        }
        
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            viewModel.clearResults()
            findNavController().navigateUp()
        }
        
        adapter = PdfImportAdapter(emptyList()) { count ->
            val formattedCount = String.format("%02d", count)
            binding.tvSelectedCountNumber.text = formattedCount
            binding.btnImport.isEnabled = count > 0
            binding.btnImport.alpha = if (count > 0) 1.0f else 0.5f
        }
        binding.rvEmails.adapter = adapter
        binding.rvEmails.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        binding.btnImport.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                viewModel.uploadSelectedPdfs(requireContext(), selectedItems)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnImport.isEnabled = !isLoading && adapter.getSelectedItems().isNotEmpty()
        }
        
        viewModel.uploadState.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            
            when (resource.status) {
                com.humotron.app.data.network.Status.LOADING -> {
                    // Handled by isLoading observer
                }
                com.humotron.app.data.network.Status.SUCCESS -> {
                    val message = resource.data?.message ?: "Upload successful!"
                    if (message.isNotEmpty()) {
                        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                    viewModel.resetUploadState()
                    // Stay on screen
                }
                com.humotron.app.data.network.Status.ERROR -> {
                    val errorMessage = resource.error?.errorMessage ?: ""
                    if (errorMessage.isNotEmpty()) {
                        android.widget.Toast.makeText(requireContext(), "Error: $errorMessage", android.widget.Toast.LENGTH_LONG).show()
                        android.util.Log.e("EmailImport", "Upload error: $errorMessage")
                    }
                    viewModel.resetUploadState()
                }
                else -> {}
            }
        }

        viewModel.pdfResults.observe(viewLifecycleOwner) { results ->
            if (results.isNotEmpty()) {
                binding.scrollView.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                binding.tvFoundCountLarge.text = String.format("%02d", results.size)
                adapter.updateData(results)
            } else {
                binding.scrollView.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvFoundCountLarge.text = "00"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
