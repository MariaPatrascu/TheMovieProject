package com.example.themovie.mvi

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Base class for a ViewModel that implements the MVI pattern and uses Coroutines and Flow.
 */
abstract class ReactiveViewModel<S : UiState, I : UiIntent, C : UiChange, N : NavDirection>(
    private var currentState: S,
    private var currentNavigationDirection: N? = null
) : ViewModel() {

    /**
     * Channel & Flow used to buffer emitted changes
     */
    private val changes = Channel<C>()
    private val changesFlow = flow {
        for (change in changes) {
            emit(change)
        }
    }

    /**
     * Channel & Flow used to buffer emitted changes
     */
    private val navigation = Channel<N>()
    private val navigationFlow = flow {
        for (change in navigation) {
            emit(change)
        }
    }

    /**
     * Helper function for emitting changes.
     */
    internal suspend fun change(change: C) {
        changes.send(change)
    }

    /**
     * Helper function for emitting a new navigation direction change.
     */
    internal suspend fun navigate(navDirection: N) {
        navigation.send(navDirection)
    }

    val state: LiveData<S> = liveData(viewModelScope.coroutineContext) {
        changesFlow.scan(currentState) { accumulator, value ->
            reducer(accumulator, value).also { newState ->
                currentState = newState
            }
        }.collect {
            emit(it)
        }
    }

    val navigationDirection: LiveData<N> = liveData(viewModelScope.coroutineContext) {
        navigationFlow.collect {
            currentNavigationDirection = it
            emit(it)
        }
    }

    /**
     * Function called when the View wants to attach to this ViewModel
     */
    fun bindTo(producer: IntentProducer<I>) {
        viewModelScope.launch {
            producer.intents.collect{
                process(it, currentState)
            }
        }
    }

    abstract val reducer: UiReducer<S, C>

    protected abstract suspend fun process(intent: I, state: S)
}

fun <VM : ReactiveViewModel<S, I, C, N>, S : UiState, I : UiIntent, C : UiChange, N : NavDirection, V> V.bind(
    viewModelClass: KClass<VM>,
    sharedWithActivity: Boolean = false
): ReactiveViewModel<S, I, C, N>? where V : ReactiveView<S, I> {
    this.activity?.let {
        val provider = if (sharedWithActivity) {
            ViewModelProvider(requireActivity(), it.defaultViewModelProviderFactory)
        } else {
            ViewModelProvider(this, it.defaultViewModelProviderFactory)
        }
        provider[viewModelClass.java].let { viewModel ->
            viewModel.bindTo(this)
            viewModel.state.observe(viewLifecycleOwner) { state -> render(state) }

            return viewModel
        }
    }

    return null
}

fun <VM : ReactiveViewModel<S, I, C, N>, S : UiState, I : UiIntent, C : UiChange, N : NavDirection, V> V.observeNavigation(
    viewModelClass: KClass<VM>
) where V : ReactiveActivity<N> {
    val provider = ViewModelProvider(this, defaultViewModelProviderFactory)
    provider[viewModelClass.java].let { viewModel ->
        viewModel.navigationDirection.observe(this) { navDirection -> navigate(navDirection) }
    }
}
