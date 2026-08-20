package com.pratham.webhub.webview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class TabWebViewSettings(
        val isJsEnabled: Boolean = true,
        val isAdBlockEnabled: Boolean = true,
        val cssOverride: String? = null,
        val userScript: String? = null
    )

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(tabId: String, settings: TabWebViewSettings): WebView {
        val webView = WebView(context)

        webView.id = tabId.hashCode()
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false

        configureSettings(webView, settings)
        configureClients(webView, tabId)

        return webView
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureSettings(webView: WebView, settings: TabWebViewSettings) {
        val webSettings = webView.settings

        // JavaScript
        webSettings.javaScriptEnabled = settings.isJsEnabled

        // DOM storage
        webSettings.domStorageEnabled = true

        // Database storage
        webSettings.databaseEnabled = true

        // File access disabled for security
        webSettings.allowFileAccess = false
        webSettings.allowFileAccessFromFileURLs = false
        webSettings.allowUniversalAccessFromFileURLs = false

        // Safe browsing
        webSettings.safeBrowsingEnabled = true

        // Mixed content mode - never allow
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        // Geolocation disabled until a proper permission flow is implemented
        webSettings.setGeolocationEnabled(false)

        // Modern web features
        webSettings.loadsImagesAutomatically = true
        webSettings.blockNetworkImage = false
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(false)

        // User agent
        webSettings.userAgentString = webSettings.userAgentString + " Veyla/1.0"

    }

    private fun configureClients(webView: WebView, tabId: String) {
        // Default WebViewClient - can be replaced by the manager with the full one
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }

        // Default WebChromeClient with geolocation handling
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                // Default: deny geolocation; the manager can set a more permissive client
                callback.invoke(origin, false, false)
            }
        }
    }
}
