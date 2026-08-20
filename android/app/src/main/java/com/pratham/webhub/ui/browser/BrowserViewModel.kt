package com.pratham.webhub.ui.browser

import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.usecase.tab.UpdateTabUseCase
import com.pratham.webhub.util.UrlNormalizer
import com.pratham.webhub.webview.AdBlocker
import com.pratham.webhub.webview.WebHubChromeClient
import com.pratham.webhub.webview.WebHubWebViewClient
import com.pratham.webhub.webview.WebViewFactory
import com.pratham.webhub.webview.WebViewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class BrowserUiState(
    val loadingProgress: Int = 0,
    val isLoading: Boolean = false,
    val currentUrl: String = "",
    val currentTitle: String = "",
    val faviconUrl: String? = null,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isFullscreen: Boolean = false,
    val sslState: SslState = SslState.None
)

enum class SslState { None, Valid, Invalid }

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val webViewManager: WebViewManager,
    private val webViewFactory: WebViewFactory,
    private val adBlocker: AdBlocker,
    private val updateTabUseCase: UpdateTabUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "BrowserViewModel"
        private const val TAB_INFO_DEBOUNCE_MS = 500L
    }

    private val _state = MutableStateFlow(BrowserUiState())
    val state: StateFlow<BrowserUiState> = _state.asStateFlow()

    /** The WebView instance currently bound to the active tab. */
    private val _webView = MutableStateFlow<WebView?>(null)
    val webView: StateFlow<WebView?> = _webView.asStateFlow()

    /** Holds the custom (fullscreen) view for video playback. */
    private val _customView = MutableStateFlow<View?>(null)
    val customView: StateFlow<View?> = _customView.asStateFlow()

    /** The ChromeClient managing fullscreen callbacks for the current tab. */
    private var currentChromeClient: WebHubChromeClient? = null

    /** ID of the currently attached tab. */
    private var currentTabId: String? = null

    /** Debounce job for persisting tab info updates. */
    private var updateTabInfoJob: Job? = null

    // ── Attach / Detach ───────────────────────────────────────────────────

    /**
     * Creates (or retrieves) a [WebView] for the given [Tab], sets up
     * WebViewClient & WebChromeClient with proper callbacks, and exposes
     * it via [webView] for Compose to render.
     */
    fun attachTab(tabId: String, tab: Tab) {
        if (currentTabId == tabId) return // already attached

        // Save state of the previous WebView before switching
        detachTab()

        currentTabId = tabId

        val wvSettings = WebViewFactory.TabWebViewSettings(
            isJsEnabled = tab.isJsEnabled,
            isAdBlockEnabled = tab.isAdBlockEnabled,
            cssOverride = tab.cssOverride,
            userScript = tab.userScript
        )

        val webView = if (tab.isHibernated) {
            webViewManager.restoreHibernatedTab(
                tabId = tabId,
                settings = wvSettings,
                url = tab.url,
                scrollY = tab.savedScrollY,
                state = null // state managed by the repository
            )
        } else {
            webViewManager.getOrCreateWebView(
                tabId = tabId,
                settings = wvSettings,
                url = tab.url
            )
        }

        // Set up the WebViewClient
        webView.webViewClient = WebHubWebViewClient(
            tabId = tabId,
            onTitleChanged = { _, title -> onTitleChanged(tabId, title) },
            onUrlChanged = { _, url -> onUrlChanged(tabId, url) },
            onFaviconChanged = { _, favicon -> onFaviconChanged(tabId, favicon) },
            onPageFinished = { _ -> onPageFinished(tabId) },
            onError = { _, error -> onPageError(tabId, error) },
            adBlocker = adBlocker
        )

        // Set up the WebChromeClient
        val chromeClient = WebHubChromeClient(
            tabId = tabId,
            onProgressChanged = { _, progress -> onProgressChanged(progress) },
            onShowCustomView = { view, _ -> onShowCustomView(view) },
            onPermissionRequest = { request -> handlePermissionRequest(request) }
        )
        webView.webChromeClient = chromeClient
        currentChromeClient = chromeClient

        _webView.value = webView

        // Sync initial state
        _state.update {
            it.copy(
                currentUrl = webView.url ?: tab.url,
                currentTitle = webView.title ?: tab.title,
                faviconUrl = tab.faviconUrl,
                canGoBack = webView.canGoBack(),
                canGoForward = webView.canGoForward(),
                isLoading = webView.progress < 100,
                loadingProgress = webView.progress,
                sslState = deriveSslState(webView.url)
            )
        }
    }

    /**
     * Saves the current WebView's scroll position & state, then clears
     * the reference so the Compose layer can stop rendering it.
     */
    fun detachTab() {
        val tabId = currentTabId ?: return
        val wv = _webView.value ?: return

        // Save state in the background
        val savedState = webViewManager.saveWebViewState(tabId)
        if (savedState != null) {
            viewModelScope.launch {
                updateTabUseCase(
                    tabId = tabId,
                    url = wv.url,
                    title = wv.title
                )
            }
        }

        currentChromeClient = null
        currentTabId = null
        _webView.value = null
    }

    // ── Navigation actions ────────────────────────────────────────────────

    fun goBack() {
        _webView.value?.goBack()
    }

    fun goForward() {
        _webView.value?.goForward()
    }

    fun reload() {
        _webView.value?.reload()
    }

    fun stopLoading() {
        _webView.value?.stopLoading()
    }

    fun loadUrl(url: String) {
        val wv = _webView.value ?: return
        if (url == wv.url) return
        wv.loadUrl(url)
    }

    // ── Tab info persistence (debounced) ──────────────────────────────────

    /**
     * Called from WebViewClient callbacks. Persists URL/title/favicon to
     * Room with a debounce so we don't flood the database on every redirect.
     */
    fun updateTabInfo(tabId: String, url: String, title: String, faviconUrl: String?) {
        updateTabInfoJob?.cancel()
        updateTabInfoJob = viewModelScope.launch {
            delay(TAB_INFO_DEBOUNCE_MS)
            updateTabUseCase(tabId = tabId, url = url, title = title, faviconUrl = faviconUrl)
        }
    }

    // ── Fullscreen video ──────────────────────────────────────────────────

    fun exitFullscreen() {
        currentChromeClient?.releaseCustomView()
        _customView.value = null
        _state.update { it.copy(isFullscreen = false) }
    }

    // ── WebViewClient callbacks ───────────────────────────────────────────

    private fun onProgressChanged(progress: Int) {
        _state.update {
            it.copy(
                loadingProgress = progress,
                isLoading = progress < 100
            )
        }
    }

    private fun onTitleChanged(tabId: String, title: String) {
        _state.update { it.copy(currentTitle = title) }
        val url = _state.value.currentUrl
        if (url.isNotBlank()) {
            updateTabInfo(tabId, url, title, _state.value.faviconUrl)
        }
    }

    private fun onUrlChanged(tabId: String, url: String) {
        _state.update {
            it.copy(
                currentUrl = url,
                sslState = deriveSslState(url)
            )
        }
    }

    private fun onFaviconChanged(tabId: String, faviconUrl: String?) {
        _state.update { it.copy(faviconUrl = faviconUrl) }
    }

    private fun onPageFinished(tabId: String) {
        _state.update {
            it.copy(
                isLoading = false,
                loadingProgress = 100,
                canGoBack = _webView.value?.canGoBack() ?: false,
                canGoForward = _webView.value?.canGoForward() ?: false
            )
        }
        // Persist final URL/title
        val wv = _webView.value ?: return
        updateTabInfo(
            tabId = tabId,
            url = wv.url ?: "",
            title = wv.title ?: "",
            faviconUrl = _state.value.faviconUrl
        )
    }

    private fun onPageError(tabId: String, error: String) {
        Log.w(TAG, "Page error on tab $tabId: $error")
        _state.update { it.copy(isLoading = false) }
    }

    // ── ChromeClient callbacks ────────────────────────────────────────────

    private fun onShowCustomView(view: View) {
        _customView.value = view
        _state.update { it.copy(isFullscreen = true) }
    }

    private fun onHideCustomView() {
        _customView.value = null
        _state.update { it.copy(isFullscreen = false) }
    }

    private fun handlePermissionRequest(request: PermissionRequest) {
        // Default-deny: Veyla does not automatically grant WebView permissions.
        // A future implementation should:
        // 1. Map WebView resources to Android runtime permissions
        // 2. Launch an Android permission request dialog
        // 3. Only grant after the user explicitly approves
        request.deny()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun deriveSslState(url: String?): SslState {
        if (url.isNullOrBlank() || !url.startsWith("https://")) return SslState.None
        return SslState.Valid
    }

    // ── Cleanup ───────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Detach the current tab so its state is persisted
        detachTab()
    }
}
