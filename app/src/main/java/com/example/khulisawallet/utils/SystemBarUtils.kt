package com.example.khulisawallet.utils

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Applies status-bar and navigation-bar padding to the activity content root.
 * Required on Android 15+ where edge-to-edge is enforced regardless of theme opt-out.
 */
fun AppCompatActivity.applySystemBarInsets() {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val root = (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0) ?: return

    fun applyPadding(view: View, insets: Insets) {
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        applyPadding(view, windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()))
        windowInsets
    }

    ViewCompat.getRootWindowInsets(root)?.let { insets ->
        applyPadding(root, insets.getInsets(WindowInsetsCompat.Type.systemBars()))
    }
    ViewCompat.requestApplyInsets(root)
}
