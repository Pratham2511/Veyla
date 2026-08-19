package com.pratham.webhub.ui.browser

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that hosts a WebView in Picture-in-Picture mode for video content.
 * Launched when a website enters fullscreen video mode and the user navigates away.
 */
@AndroidEntryPoint
class PipActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl(url)
        }

        setContentView(webView)
        enterPiPMode(title)
    }

    private fun enterPiPMode(title: String) {
        val params = PictureInPictureParams.Builder()
            .setTitle(title)
            .setAspectRatio(Rational(16, 9))
            .setAutoEnterEnabled(true)
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            finish()
        }
    }

    override fun onUserLeaveHint() {
        val webView = this.webView ?: return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setAutoEnterEnabled(true)
            .build()
        enterPictureInPictureMode(params)
    }

    override fun onDestroy() {
        customViewCallback?.onCustomViewHidden()
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "pip_url"
        const val EXTRA_TITLE = "pip_title"
    }
}
