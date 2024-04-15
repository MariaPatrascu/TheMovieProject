package com.example.themovie.feature.common

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    protected var toolbarTitle: String?
        get() {
            val parentActivity = requireActivity()
            if (parentActivity is MainActivity) {
                parentActivity.supportActionBar?.let { it.title.toString() }
            }
            return null
        }
        set(title) {
            val parentActivity = requireActivity()
            if (parentActivity is MainActivity) {
                parentActivity.supportActionBar?.let { it.title = title }
            }
        }
}
