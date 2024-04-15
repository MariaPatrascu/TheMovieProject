package com.example.themovie.feature.movie.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.themovie.R
import com.example.themovie.config.GlobalConfig
import com.example.themovie.databinding.FragmentMovieDetailsBinding
import com.example.themovie.databinding.ItemMovieGenreBinding
import com.example.themovie.feature.common.SharedExtras
import com.example.themovie.feature.movie.details.MovieDetails.Intent
import com.example.themovie.feature.movie.details.MovieDetails.State
import com.example.themovie.mvi.ReactiveView
import com.example.themovie.mvi.bind
import com.example.themovie.mvi.intent
import com.example.themovie.network.api.data.Movie
import com.example.themovie.util.ExceptionUtils
import com.example.themovie.util.LoadingIndicatorHelper.showLoading
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class MovieDetailsFragment @Inject constructor() :
    ReactiveView<State, Intent>() {

    private var _viewBinding: FragmentMovieDetailsBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(MovieDetailsViewModel::class, sharedWithActivity = true)
        arguments?.takeIf { it.containsKey(SharedExtras.MOVIE_ID) }?.apply {
            val movieId = getInt(SharedExtras.MOVIE_ID)
            intent(Intent.LoadContent(requireContext(), movieId))
        }
        viewBinding.closeImage.setOnClickListener {
            intent(Intent.OnCloseClicked)
        }
    }

    private fun displayMovieDetails(movie: Movie) {
        displayYear(movie)
        displayVotes(movie)
        displayMovieGenres(movie)

        // set backdropImage, posterImage and hide if empty
        val backdropImage = GlobalConfig.baseImageUrl + movie.backdropPath
        val posterImage = GlobalConfig.baseImageUrl + movie.posterPath
        Glide.with(this@MovieDetailsFragment)
            .load(backdropImage)
            .into(viewBinding.movieBackdrop)
        Glide.with(this@MovieDetailsFragment)
            .load(posterImage)
            .into(viewBinding.moviePoster)

        // set title, tagline, overview and hide if empty
        viewBinding.movieTitle.isVisible = movie.title.isNotEmpty()
        viewBinding.movieTitle.text = movie.title

        viewBinding.movieTagline.isVisible = !movie.tagline.isNullOrEmpty()
        viewBinding.movieTagline.text = movie.tagline

        viewBinding.movieOverview.isVisible = movie.overview.isNotEmpty()
        viewBinding.movieOverviewTitle.isVisible = movie.overview.isNotEmpty()
        viewBinding.movieOverview.text = movie.overview

        // separator not needed if overview it's empty
        viewBinding.movieSeparator.isVisible = movie.overview.isNotEmpty()

        // set favorite image
        val favoriteImage =
            if (movie.isFavoriteMovie == true) R.drawable.ic_favorite_pressed else R.drawable.ic_favorite_unpressed
        Glide.with(this)
            .load(favoriteImage)
            .into(viewBinding.movieFavoriteImage)

        viewBinding.movieFavoriteImage.setOnClickListener {
            if (movie.isFavoriteMovie == true)
                intent(Intent.OnRemoveFromFavoritesClicked(requireContext(), movie.movieId))
            else
                intent(Intent.OnAddToFavoritesClicked(requireContext(), movie.movieId))
        }
    }

    private fun displayYear(movie: Movie) {
        // code execution not needed if the movie release date it's empty
        if (movie.releaseDate.isNotEmpty()) {
            viewBinding.movieReleaseYear.isVisible = true
            try {
                val format = SimpleDateFormat(
                    getString(R.string.date_format_short_year_month_day),
                    Locale.getDefault()
                )
                val yearFormat = SimpleDateFormat(
                    getString(R.string.date_format_short_year),
                    Locale.getDefault()
                )
                val date: Date = format.parse(movie.releaseDate) ?: Date()
                viewBinding.movieReleaseYear.text =
                    getString(R.string.released_in_x, yearFormat.format(date))
            } catch (e: Exception) {
                print("Error formatting release date")
            }
        } else {
            viewBinding.movieReleaseYear.isVisible = false
        }
    }

    private fun displayVotes(movie: Movie) {
        val voteAverageTwoDecimals = DecimalFormat("#,##0.00").format(movie.voteAverage)
        val voteAverageVoteCountSpannable: Spannable = SpannableString(
            getString(
                R.string.x_vote_average_vote_count_x, voteAverageTwoDecimals.toString(),
                movie.voteCount
            )
        )
        voteAverageVoteCountSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.medium_gray)),
            getString(R.string.x_vote_average, voteAverageTwoDecimals).length,
            voteAverageVoteCountSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        viewBinding.movieVoteAverageVoteCount.text = voteAverageVoteCountSpannable
    }

    private fun displayMovieGenres(movie: Movie) {
        viewBinding.movieGenresContainer.removeAllViews()
        var movieGenres = movie.genres
        //  take only the first two from the `genres` array. If the array contains
        //  less than two objects display one or none
        if (!movieGenres.isNullOrEmpty()) {
            if (movieGenres.size > 1) {
                movieGenres = movie.genres?.take(2)
                movieGenres?.forEach {
                    val movieGenreView = ItemMovieGenreBinding.inflate(LayoutInflater.from(context))
                    movieGenreView.movieGenre.text = it.name
                    viewBinding.movieGenresContainer.addView(movieGenreView.root)
                }
            } else {
                val movieGenreView = ItemMovieGenreBinding.inflate(LayoutInflater.from(context))
                val movieGenre = movieGenres[0]
                movieGenreView.movieGenre.text = movieGenre.name
                viewBinding.movieGenresContainer.addView(movieGenreView.root)
            }
        } else {
            viewBinding.movieGenreHorizontalScroll.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // handle on swipe back and on back pressed when returning back from movie details
        // to show favorite movies updated
        view?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val deltaX: Float = event.x
                if (abs(deltaX.toDouble()) > 150) {
                    intent(Intent.OnCloseClicked)
                }
            }
            true
        }

        val callback: OnBackPressedCallback = object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                intent(Intent.OnCloseClicked)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun render(state: State) {
        with(state) {
            showLoading(isLoading)
            movie.get()?.let {
                displayMovieDetails(it)
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
