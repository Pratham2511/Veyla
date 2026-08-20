package com.pratham.webhub.util

import android.net.Uri

/**
 * Provides built-in search engine definitions and helper methods for
 * constructing search URLs.
 */
object SearchEngineHelper {

    // ------------------------------------------------------------------
    // Built-in search engines
    // ------------------------------------------------------------------

    const val GOOGLE = "https://www.google.com/search?q="
    const val DUCK_DUCK_GO = "https://duckduckgo.com/?q="
    const val BING = "https://www.bing.com/search?q="
    const val BRAVE = "https://search.brave.com/search?q="

    /**
 * Returns all built-in search engines as a list of `(displayName, searchUrl)` pairs.
 */
    fun getAvailableEngines(): List<Pair<String, String>> = listOf(
        "Google" to GOOGLE,
        "DuckDuckGo" to DUCK_DUCK_GO,
        "Bing" to BING,
        "Brave" to BRAVE
    )

    /**
 * Builds a full search URL by appending the encoded [query] to the
 * given [engineUrl].
 */
    fun getSearchUrl(engineUrl: String, query: String): String {
        val encoded = Uri.encode(query)
        return "${engineUrl}$encoded"
    }

    /**
 * Resolves a display name back to its search URL.
 * Returns `null` if the name doesn't match a built-in engine.
 */
    fun getEngineUrlByName(name: String): String? {
        return getAvailableEngines().firstOrNull { it.first.equals(name, ignoreCase = true) }?.second
    }

    /**
 * Resolves a search URL back to its display name.
 * Returns `null` if the URL doesn't match a built-in engine.
 */
    fun getEngineNameByUrl(url: String): String? {
        return getAvailableEngines().firstOrNull { it.second == url }?.first
    }

    /**
 * Returns the default search engine URL (Google).
 */
    fun getDefaultEngineUrl(): String = GOOGLE
}
