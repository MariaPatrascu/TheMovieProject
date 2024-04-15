package com.example.themovie.feature.movie.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.themovie.R
import com.example.themovie.databinding.ActivityMovieDetailsBinding
import com.example.themovie.feature.common.SharedExtras
import com.example.themovie.mvi.NavDirection
import com.example.themovie.mvi.ReactiveActivity
import com.example.themovie.mvi.observeNavigation
import com.example.themovie.util.FragmentUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MovieDetailsActivity : ReactiveActivity<NavDirection>() {

    @Inject
    lateinit var movieDetailsFragment: MovieDetailsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val viewBinding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        observeNavigation(MovieDetailsViewModel::class)

        movieDetailsFragment.arguments = intent.extras
        FragmentUtil.addFragment(
            supportFragmentManager,
            movieDetailsFragment,
            R.id.main_content_container
        )
    }

    companion object {
        fun createIntent(context: Context, movieId: Int): Intent {
            val intent = Intent(context, MovieDetailsActivity::class.java)
            intent.putExtra(SharedExtras.MOVIE_ID, movieId)
            return intent
        }
    }

    override fun navigate(navDirection: NavDirection) {
        when (navDirection) {
            is MovieDetails.Navigation.GoBack -> {
                val resultIntent = Intent()
                resultIntent.putExtra(SharedExtras.MOVIE_IS_EDITED, navDirection.movieIsEdited)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
