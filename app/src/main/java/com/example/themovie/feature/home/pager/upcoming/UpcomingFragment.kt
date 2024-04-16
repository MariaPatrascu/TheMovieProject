package com.example.themovie.feature.home.pager.upcoming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.themovie.databinding.FragmentViewPagerBinding
import com.example.themovie.feature.common.MovieAdapter
import com.example.themovie.feature.common.SharedExtras
import com.example.themovie.feature.common.SortingOptionsByRecommendation
import com.example.themovie.feature.home.pager.MoviePager.Intent
import com.example.themovie.feature.home.pager.MoviePager.State
import com.example.themovie.mvi.ReactiveView
import com.example.themovie.mvi.bind
import com.example.themovie.mvi.intent
import com.example.themovie.util.ExceptionUtils
import com.example.themovie.util.LoadingIndicatorHelper.showLoading
import kotlinx.coroutines.launch

class UpcomingFragment : ReactiveView<State, Intent>(),
    MovieAdapter.Callback {

    private lateinit var movieAdapter: MovieAdapter
    private var _viewBinding: FragmentViewPagerBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var recommendationType = ""

    // we need to know what sorting option was currently selected for swipe to refresh
    private var sortingOption = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind(UpcomingViewModel::class, sharedWithActivity = true)
        initMovieAdapter()
        arguments?.takeIf { it.containsKey(SharedExtras.RECOMMENDATION_TYPE) }?.apply {
            recommendationType = getString(SharedExtras.RECOMMENDATION_TYPE) ?: ""
            intent(Intent.ShowMoviesByRecommendation(recommendationType))
        }
        lifecycleScope.launch {
            SortingOptionsByRecommendation.sortingOptionsUpcoming.collect {
                sortingOption = it.name
                intent(
                    Intent.SortMovies(
                        sortingOption,
                        recommendationType
                    )
                )
            }
        }
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            intent(Intent.RefreshContent(requireContext(), recommendationType, sortingOption))
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initMovieAdapter() {
        viewBinding.moviesRv.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(this)
        viewBinding.moviesRv.adapter = movieAdapter
    }

    override fun onMovieClicked(movieAdapterPosition: Int, movieId: Int) =
        intent(
            Intent.OnMovieSelection(
                sortingOption,
                movieAdapterPosition,
                recommendationType,
                movieId
            )
        )

    override fun onAddToFavoritesClicked(movieId: Int) =
        intent(
            Intent.OnAddToFavoritesClicked(
                movieId,
                recommendationType
            )
        )

    override fun onRemoveFromFavoritesClicked(movieId: Int) =
        intent(
            Intent.OnRemoveFromFavoritesClicked(
                movieId,
                recommendationType
            )
        )

    override fun render(state: State) {
        with(state) {
            showLoading(isLoading)
            if (!isLoading) viewBinding.swipeRefreshLayout.isRefreshing = false
            movieList.get()?.let {
                if (it.isNotEmpty()) {
                    movieAdapter.submitList(it)
                    viewBinding.moviesRv.isVisible = true
                }
            }
            error.get()?.let {
                when (it) {
                    is State.Error.Default -> ExceptionUtils.toast(
                        message = it.message,
                        context = context
                    )

                    is State.Error.Network -> ExceptionUtils.toast(
                        message = it.message,
                        context = context
                    )
                }
            }
            // we need to know adapter position when we come back from the movie details screen and we've added/removed a movie
            // from favorites
            // this can be improved on the recycler view scroll animation side
            movieAdapterPosition.get()?.let {
                if (it != 0) {
                    viewBinding.moviesRv.smoothScrollToPosition(
                        it
                    )
                    intent(Intent.ResetAdapterPosition)
                }
            }
        }
    }
}
