package com.pratham.webhub.util

import android.net.Uri
import android.webkit.URLUtil
import java.net.MalformedURLException
import java.net.URL

/**
 * Normalises user input into either a navigable URL or a search query.
 *
 * Heuristics:
 * - If the input already looks like a URL (contains a dot, has an http(s)
 *   scheme, or is recognised by [URLUtil.isNetworkUrl]), it is normalised to HTTPS.
 * - Otherwise the input is treated as a search term.
 */
object UrlNormalizer {

    /**
     * The result of normalising user text-bar input.
     *
     * @param isSearch  `true` when the input was interpreted as a search query.
     * @param url       The fully-qualified URL to load.
     * @param searchTerm The raw search term when [isSearch] is `true`, else `null`.
     */
    data class UrlResult(
        val isSearch: Boolean,
        val url: String,
        val searchTerm: String?
    )

    private val URL_SCHEME_REGEX = Regex("^https?://", RegexOption.IGNORE_CASE)

    /**
     * Normalises [input] against the given [searchEngineUrl].
     */
    fun normalize(input: String, searchEngineUrl: String): UrlResult {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            // Empty input → new-tab page (or about:blank)
            return UrlResult(
                isSearch = false,
                url = "about:blank",
                searchTerm = null
            )
        }

        return if (looksLikeUrl(trimmed)) {
            UrlResult(
                isSearch = false,
                url = toHttps(trimmed),
                searchTerm = null
            )
        } else {
            val encodedQuery = Uri.encode(trimmed)
            UrlResult(
                isSearch = true,
                url = "${searchEngineUrl}$encodedQuery",
                searchTerm = trimmed
            )
        }
    }

    /**
     * Extracts the domain (e.g. "example.com") from a full URL.
     */
    fun getDomainFromUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Returns the Google Favicon API URL for the given page URL.
     *
     * @param url The page URL whose favicon is requested.
     * @param size Desired icon size in pixels (default 64).
     */
    fun getFaviconUrl(url: String, size: Int = 64): String {
        val domain = getDomainFromUrl(url)
        return "https://www.google.com/s2/favicons?domain=$domain&sz=$size"
    }

    /**
     * Returns `true` when [url] is a syntactically valid HTTP/HTTPS URL.
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val parsed = URL(url)
            parsed.protocol == "http" || parsed.protocol == "https"
        } catch (_: MalformedURLException) {
            false
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    /**
     * Heuristic check: does this look like a URL rather than a search query?
     *
     * A string is considered a URL when:
     * - It starts with http:// or https://
     * - It contains a '.' and no spaces (common domain pattern like example.com)
     * - It matches a known scheme (e.g. file://, about:)
     */
    private fun looksLikeUrl(input: String): Boolean {
        // Explicit scheme
        if (URL_SCHEME_REGEX.containsMatchIn(input)) return true

        // Known non-HTTP schemes
        val lower = input.lowercase()
        if (lower.startsWith("file://") || lower.startsWith("about:") || lower.startsWith("data:")) {
            return true
        }

        // URLUtil thinks it's a network URL
        if (URLUtil.isNetworkUrl(input)) return true

        // Heuristic: contains a dot, has no spaces, and isn't all numeric
        // (e.g. "example.com", "192.168.1.1", but NOT "hello world" or "3.14")
        if ("." in input && " " !in input) {
            val withoutDots = input.replace(".", "")
            // If everything else is a digit it might be an IP address – still a URL
            // but also allow typical domain-like inputs.
            // Reject single-word inputs without any letter (pure number like "123.456")
            if (withoutDots.all { it.isDigit() } && input.count { it == '.' } == 1) {
                // Could be a decimal number, not a URL
                return false
            }
            return true
        }

        return false
    }

    /**
     * Ensures the URL uses the HTTPS scheme.
     */
    private fun toHttps(input: String): String {
        // Already has a scheme – replace http with https
        if (URL_SCHEME_REGEX.containsMatchIn(input)) {
            return input.replaceFirst("http://", "https://", ignoreCase = true)
        }
        // No scheme → prepend https://
        val sanitized = if (input.startsWith("//")) "https:$input" else "https://$input"
        return sanitized
    }
}
