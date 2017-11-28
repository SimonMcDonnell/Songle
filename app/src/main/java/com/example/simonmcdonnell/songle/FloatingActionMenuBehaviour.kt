package com.example.simonmcdonnell.songle

import com.github.clans.fab.FloatingActionMenu
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPropertyAnimatorListener
import android.view.View

// Class used to handle the movement of floating action button when Snackbar appears
class FloatingActionMenuBehavior : CoordinatorLayout.Behavior<FloatingActionMenu>() {
    private var mTranslationY: Float = 0.toFloat()

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        if (child is FloatingActionMenu && dependency is Snackbar.SnackbarLayout) {
            updateTranslation(parent, child, dependency)
        }
        return false
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View) {
        updateTranslation(parent, child, dependency)
    }

    private fun updateTranslation(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View) {
        val translationY = getTranslationY(parent, child)
        if (translationY != mTranslationY) {
            ViewCompat.animate(child)
                    .cancel()
            if (Math.abs(translationY - mTranslationY) == dependency.height.toFloat()) {
                ViewCompat.animate(child)
                        .translationY(translationY)
                        .setListener(null as ViewPropertyAnimatorListener?)
            } else {
                ViewCompat.setTranslationY(child, translationY)
            }
            mTranslationY = translationY
        }
    }

    private fun getTranslationY(parent: CoordinatorLayout, child: FloatingActionMenu): Float {
        var minOffset = 0.0f
        val dependencies = parent.getDependencies(child)
        var i = 0
        val z = dependencies.size
        while (i < z) {
            val view = dependencies[i] as View
            if (view is Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - view.getHeight().toFloat())
            }
            i++
        }
        return minOffset
    }
}
