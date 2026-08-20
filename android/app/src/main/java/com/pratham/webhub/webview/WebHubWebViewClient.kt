package com.pratham.webhub.webview

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

private const val TAG = "WebHubWebViewClient"

class WebHubWebViewClient(
    private val tabId: String,
    private val onTitleChanged: (tabId: String, title: String) -> Unit,
    private val onUrlChanged: (tabId: String, url: String) -> Unit,
    private val onFaviconChanged: (tabId: String, faviconUrl: String?) -> Unit,
    private val onPageFinished: (tabId: String) -> Unit,
    private val onError: (tabId: String, error: String) -> Unit,
    private val adBlocker: AdBlocker
) : WebViewClient() {

    private var isLoading = false

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        if (adBlocker.shouldBlock(url)) {
            Log.d(TAG, "Blocked ad/resource: $url")
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                "".byteInputStream()
            )
        }
        return null
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        return handleSpecialUrl(view, url)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return handleSpecialUrl(view, url)
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        isLoading = true
        if (!url.isNullOrEmpty()) {
            onUrlChanged(tabId, url)
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        isLoading = false
        onPageFinished(tabId)
        if (!url.isNullOrEmpty()) {
            onUrlChanged(tabId, url)
        }
        extractFavicon(view)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: android.webkit.WebResourceError) {
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) {
            val errorMsg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                error.description?.toString() ?: "Unknown error"
            } else {
                "Failed to load page"
            }
            onError(tabId, errorMsg)
        }
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        onError(tabId, description ?: "Unknown error (code: $errorCode)")
    }

    override fun onReceivedSslError(view: WebView, handler: android.webkit.SslErrorHandler, error: android.net.http.SslError) {
        super.onReceivedSslError(view, handler, error)
        // Secure default: do NOT proceed with SSL errors
        handler.cancel()
        val errorMsg = "SSL error: ${error.primaryError} - ${error.url}"
        onError(tabId, errorMsg)
        Log.w(TAG, errorMsg)
    }

    override fun onRenderProcessGone(view: WebView, detail: android.webkit.RenderProcessGoneDetail): Boolean {
        val message = if (detail.didCrash()) {
            "Renderer process crashed for tab $tabId"
        } else {
            "Renderer process was killed by the system for tab $tabId"
        }
        Log.e(TAG, message)
        onError(tabId, message)
        return true // Return true to indicate we handled it (prevent default behavior)
    }

    private fun handleSpecialUrl(view: WebView, url: String): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme?.lowercase() ?: return false

        return when (scheme) {
            "http", "https" -> false // Let WebView handle normal URLs
            "tel" -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to handle tel: intent", e)
                }
                true
            }
            "mailto" -> {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to handle mailto: intent", e)
                }
                true
            }
            "intent" -> {
                // Only allow intents targeting known safe packages
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    val component = intent.component
                    if (component != null) {
                        val packageName = component.packageName
                        // Whitelist of safe packages that can receive intents
                        val safePackages = setOf(
                            "com.google.android.apps.maps",
                            "com.google.android.gm",
                            "com.google.android.dialer",
                            "com.android.vending",
                            "com.android.settings"
                        )
                        if (packageName in safePackages) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent.resolveActivity(view.context.packageManager) != null) {
                                view.context.startActivity(intent)
                            }
                        }
                        Log.d(TAG, "intent: URL targeted $packageName — allowed: ${packageName in safePackages}")
                    } else {
                        // Non-component intents are not allowed from WebView
                        Log.w(TAG, "Blocked intent: URL without explicit component")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to handle intent: URL", e)
                }
                true
            }
            else -> {
                // Try to launch an external intent for other schemes
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(view.context.packageManager) != null) {
                        view.context.startActivity(intent)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to handle $scheme: URL", e)
                    false
                }
            }
        }
    }

    private fun extractFavicon(webView: WebView) {
        try {
            webView.evaluateJavascript(
                """(function() {
                    var link = document.querySelector('link[rel="icon"]') ||
                              document.querySelector('link[rel="shortcut icon"]') ||
                              document.querySelector('link[rel="apple-touch-icon"]');
                    if (link) {
                        var href = link.href;
                        if (href && !href.startsWith('data:') && !href.startsWith('blob:')) {
                            return href;
                        }
                    }
                    return null;
                })()""".trimIndent()
            ) { result ->
                val faviconUrl = result?.trim()?.removeSurrounding("\"", "\"")
                if (!faviconUrl.isNullOrEmpty() && faviconUrl != "null") {
                    onFaviconChanged(tabId, faviconUrl)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract favicon for tab: $tabId", e)
        }
    }
}