package com.example.themovie.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

object FragmentUtil {
    fun addFragment(
        fragmentManager: FragmentManager,
        newFragment: Fragment,
        @IdRes parentContainer: Int
    ) {
        val transaction = fragmentManager.beginTransaction()
        transaction.add(parentContainer, newFragment)
        transaction.commit()
    }

    fun replaceFragment(
        fragmentManager: FragmentManager, newFragment: Fragment,
        @IdRes parentContainer: Int
    ) {
        val t: FragmentTransaction = fragmentManager.beginTransaction()
        val fragment = fragmentManager.findFragmentByTag(newFragment.javaClass.simpleName)
        // check if the new Fragment is the same
        // if it is, don't add to the back stack
        if (newFragment.javaClass == fragment?.javaClass) {
            t.replace(parentContainer, newFragment, newFragment.javaClass.simpleName).commit()
        } else {
            t.replace(parentContainer, newFragment, newFragment.javaClass.simpleName)
                .addToBackStack(newFragment.javaClass.simpleName)
                .commit()
        }
    }
}
