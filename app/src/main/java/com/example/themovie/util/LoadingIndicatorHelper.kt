package com.example.themovie.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.themovie.R

private const val FULL_ALPHA = 1f
private const val DIM_ALPHA = .3f

private const val ANIMATION_DURATION_MS = 150L

object LoadingIndicatorHelper {
    fun Fragment.showLoading(show: Boolean) =
        showLoadingIndicator(requireActivity(), show)

    private fun showLoadingIndicator(fragmentActivity: FragmentActivity, show: Boolean) {
        var loadingIndicatorContainer =
            fragmentActivity.findViewById<View>(R.id.container_loading_indicator)
        if (loadingIndicatorContainer == null && show) {
            val content = fragmentActivity.findViewById<ViewGroup>(android.R.id.content)
            loadingIndicatorContainer = LayoutInflater.from(fragmentActivity)
                .inflate(R.layout.container_loading_indicator, content, false)
            content.addView(loadingIndicatorContainer, -1)
        }
        loadingIndicatorContainer?.apply {
            // don't show or hide if it's already shown/hidden
            if (isVisible != show) {
                setParentViewEnabled(fragmentActivity, !show)
                isVisible = show
            }
        }
    }

    private fun setParentViewEnabled(
        fragmentActivity: FragmentActivity,
        enable: Boolean
    ) {
        val contentView = fragmentActivity.findViewById(R.id.main_content_container)
            ?: fragmentActivity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        contentView?.apply {
            clearAnimation()
            animate()
                .setDuration(ANIMATION_DURATION_MS)
                .alpha(if (enable) FULL_ALPHA else DIM_ALPHA).setAnimationListener(
                    onAnimationStart = {
                        setViewAndChildrenEnabled(
                            this@apply,
                            enable
                        )
                    }
                ).start()
        }
    }

    private fun ViewPropertyAnimator.setAnimationListener(
        onAnimationStart: (animator: Animator) -> Unit = {},
        onAnimationEnd: (animator: Animator) -> Unit = {},
    ): ViewPropertyAnimator {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) = onAnimationStart(animator)
            override fun onAnimationEnd(animator: Animator) = onAnimationEnd(animator)
        }
        setListener(listener)

        return this
    }

    private fun setViewAndChildrenEnabled(view: View, enabled: Boolean) {
        if (view !is EditText) view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                setViewAndChildrenEnabled(child, enabled)
            }
        }
    }
}
