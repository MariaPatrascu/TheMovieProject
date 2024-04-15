package com.example.themovie.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.themovie.R
import com.example.themovie.databinding.FragmentHomeBinding
import com.example.themovie.feature.home.Home.Intent
import com.example.themovie.feature.home.Home.State
import com.example.themovie.feature.home.pager.MoviePagerAdapter
import com.example.themovie.feature.home.pager.now_playing.NowPlayingFragment
import com.example.themovie.feature.home.pager.popular.PopularFragment
import com.example.themovie.feature.home.pager.top_rated.TopRatedFragment
import com.example.themovie.feature.home.pager.upcoming.UpcomingFragment
import com.example.themovie.mvi.ReactiveView
import com.example.themovie.mvi.bind
import com.example.themovie.mvi.intent
import com.example.themovie.network.api.constants.RecommendationType.NOW_PLAYING
import com.example.themovie.network.api.constants.RecommendationType.POPULAR
import com.example.themovie.network.api.constants.RecommendationType.TOP_RATED
import com.example.themovie.network.api.constants.RecommendationType.UPCOMING
import com.example.themovie.util.ExceptionUtils
import com.example.themovie.util.LoadingIndicatorHelper.showLoading
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class HomeFragment @Inject constructor() :
    ReactiveView<State, Intent>() {

    private var _viewBinding: FragmentHomeBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitle = getString(R.string.bottom_nav_item_home)
        bind(HomeViewModel::class, sharedWithActivity = true)
        intent(
            Intent.LoadContent(
                requireContext(),
                listOf(NOW_PLAYING, POPULAR, TOP_RATED, UPCOMING)
            )
        )
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    private fun setupTabLayout() {
        val fragments = listOf(
            NowPlayingFragment(),
            PopularFragment(),
            TopRatedFragment(),
            UpcomingFragment()
        )
        val moviePagerAdapter = MoviePagerAdapter(
            fragments,
            childFragmentManager,
            lifecycle
        )
        viewBinding.moviesPager.offscreenPageLimit = fragments.size
        viewBinding.moviesPager.adapter = moviePagerAdapter
        TabLayoutMediator(
            viewBinding.recommendationTabs,
            viewBinding.moviesPager
        ) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.recommendation_type_now_playing)
                1 -> tab.text = getString(R.string.recommendation_type_popular)
                2 -> tab.text = getString(R.string.recommendation_type_top_rated)
                3 -> tab.text = getString(R.string.recommendation_type_upcoming)
            }
        }.attach()
    }

    private fun setListeners() {
        viewBinding.recommendationTabs.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                intent(Intent.RecommendationSelectedPosition(tab.position))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun render(state: State) {
        with(state) {
            showLoading(isLoading)
            dataLoaded.get()?.let {
                if (it) setupTabLayout()
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
