package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTagBinding
import com.humotron.app.ui.bioHack.adapter.QuestionAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagFragment : BaseFragment(R.layout.fragment_tag) {

    private val viewModel: NuggetsViewModel by activityViewModels()

    private lateinit var binding: FragmentTagBinding
    private lateinit var adapter: QuestionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTagBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }
        initAdapter()

        viewModel.getNuggetsTypeAndLevel()
        binding.btnNext.isEnabled = false

        viewModel.getNuggetsTypeAndLevelData().observe(viewLifecycleOwner) { networkStatus ->
            when (networkStatus.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data =
                        networkStatus.data?.nuggetsData?.first { it.tagType == "CONTEXT TAG" }
                            ?: return@observe

                    if (!data.tags.isNullOrEmpty()) {
                        binding.tvSelected.text =
                            getString(R.string.selected_d_d, 0, data.tags.size)
                        adapter.setData(data.tags)
                    }
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


        binding.btnNext.setOnClickListener {
            viewModel.setSelectedTags(adapter.questionList.filter { it.isChecked })
            findNavController().navigate(R.id.fragmentCategoryTag)
        }
    }

    private fun initAdapter() {
        adapter = QuestionAdapter {
            binding.btnNext.isEnabled = it > 0
            binding.tvSelected.text =
                getString(R.string.selected_d_d, it, binding.rvQuestions.adapter?.itemCount ?: 0)
        }
        binding.rvQuestions.adapter = adapter
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
    }
}