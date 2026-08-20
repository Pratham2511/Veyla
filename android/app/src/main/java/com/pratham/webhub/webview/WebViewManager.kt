package com.pratham.webhub.webview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewManager @Inject constructor(
    private val webViewFactory: WebViewFactory,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WebViewManager"
        const val DEFAULT_MAX_ACTIVE_WEBVIEWS = 5
    }

    private val tabWebViews = ConcurrentHashMap<String, WebView>()
    private val lastUsedTimes = ConcurrentHashMap<String, Long>()
    private val hibernatedStates = ConcurrentHashMap<String, Bundle>()
    private val hibernatedScrollY = ConcurrentHashMap<String, Int>()
    private val hibernatedUrls = ConcurrentHashMap<String, String>()

    var maxActiveWebViews: Int = DEFAULT_MAX_ACTIVE_WEBVIEWS

    /** The tab currently visible in the UI — never hibernate this. */
    @Volatile
    var protectedTabId: String? = null

    fun getOrCreateWebView(
        tabId: String,
        settings: WebViewFactory.TabWebViewSettings,
        url: String
    ): WebView {
        val existing = tabWebViews[tabId]
        if (existing != null) {
            lastUsedTimes[tabId] = System.currentTimeMillis()
            return existing
        }

        // Enforce max active WebViews by hibernating LRU tabs
        while (tabWebViews.size >= maxActiveWebViews) {
            hibernateLeastRecentlyUsed()
        }

        val webView = webViewFactory.createWebView(tabId, settings)
        tabWebViews[tabId] = webView
        lastUsedTimes[tabId] = System.currentTimeMillis()

        if (url.isNotBlank()) {
            webView.loadUrl(url)
        }

        Log.d(TAG, "Created WebView for tab: $tabId, active count: ${tabWebViews.size}")
        return webView
    }

    fun destroyWebView(tabId: String) {
        val webView = tabWebViews.remove(tabId)
        if (webView != null) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.webViewClient = object : WebViewClient() {}
            webView.webChromeClient = null
            webView.destroy()
            Log.d(TAG, "Destroyed WebView for tab: $tabId")
        }
        lastUsedTimes.remove(tabId)
        hibernatedStates.remove(tabId)
        hibernatedScrollY.remove(tabId)
        hibernatedUrls.remove(tabId)
    }

    fun destroyAllWebViews() {
        val tabIds = tabWebViews.keys.toList()
        for (tabId in tabIds) {
            destroyWebView(tabId)
        }
        hibernatedStates.clear()
        hibernatedScrollY.clear()
        hibernatedUrls.clear()
        Log.d(TAG, "Destroyed all WebViews")
    }

    fun getWebView(tabId: String): WebView? {
        return tabWebViews[tabId]?.also {
            lastUsedTimes[tabId] = System.currentTimeMillis()
        }
    }

    fun saveWebViewState(tabId: String): Bundle? {
        val webView = tabWebViews[tabId] ?: return null
        return try {
            val state = Bundle()
            val result = webView.saveState(state)
            if (result != null) {
                state.putInt("saved_scroll_y", webView.scrollY)
                state
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save state for tab: $tabId", e)
            null
        }
    }

    fun restoreWebViewState(tabId: String, state: Bundle?) {
        if (state == null) return
        val webView = tabWebViews[tabId] ?: return
        try {
            webView.restoreState(state)
            val scrollY = state.getInt("saved_scroll_y", 0)
            if (scrollY > 0) {
                webView.post { webView.scrollTo(0, scrollY) }
            }
            Log.d(TAG, "Restored state for tab: $tabId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to restore state for tab: $tabId", e)
        }
    }

    fun captureThumbnail(tabId: String, width: Int, height: Int): Bitmap? {
        val webView = tabWebViews[tabId] ?: return null
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            webView.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture thumbnail for tab: $tabId", e)
            null
        }
    }

    fun hibernateTab(tabId: String, scrollY: Int, onSaveState: (Bundle?) -> Unit) {
        val webView = tabWebViews[tabId] ?: return
        val state = saveWebViewState(tabId)
        hibernatedStates[tabId] = state ?: Bundle()
        hibernatedScrollY[tabId] = scrollY
        hibernatedUrls[tabId] = webView.url ?: ""
        onSaveState(state)

        // Destroy the WebView to free memory
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.webViewClient = object : WebViewClient() {}
        webView.webChromeClient = null
        webView.destroy()
        tabWebViews.remove(tabId)
        lastUsedTimes.remove(tabId)

        Log.d(TAG, "Hibernated tab: $tabId, hasState: ${state != null}")
    }

    fun restoreHibernatedTab(
        tabId: String,
        settings: WebViewFactory.TabWebViewSettings,
        url: String,
        scrollY: Int,
        state: Bundle?
    ): WebView {
        val savedState = state ?: hibernatedStates.remove(tabId)
        val savedScrollY = if (scrollY >= 0) scrollY else hibernatedScrollY.remove(tabId) ?: 0
        val savedUrl = if (url.isNotBlank()) url else hibernatedUrls.remove(tabId) ?: ""

        hibernatedStates.remove(tabId)
        hibernatedScrollY.remove(tabId)
        hibernatedUrls.remove(tabId)

        // Enforce max active WebViews
        while (tabWebViews.size >= maxActiveWebViews) {
            hibernateLeastRecentlyUsed()
        }

        val webView = webViewFactory.createWebView(tabId, settings)
        tabWebViews[tabId] = webView
        lastUsedTimes[tabId] = System.currentTimeMillis()

        if (savedState != null) {
            try {
                webView.restoreState(savedState)
                if (savedScrollY > 0) {
                    webView.post { webView.scrollTo(0, savedScrollY) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore hibernated state for tab: $tabId", e)
                if (savedUrl.isNotBlank()) {
                    webView.loadUrl(savedUrl)
                }
            }
        } else if (savedUrl.isNotBlank()) {
            webView.loadUrl(savedUrl)
        }

        Log.d(TAG, "Restored hibernated tab: $tabId, url: $savedUrl")
        return webView
    }

    fun onTrimMemory(level: Int) {
        when {
            level >= android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Critical: hibernate all but the most recently used tab
                hibernateAllExceptMostRecent(1)
            }
            level >= android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // Moderate: hibernate all but the 2 most recently used tabs
                hibernateAllExceptMostRecent(2)
            }
            level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // Running low: hibernate all but the 3 most recently used tabs
                hibernateAllExceptMostRecent(3)
            }
            level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI hidden: hibernate LRU tab if we have more than 3 active
                while (tabWebViews.size > 3) {
                    hibernateLeastRecentlyUsed()
                }
            }
        }
    }

    fun getActiveTabCount(): Int = tabWebViews.size

    fun getHibernatedTabIds(): Set<String> = hibernatedStates.keys

    fun hasHibernatedState(tabId: String): Boolean = hibernatedStates.containsKey(tabId)

    private fun hibernateLeastRecentlyUsed() {
        val lruTabId = lastUsedTimes.entries
            .filter { it.key != protectedTabId }
            .minByOrNull { it.value }?.key ?: return
        val webView = tabWebViews[lruTabId]
        if (webView != null) {
            hibernateTab(lruTabId, webView.scrollY) {}
        }
    }

    private fun hibernateAllExceptMostRecent(keepCount: Int) {
        if (tabWebViews.size <= keepCount) return

        val sortedTabs = lastUsedTimes.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val toHibernate = sortedTabs
            .filter { it != protectedTabId }
            .drop(keepCount)
        for (tabId in toHibernate) {
            val webView = tabWebViews[tabId]
            if (webView != null) {
                hibernateTab(tabId, webView.scrollY) {}
            }
        }
    }
}