package com.example.themovie.mvi

import androidx.appcompat.app.AppCompatActivity
import com.example.themovie.feature.common.BaseFragment

abstract class ReactiveView<S : UiState, I : UiIntent> :
    BaseFragment(), StateConsumer<S>, IntentProducer<I> by ChannelIntentProducer()

abstract class ReactiveActivity<N : NavDirection> :
    AppCompatActivity(), NavigationConsumer<N>
