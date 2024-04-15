package com.example.themovie.feature.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.themovie.R
import com.example.themovie.config.GlobalConfig
import com.example.themovie.databinding.ViewMovieItemBinding
import com.example.themovie.network.api.data.Movie
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovieAdapter(val callback: Callback) :
    ListAdapter<Movie, MovieAdapter.ViewHolder>(DiffCallback()) {

    interface Callback {
        fun onItemClicked(movieId: Int)
        fun onAddToFavoritesClicked(movieId: Int)
        fun onRemoveFromFavoritesClicked(movieId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ViewMovieItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val viewBinding: ViewMovieItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(movie: Movie) {
            //format vote average to have two decimals
            val voteAverageTwoDecimals = DecimalFormat("#,##0.00").format(movie.voteAverage)
            viewBinding.movieRating.text = voteAverageTwoDecimals.toString()

            //show movie image
            val movieImage = GlobalConfig.baseImageUrl + movie.posterPath
            Glide.with(itemView)
                .load(movieImage)
                .into(viewBinding.moviePoster)

            //format and show movie year
            displayMovieYear(movie)

            viewBinding.root.setOnClickListener {
                callback.onItemClicked(movie.movieId)
            }

            val favoriteImage =
                if (movie.isFavoriteMovie == true) R.drawable.ic_favorite_pressed else R.drawable.ic_favorite_unpressed
            Glide.with(itemView)
                .load(favoriteImage)
                .into(viewBinding.movieFavoriteIcon)

            viewBinding.movieFavoriteIcon.setOnClickListener {
                setFavoriteMovies(movie)
            }
        }

        private fun displayMovieYear(movie: Movie) {
            try {
                val format = SimpleDateFormat(
                    itemView.context.getString(R.string.date_format_short_year_month_day),
                    Locale.getDefault()
                )
                val yearFormat = SimpleDateFormat(
                    itemView.context.getString(R.string.date_format_short_year),
                    Locale.getDefault()
                )
                val date: Date = format.parse(movie.releaseDate) ?: Date()
                viewBinding.movieYear.text = yearFormat.format(date)
            } catch (e: Exception) {
                print("Error formatting release date")
            }
        }

        private fun setFavoriteMovies(movie: Movie) {
            if (movie.isFavoriteMovie == true) {
                callback.onRemoveFromFavoritesClicked(movie.movieId)
                Glide.with(itemView)
                    .load(R.drawable.ic_favorite_unpressed)
                    .into(viewBinding.movieFavoriteIcon)
                currentList[adapterPosition].isFavoriteMovie = false
                submitList(currentList)
            } else {
                callback.onAddToFavoritesClicked(movie.movieId)
                Glide.with(itemView)
                    .load(R.drawable.ic_favorite_pressed)
                    .into(viewBinding.movieFavoriteIcon)
                currentList[adapterPosition].isFavoriteMovie = true
                submitList(currentList)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie) =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie) =
            oldItem == newItem
    }
}