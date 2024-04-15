package com.example.themovie.feature.home.pager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.themovie.feature.common.SharedExtras
import com.example.themovie.network.api.constants.RecommendationType


class MoviePagerAdapter(
    private val fragments: List<Fragment>,
    fragmentManager: FragmentManager,
    lifecycle: androidx.lifecycle.Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        val fragment = fragments[position]
        fragment.arguments = Bundle().apply {
            val recommendationString = when (position) {
                0 -> RecommendationType.NOW_PLAYING
                1 -> RecommendationType.POPULAR
                2 -> RecommendationType.TOP_RATED
                3 -> RecommendationType.UPCOMING
                else -> ""
            }
            putString(SharedExtras.RECOMMENDATION_TYPE, recommendationString)
        }
        return fragment
    }
}
