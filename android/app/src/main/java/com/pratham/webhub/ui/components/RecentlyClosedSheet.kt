package com.pratham.webhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pratham.webhub.domain.model.ClosedTab
import com.pratham.webhub.util.UrlNormalizer

/**
 * Modal bottom sheet showing recently closed tabs with the ability to
 * restore individual tabs or clear the entire history.
 *
 * @param closedTabs  The list of [ClosedTab] entries to display.
 * @param onRestore   Called with the closed-tab's original tab ID when the
 *                    user taps to restore it.
 * @param onDismiss   Called to remove individual items or close the sheet.
 *                    For clearing a specific item, pass the tab ID;
 *                    this is handled by [onDismissItem].
 * @param onDismissItem  Called with the closed-tab ID to remove it from the list
 *                        (e.g. swipe-to-dismiss or delete button).
 * @param onClearAll  Called when the user taps "Clear all".
 * @param sheetState  Optional sheet state for controlling dismiss behaviour.
 * @param modifier    Optional [Modifier] applied to the sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyClosedSheet(
    closedTabs: List<ClosedTab>,
    onRestore: (tabId: String) -> Unit,
    onDismiss: () -> Unit,
    onDismissItem: (closedTabId: String) -> Unit,
    onClearAll: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Recently closed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (closedTabs.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text(
                            text = "Clear all",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

            // ── List or empty state ──────────────────────────────────────
            if (closedTabs.isEmpty()) {
                RecentlyClosedEmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = closedTabs,
                        key = { it.id }
                    ) { closedTab ->
                        SwipeToDismissClosedTab(
                            closedTab = closedTab,
                            onRestore = { onRestore(closedTab.tabId) },
                            onDismiss = { onDismissItem(closedTab.id) }
                        )
                    }
                }
            }
        }
    }
}

// ── Dismissable closed-tab row ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissClosedTab(
    closedTab: ClosedTab,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    // Red background that shows behind during swipe
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by androidx.compose.animation.animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ClosedTabListItem(
            closedTab = closedTab,
            onRestore = onRestore
        )
    }
}

// ── Individual closed-tab list item ──────────────────────────────────────────

@Composable
private fun ClosedTabListItem(
    closedTab: ClosedTab,
    onRestore: () -> Unit,
) {
    val displayTitle = closedTab.title.ifBlank {
        UrlNormalizer.getDomainFromUrl(closedTab.url)
    }
    val domain = remember(closedTab.url) {
        UrlNormalizer.getDomainFromUrl(closedTab.url)
    }
    val faviconUrl = closedTab.faviconUrl
        ?: UrlNormalizer.getFaviconUrl(closedTab.url, size = 64)
    val timeAgo = remember(closedTab.closedAt) {
        formatRelativeTime(closedTab.closedAt)
    }

    ListItem(
        headlineContent = {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = domain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            AsyncImage(
                model = faviconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        },
        trailingContent = {
            // Restore button
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onRestore)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore tab",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onRestore)
    )
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun RecentlyClosedEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No recently closed tabs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Closed tabs will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Relative time formatter ──────────────────────────────────────────────────

/**
 * Formats a [timestampMillis] epoch-millis into a human-readable relative
 * string such as "just now", "2 min ago", "1 hour ago", etc.
 */
private fun formatRelativeTime(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestampMillis

    val seconds = diffMs / 1000
    if (seconds < 60) return "just now"

    val minutes = seconds / 60
    if (minutes < 60) return "${minutes} min ago"

    val hours = minutes / 60
    if (hours < 24) return "${hours} hour${if (hours != 1L) "s" else ""} ago"

    val days = hours / 24
    if (days < 7) return "${days} day${if (days != 1L) "s" else ""} ago"

    val weeks = days / 7
    return "${weeks} week${if (weeks != 1L) "s" else ""} ago"
}
