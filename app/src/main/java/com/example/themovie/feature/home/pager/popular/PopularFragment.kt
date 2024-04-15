package com.example.themovie.feature.home.pager.popular

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

class PopularFragment : ReactiveView<State, Intent>(),
    MovieAdapter.Callback {

    private lateinit var movieAdapter: MovieAdapter
    private var _viewBinding: FragmentViewPagerBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var recommendationType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bind(PopularViewModel::class, sharedWithActivity = true)
        initMovieAdapter()
        arguments?.takeIf { it.containsKey(SharedExtras.RECOMMENDATION_TYPE) }?.apply {
            recommendationType = getString(SharedExtras.RECOMMENDATION_TYPE) ?: ""
            intent(Intent.ShowMoviesByRecommendation(recommendationType))
        }
        // we need to know what sorting option was currently selected for swipe to refresh
        var sortingOption = ""
        lifecycleScope.launch {
            SortingOptionsByRecommendation.sortingOptionsPopular.collect {
                sortingOption = it.name
                intent(Intent.SortMovies(sortingOption, recommendationType))
            }
        }
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            intent(
                Intent.RefreshContent(requireContext(), recommendationType, sortingOption)
            )
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    private fun initMovieAdapter() {
        viewBinding.moviesRv.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(this)
        viewBinding.moviesRv.adapter = movieAdapter
    }

    override fun onItemClicked(movieId: Int) =
        intent(Intent.OnMovieSelection(recommendationType, movieId))

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
        }
    }
}
