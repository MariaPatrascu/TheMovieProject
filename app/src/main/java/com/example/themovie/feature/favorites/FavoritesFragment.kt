package com.example.themovie.feature.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.themovie.R
import com.example.themovie.databinding.FragmentFavoritesBinding
import com.example.themovie.feature.common.MovieAdapter
import com.example.themovie.feature.favorites.Favorites.Intent
import com.example.themovie.feature.favorites.Favorites.State
import com.example.themovie.mvi.ReactiveView
import com.example.themovie.mvi.bind
import com.example.themovie.mvi.intent
import javax.inject.Inject

class FavoritesFragment @Inject constructor() :
    ReactiveView<State, Intent>(), MovieAdapter.Callback {

    private var _viewBinding: FragmentFavoritesBinding? = null
    private val viewBinding get() = _viewBinding!!
    private lateinit var movieAdapter: MovieAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentFavoritesBinding.inflate(inflater, container, false)
        hideSortingToolbarMenu()
        return viewBinding.root
    }

    private fun hideSortingToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onPrepareMenu(menu: Menu) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitle = getString(R.string.bottom_nav_item_favorites)
        initMovieAdapter()
        bind(FavoritesViewModel::class, sharedWithActivity = true)
        intent(Intent.LoadContent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    private fun initMovieAdapter() {
        viewBinding.favoriteMoviesRv.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(this)
        viewBinding.favoriteMoviesRv.adapter = movieAdapter
    }

    override fun onItemClicked(movieId: Int) {
        intent(Intent.OnMovieSelection(movieId))
    }

    override fun onAddToFavoritesClicked(movieId: Int) {
        intent(Intent.OnAddToFavoritesClicked(movieId))
    }

    override fun onRemoveFromFavoritesClicked(movieId: Int) {
        intent(Intent.OnRemoveFromFavoritesClicked(movieId))
    }

    override fun render(state: State) {
        with(state) {
            movieList.get()?.let {
                viewBinding.favoriteMoviesRv.isVisible = it.isNotEmpty()
                viewBinding.favoritesEmptyText.isVisible = it.isEmpty()
                movieAdapter.submitList(it)
            }
        }
    }
}
