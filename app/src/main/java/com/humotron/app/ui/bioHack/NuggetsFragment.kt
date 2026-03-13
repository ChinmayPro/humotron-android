package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentNuggetsBinding
import com.humotron.app.domain.modal.param.NuggetsInteraction
import com.humotron.app.ui.bioHack.adapter.CardStackAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeableMethod
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NuggetsFragment : BaseFragment(R.layout.fragment_nuggets), CardStackListener {

    private lateinit var binding: FragmentNuggetsBinding

    private lateinit var manager: CardStackLayoutManager

    private val viewModel: NuggetsViewModel by activityViewModels()
    private var nuggetId: String? = null
    private var anecdote: String? = null
    private val adapter by lazy {
        CardStackAdapter { nuggetId, anecdoteId ->
            this.nuggetId = nuggetId
            this.anecdote = anecdoteId
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNuggetsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 30)
            insets
        }


        binding.header.ivBooks.setImageResource(R.drawable.ic_books_disable)
        binding.header.ivProgress.setImageResource(R.drawable.ic_biohack_progress_disable)
        binding.header.ivNuggets.setImageResource(R.drawable.ic_nuggets_checked)
        initialize()

        if (arguments?.getString("is_from") == "setup") {
            viewModel.getNuggetsPreference()
        } else {
            viewModel.getNuggetsPreference()
        }

        subscribeToObservers()

        binding.ivLike.setOnClickListener {
            sendInteraction("like")
        }

        binding.ivDisLike.setOnClickListener {
            sendInteraction("dislike")
        }

        binding.ivBookMark.setOnClickListener {
            sendInteraction("bookmark")
        }

        binding.ivDetail.setOnClickListener {
            val bundle = Bundle().apply {
                nuggetId?.let {
                    anecdote?.let {
                        putString("id", nuggetId)
                        putString("anecdote", anecdote)
                    }
                }
            }
            findNavController().navigate(R.id.fragmentNuggetDetail, bundle)
        }

        binding.header.ivBooks.setOnClickListener {
            findNavController().navigate(R.id.fragmentBookDetail)
        }

        binding.header.ivProgress.setOnClickListener {
            findNavController().navigate(R.id.fragmentProgress)
        }

    }

    private fun subscribeToObservers() {
        viewModel.nuggetsPreference.observe(viewLifecycleOwner) { data ->
            if (!data.isNullOrEmpty()) {
                val nuggets = data.filter { !it.anecdotes.isNullOrEmpty() }
                adapter.setNuggets(nuggets)
            }
        }

        viewModel.nuggetsInteractionLiveDataData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val nuggets = it.data?.data?.nugget ?: return@observe
                    when (nuggets.interaction) {
                        "like" -> {
                            binding.cardStackView.scrollToPosition(manager.topPosition+1)
                        }

                        "dislike" -> {
                            binding.cardStackView.scrollToPosition(manager.topPosition+1)
                        }

                        "bookmark" -> {
                           binding.cardStackView.scrollToPosition(manager.topPosition+1)
                        }
                    }
                }

                Status.ERROR -> {
                }

                Status.EXCEPTION -> {
                }

                Status.LOADING -> {
                }
            }
        }
    }

    private fun initialize() {
        manager = CardStackLayoutManager(requireContext(), this)
        manager.setStackFrom(StackFrom.TopAndRight)
        manager.setVisibleCount(2)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.FREEDOM)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)


        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = adapter
        binding.cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun sendInteraction(type: String) {
        val id = nuggetId ?: adapter.getNuggets().getOrNull(manager.topPosition)?.id
        id?.let { id ->
            viewModel.nuggetsInteraction(
                NuggetsInteraction(
                    nuggetId = id,
                    anecdoteId = anecdote,   // can be null safely
                    interactionType = type,
                    detailPage = false
                )
            )
        }
    }


    override fun onCardDragging(
        direction: Direction?,
        ratio: Float
    ) {

    }

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Left -> sendInteraction("dislike")
            Direction.Right -> sendInteraction("like")
            Direction.Top -> sendInteraction("bookmark")
            Direction.Bottom -> sendInteraction("like")   // as your logic
            else -> {}
        }
    }

    override fun onCardRewound() {

    }

    override fun onCardCanceled() {

    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardDisappeared(view: View?, position: Int) {

    }


}