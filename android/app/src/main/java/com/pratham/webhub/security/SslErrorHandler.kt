package com.pratham.webhub.security

import android.net.http.SslError
import android.util.Log
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

    // SslError constant values (kept as ints for forward compat with API 36+)
    private const val SSL_NOT_YET_VALIDATED = 0
    private const val SSL_UNTRUSTED = 1
    private const val SSL_EXPIRED = 2
    private const val SSL_IDMISMATCH = 3
    private const val SSL_DATE_INVALID = 4
    private const val SSL_INVALID = 5

    /**
     * Handles an SSL error reported by the WebView.
     *
     * @param handler       The platform [AndroidSslErrorHandler] to proceed or cancel.
     * @param error         The [SslError] reported by the WebView client.
     * @param allowProceed When `true` the page will be loaded despite the error;
     *                    when `false` (default) the request is cancelled.
     */
    fun handleSslError(
        handler: AndroidSslErrorHandler,
        error: SslError,
        allowProceed: Boolean = false
    ) {
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
     * Converts an SslError error code to a human-readable string for logging.
     */
    fun sslErrorToString(error: Int): String = when (error) {
        SSL_NOT_YET_VALIDATED -> "SSL_NOTYETVALIDATED"
        SSL_UNTRUSTED -> "SSL_UNTRUSTED"
        SSL_EXPIRED -> "SSL_EXPIRED"
        SSL_IDMISMATCH -> "SSL_IDMISMATCH"
        SSL_DATE_INVALID -> "SSL_DATE_INVALID"
        SSL_INVALID -> "SSL_INVALID"
        else -> "UNKNOWN($error)"
    }
}
