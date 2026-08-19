package com.pratham.webhub.webview

import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient

private const val TAG = "WebHubChromeClient"

class WebHubChromeClient(
    private val tabId: String,
    private val onProgressChanged: (tabId: String, progress: Int) -> Unit,
    private val onShowCustomView: (view: View, callback: WebChromeClient.CustomViewCallback) -> Unit,
    private val onHideCustomView: () -> Unit,
    private val onPermissionRequest: (permissionRequest: PermissionRequest) -> Unit
) : WebChromeClient() {

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(tabId, newProgress)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (customView != null) {
            // Already showing a custom view; hide the previous one first
            onHideCustomView()
        }
        if (view != null && callback != null) {
            customView = view
            customViewCallback = callback
            onShowCustomView(view, callback)
        }
    }

    override fun onHideCustomView() {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
        }
        onHideCustomView()
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        if (request != null) {
            onPermissionRequest(request)
        } else {
            request?.deny()
        }
    }

    override fun onCreateWindow(
        view: android.webkit.WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: android.os.Message?
    ): Boolean {
        // Block popups by default
        return true
    }

    override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
        if (message != null) {
            when (message.messageLevel()) {
                ConsoleMessage.MessageLevel.ERROR -> Log.e(TAG, "[${tabId}] ${message.sourceId()}:${message.lineNumber()} - ${message.message()}")
                ConsoleMessage.MessageLevel.WARNING -> Log.w(TAG, "[${tabId}] ${message.sourceId()}:${message.lineNumber()} - ${message.message()}")
                else -> Log.d(TAG, "[${tabId}] ${message.sourceId()}:${message.lineNumber()} - ${message.message()}")
            }
        }
        return true
    }

    fun isCustomViewShowing(): Boolean = customView != null

    fun getCustomView(): View? = customView
}