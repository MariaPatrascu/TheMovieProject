package com.example.themovie.mvi

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * IntentProducer needs to provide a stream of Intents and a way to produce them. The View
 * should implement this interface.
 */
interface IntentProducer<T : UiIntent> {
    val intents: Flow<T>
    suspend fun produce(intent: T)
}

/**
 * IntentProducer implemented using Kotlin Coroutines
 */
class ChannelIntentProducer<T : UiIntent> :
    IntentProducer<T> {

    private val channel = Channel<T>()

    override val intents: Flow<T> = flow {
        for (intent in channel) {
            emit(intent)
        }
    }

    override suspend fun produce(intent: T) {
        channel.send(intent)
    }
}

fun <I : UiIntent, F> F.intent(intent: I)
        where F : Fragment, F : IntentProducer<I> {
    lifecycleScope.launch { produce(intent) }
}
