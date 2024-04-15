package com.example.themovie.util

data class SingleAccessData<out T>(
    val content: T,
    var isConsumed: Boolean = false
) {
    fun get(): T? {
        return if (isConsumed) {
            null
        } else {
            isConsumed = true
            content
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleAccessData<*>

        return content == other.content
    }

    override fun hashCode(): Int {
        return content?.hashCode() ?: 0
    }
}
