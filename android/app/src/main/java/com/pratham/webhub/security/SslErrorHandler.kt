package com.pratham.webhub.security

import android.util.Log
import android.webkit.SslError
import android.webkit.SslErrorHandler as AndroidSslErrorHandler

/**
 * Centralised SSL error handling for WebView.
 *
 * By default, SSL errors are **not** allowed to proceed to protect
 * users from man-in-the-middle attacks.  Only callers that explicitly
 * opt in can override this behaviour.
 */
object SslErrorHandler {

    private const val TAG = "SslErrorHandler"

    /**
 * Handles an SSL error reported by the WebView.
 *
 * @param error         The [SslError] reported by the WebView client.
 * @param allowProceed When `true` the page will be loaded despite the error;
 *                    when `false` (default) the request is cancelled.
 */
    fun handleSslError(error: SslError, allowProceed: Boolean = false) {
        val handler = error.handler
        val url = error.url
        val primaryError = sslErrorToString(error.primaryError)

        if (allowProceed) {
            Log.w(
                TAG,
                "Proceeding past SSL error for $url – primary error: $primaryError"
            )
            handler.proceed()
        } else {
            Log.w(
                TAG,
                "Cancelling request to $url due to SSL error: $primaryError"
            )
            handler.cancel()
        }
    }

    /**
 * Converts an [SslError] error code to a human-readable string for logging.
 */
    fun sslErrorToString(error: Int): String = when (error) {
        SslError.SSL_NOTYETVALIDATED -> "SSL_NOTYETVALIDATED"
        SslError.SSL_UNTRUSTED -> "SSL_UNTRUSTED"
        SslError.SSL_EXPIRED -> "SSL_EXPIRED"
        SslError.SSL_IDMISMATCH -> "SSL_IDMISMATCH"
        SslError.SSL_DATE_INVALID -> "SSL_DATE_INVALID"
        else -> "UNKNOWN($error)"
    }
}
