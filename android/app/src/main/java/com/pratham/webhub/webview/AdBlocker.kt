package com.pratham.webhub.webview

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdBlocker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AdBlocker"
        private const val BLOCKLIST_ASSET = "adblock_hosts.txt"
    }

    private val blockedHosts = mutableSetOf<String>()
    private val customBlockedDomains = mutableSetOf<String>()
    private val blocklistLock = Any()

    var blockedCount: AtomicInteger = AtomicInteger(0)
        private set

    private var adBlockEnabled: Boolean = true

    init {
        loadBlocklist()
    }

    fun loadBlocklist() {
        synchronized(blocklistLock) {
            try {
                val inputStream = context.assets.open(BLOCKLIST_ASSET)
                inputStream.bufferedReader().useLines { lines ->
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            blockedHosts.add(normalizeHost(trimmed))
                        }
                    }
                }
                Log.d(TAG, "Loaded ${blockedHosts.size} hosts from blocklist")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load blocklist from assets: ${e.message}")
            }
        }
    }

    fun shouldBlock(url: String): Boolean {
        if (!adBlockEnabled) return false

        val host = extractHost(url) ?: return false
        val normalizedHost = normalizeHost(host)

        synchronized(blocklistLock) {
            // Exact host match
            if (blockedHosts.contains(normalizedHost) || customBlockedDomains.contains(normalizedHost)) {
                blockedCount.incrementAndGet()
                return true
            }

            // Check if the host or any parent domain is blocked
            // e.g., if "ads.example.com" is blocked, "tracker.ads.example.com" should also be blocked
            val parts = normalizedHost.split(".")
            if (parts.size > 2) {
                for (i in 1 until parts.size - 1) {
                    val parentDomain = parts.drop(i).joinToString(".")
                    if (blockedHosts.contains(parentDomain) || customBlockedDomains.contains(parentDomain)) {
                        blockedCount.incrementAndGet()
                        return true
                    }
                }
            }

            return false
        }
    }

    fun addCustomDomain(domain: String) {
        synchronized(blocklistLock) {
            customBlockedDomains.add(normalizeHost(domain))
            Log.d(TAG, "Added custom blocked domain: $domain")
        }
    }

    fun removeCustomDomain(domain: String) {
        synchronized(blocklistLock) {
            customBlockedDomains.remove(normalizeHost(domain))
            Log.d(TAG, "Removed custom blocked domain: $domain")
        }
    }

    fun getCustomBlockedDomains(): Set<String> {
        return synchronized(blocklistLock) {
            customBlockedDomains.toSet()
        }
    }

    fun getBlockedHostCount(): Int {
        return synchronized(blocklistLock) {
            blockedHosts.size + customBlockedDomains.size
        }
    }

    fun isAdBlockEnabled(): Boolean = adBlockEnabled

    fun setAdBlockEnabled(enabled: Boolean) {
        adBlockEnabled = enabled
        Log.d(TAG, "AdBlock ${if (enabled) "enabled" else "disabled"}")
    }

    fun resetBlockedCount() {
        blockedCount.set(0)
    }

    private fun extractHost(url: String): String? {
        return try {
            val parsed = URL(url)
            parsed.host
        } catch (e: Exception) {
            // Try simple extraction if URL parsing fails
            val hostMatch = Regex("^https?://([^/:]+)").find(url)
            hostMatch?.groupValues?.getOrNull(1)
        }
    }

    private fun normalizeHost(host: String): String {
        return host.lowercase().trim()
    }
}
