package com.example.themovie.feature.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.themovie.R
import com.example.themovie.databinding.FragmentSearchBinding
import com.example.themovie.feature.common.MovieAdapter
import com.example.themovie.feature.search.Search.Intent
import com.example.themovie.feature.search.Search.State
import com.example.themovie.mvi.ReactiveView
import com.example.themovie.mvi.bind
import com.example.themovie.mvi.intent
import com.example.themovie.util.ExceptionUtils
import com.example.themovie.util.LoadingIndicatorHelper.showLoading
import javax.inject.Inject


open class SearchFragment @Inject constructor() :
    ReactiveView<State, Intent>(), MovieAdapter.Callback {

    private var _viewBinding: FragmentSearchBinding? = null
    private val viewBinding get() = _viewBinding!!
    private lateinit var movieAdapter: MovieAdapter

    // we need to refresh search screen if a movie it's added or removed from favorites
    // when we come back from movie details screen
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
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
        toolbarTitle = getString(R.string.bottom_nav_item_search)
        bind(SearchViewModel::class, sharedWithActivity = true)
        initMovieAdapter()
        setListeners()
        showKeyboard()
    }

    private fun showKeyboard() {
        viewBinding.searchEdit.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(viewBinding.searchEdit, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onPause() {
        super.onPause()
        viewBinding.searchEdit.text?.clear()
    }

    private fun setListeners() {
        viewBinding.searchClear.setOnClickListener {
            viewBinding.searchEdit.text?.clear()
        }
        viewBinding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    searchQuery = editable.toString()
                    intent(
                        Intent.SearchForMovie(
                            requireContext(),
                            editable.toString()
                        )
                    )
                }
            }
        })
    }

    private fun initMovieAdapter() {
        viewBinding.searchMoviesRv.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(this)
        viewBinding.searchMoviesRv.adapter = movieAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    override fun onItemClicked(movieId: Int) =
        intent(Intent.OnMovieSelection(requireContext(), movieId, searchQuery))

    override fun onAddToFavoritesClicked(movieId: Int) =
        intent(Intent.OnAddToFavoritesClicked(movieId))

    override fun onRemoveFromFavoritesClicked(movieId: Int) =
        intent(Intent.OnRemoveFromFavoritesClicked(movieId))

    override fun render(state: State) {
        with(state) {
            showLoading(isLoading)
            movieList.get()?.let {
                movieAdapter.submitList(it)
                viewBinding.searchMoviesRv.isVisible = it.isNotEmpty()
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
