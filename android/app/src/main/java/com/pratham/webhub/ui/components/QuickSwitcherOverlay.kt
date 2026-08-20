package com.pratham.webhub.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import coil.compose.AsyncImage
import com.pratham.webhub.domain.model.Tab
import java.net.URI
import java.net.URISyntaxException

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * A floating search overlay (Spotlight / Android Quick-Search style).
 *
 * Displays a centered search field over a semi-transparent dark backdrop.
 * The user can filter tabs by title or URL, and select one with keyboard
 * navigation (up / down arrows, enter) or by tapping.
 *
 * @param query          The current search query.
 * @param tabs           The (already filtered) list of tabs to display.
 * @param activeTabId    The ID of the currently active tab, highlighted in the list.
 * @param onQueryChanged Called when the query text changes.
 * @param onTabSelected  Called with the tab ID when a tab is selected.
 * @param onDismiss      Called when the overlay should close (Escape, tap outside, etc.).
 */
@Composable
fun QuickSwitcherOverlay(
    query: String,
    tabs: List<Tab>,
    activeTabId: String?,
    onQueryChanged: (String) -> Unit,
    onTabSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // Keyboard focus state
    val focusRequester = remember { FocusRequester() }
    var highlightedIndex by remember { mutableIntStateOf(0) }

    // Clamp highlighted index when the list changes
    LaunchedEffect(tabs.size) {
        if (highlightedIndex >= tabs.size) {
            highlightedIndex = (tabs.size - 1).coerceAtLeast(0)
        }
    }

    // Auto-focus the search field when the overlay appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Reset highlight when query changes
    LaunchedEffect(query) {
        highlightedIndex = 0
    }

    // Animation for the overlay appearance
    val overlayAlpha by animateFloatAsState(
        targetValue = 1f,
        label = "overlay_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * overlayAlpha))
            .clickable { onDismiss() }
    ) {
        // Centered search card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .onPreviewKeyEvent { event ->
                    when {
                        // Escape to dismiss
                        event.type == KeyEventType.KeyDown &&
                                event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ESCAPE -> {
                            onDismiss()
                            true
                        }

                        // Arrow down
                        event.type == KeyEventType.KeyDown &&
                                event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (highlightedIndex < tabs.size - 1) {
                                highlightedIndex++
                            }
                            true
                        }

                        // Arrow up
                        event.type == KeyEventType.KeyDown &&
                                event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                            if (highlightedIndex > 0) {
                                highlightedIndex--
                            }
                            true
                        }

                        // Enter to select
                        event.type == KeyEventType.KeyDown &&
                                event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER -> {
                            if (tabs.isNotEmpty() && highlightedIndex in tabs.indices) {
                                onTabSelected(tabs[highlightedIndex].id)
                            }
                            true
                        }

                        else -> false
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { /* consume clicks */ }
                    .padding(16.dp)
            ) {
                // ── Search field ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = onQueryChanged,
                        placeholder = {
                            Text(
                                "Search tabs...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Tab list ───────────────────────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))

                if (tabs.isEmpty()) {
                    // No results
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isBlank()) "No open tabs" else "No tabs match \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = tabs,
                            key = { it.id }
                        ) { tab ->
                            val tabIndex = tabs.indexOf(tab)
                            val isHighlighted = tabIndex == highlightedIndex
                            val isActive = tab.id == activeTabId

                            QuickSwitcherTabItem(
                                tab = tab,
                                isActive = isActive,
                                isHighlighted = isHighlighted,
                                onClick = { onTabSelected(tab.id) }
                            )
                        }
                    }
                }

                // ── Keyboard hint ──────────────────────────────────────────
                if (tabs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "↑↓ Navigate  •  Enter to select  •  Esc to close",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun QuickSwitcherTabItem(
    tab: Tab,
    isActive: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    val domain = remember(tab.url) { extractDomain(tab.url) }
    val displayName = tab.customName ?: tab.title.ifBlank { domain }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isHighlighted) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favicon or globe icon
            if (tab.faviconUrl != null) {
                AsyncImage(
                    model = tab.faviconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = domain.take(1).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & domain
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isHighlighted) {
                            androidx.compose.ui.text.font.FontWeight.SemiBold
                        } else {
                            androidx.compose.ui.text.font.FontWeight.Normal
                        },
                        color = if (isHighlighted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────

/**
 * Extracts the host/domain from a URL string.
 * Falls back to the raw URL if parsing fails.
 */
private fun extractDomain(url: String): String {
    return try {
        val uri = URI(url)
        uri.host ?: url
    } catch (_: URISyntaxException) {
        url
    }
}
