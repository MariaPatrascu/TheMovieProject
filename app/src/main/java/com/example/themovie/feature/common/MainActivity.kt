package com.example.themovie.feature.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.themovie.R
import com.example.themovie.databinding.ActivityMainBinding
import com.example.themovie.feature.favorites.Favorites
import com.example.themovie.feature.favorites.FavoritesFragment
import com.example.themovie.feature.favorites.FavoritesViewModel
import com.example.themovie.feature.home.Home
import com.example.themovie.feature.home.HomeFragment
import com.example.themovie.feature.home.HomeViewModel
import com.example.themovie.feature.home.pager.MoviePager
import com.example.themovie.feature.home.pager.now_playing.NowPlayingViewModel
import com.example.themovie.feature.home.pager.popular.PopularViewModel
import com.example.themovie.feature.home.pager.top_rated.TopRatedViewModel
import com.example.themovie.feature.home.pager.upcoming.UpcomingViewModel
import com.example.themovie.feature.movie.details.MovieDetailsActivity
import com.example.themovie.feature.search.Search
import com.example.themovie.feature.search.SearchFragment
import com.example.themovie.feature.search.SearchViewModel
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveActivity
import com.example.themovie.mvi.observeNavigation
import com.example.themovie.util.FragmentUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ReactiveActivity<NavDirection>() {

    @Inject
    lateinit var homeFragment: HomeFragment

    @Inject
    lateinit var favoritesFragment: FavoritesFragment

    @Inject
    lateinit var searchFragment: SearchFragment

    private var sortingOptionsByRecommendation: MutableStateFlow<SortingOptions> =
        SortingOptionsByRecommendation.sortingOptionsNowPlaying

    private var movieFavoriteResultCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        observeNavigation(HomeViewModel::class)
        observeNavigation(NowPlayingViewModel::class)
        observeNavigation(PopularViewModel::class)
        observeNavigation(TopRatedViewModel::class)
        observeNavigation(UpcomingViewModel::class)
        observeNavigation(FavoritesViewModel::class)
        observeNavigation(SearchViewModel::class)
        setupBottomNavigation()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private var onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    // we receive back a boolean from the movie details screen that changes
    // it's value if a movie was added/removed from favorites so we can refresh
    // the current screen where we are currently in
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val movieIdEdited = data?.getBooleanExtra(
                    SharedExtras.MOVIE_IS_EDITED,
                    false
                ) ?: false
                when (result.resultCode) {
                    Activity.RESULT_OK -> movieFavoriteResultCallback?.invoke(movieIdEdited)
                }
            }
        }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_favorites ->
                    FragmentUtil.replaceFragment(
                        supportFragmentManager,
                        favoritesFragment,
                        R.id.main_content_container

                    )

                R.id.action_home ->
                    if (supportFragmentManager.fragments.isEmpty()) {
                        FragmentUtil.addFragment(
                            supportFragmentManager,
                            homeFragment,
                            R.id.main_content_container
                        )
                    } else {
                        FragmentUtil.replaceFragment(
                            supportFragmentManager,
                            homeFragment,
                            R.id.main_content_container
                        )
                    }

                R.id.action_search -> FragmentUtil.replaceFragment(
                    supportFragmentManager,
                    searchFragment,
                    R.id.main_content_container
                )
            }
            true
        }
        bottomNavigationView.selectedItemId = R.id.action_home
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sorting_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // notifying the correct fragment to sort movies based on the menu option selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_rating_ascending -> {
                sortingOptionsByRecommendation.value =
                    SortingOptions.RATING_ASCENDING
            }

            R.id.action_rating_descending -> {
                sortingOptionsByRecommendation.value = SortingOptions.RATING_DESCENDING
            }

            R.id.action_date_ascending -> {
                sortingOptionsByRecommendation.value = SortingOptions.DATE_ASCENDING
            }

            R.id.action_date_descending -> {
                sortingOptionsByRecommendation.value = SortingOptions.DATE_DESCENDING
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // checking for what recommendation type it's the sorting from the menu options
    private fun recommendationSelectedPosition(position: Int) {
        when (position) {
            0 -> sortingOptionsByRecommendation =
                SortingOptionsByRecommendation.sortingOptionsNowPlaying

            1 -> sortingOptionsByRecommendation =
                SortingOptionsByRecommendation.sortingOptionsPopular

            2 -> sortingOptionsByRecommendation =
                SortingOptionsByRecommendation.sortingOptionsTopRated

            3 -> sortingOptionsByRecommendation =
                SortingOptionsByRecommendation.sortingOptionsUpcoming
        }
    }

    override fun navigate(navDirection: NavDirection) {
        when (navDirection) {
            is Home.Navigation.RecommendationSelectedPosition -> recommendationSelectedPosition(
                navDirection.position
            )

            is MoviePager.Navigation.GoToMovieDetailsActivity -> goToMovieDetailsActivity(
                navDirection.movieId,
                navDirection.resultCallback
            )

            is Favorites.Navigation.GoToMovieDetailsActivity -> goToMovieDetailsActivity(
                navDirection.movieId,
                navDirection.resultCallback
            )

            is Search.Navigation.GoToMovieDetailsActivity -> goToMovieDetailsActivity(
                navDirection.movieId,
                navDirection.resultCallback
            )
        }
    }

    private fun goToMovieDetailsActivity(movieId: Int, resultCallback: ((Boolean) -> Unit)) {
        movieFavoriteResultCallback = resultCallback
        resultLauncher.launch(
            MovieDetailsActivity.createIntent(this, movieId)
        )
    }
}
