package com.pratham.webhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.ui.theme.TabBarBackgroundDark
import com.pratham.webhub.ui.theme.TabBarBackgroundLight
import com.pratham.webhub.util.UrlNormalizer

/**
 * A horizontally scrollable strip of tab chips rendered at the bottom
 * of the browser screen. Each chip shows the tab's favicon, a truncated
 * title, a close button, and visual indicators for hibernated
 * tabs. An "add tab" FAB is pinned at the trailing edge.
 *
 * @param tabs           The list of tabs to display.
 * @param activeTabId    The ID of the currently active tab (highlighted).
 * @param onTabSelected  Called when a tab chip is tapped.
 * @param onTabClosed    Called when a tab's close button is tapped.
 * @param onTabLongClick Called when a tab chip is long-pressed.
 * @param onAddTab       Called when the "+" FAB is tapped.
 * @param modifier       Optional [Modifier] applied to the root.
 */
@Composable
fun TabStrip(
    tabs: List<Tab>,
    activeTabId: String?,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onTabLongClick: (String) -> Unit,
    onAddTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = !MaterialTheme.colorScheme.isLight()
    val listState = rememberLazyListState()

    // Auto-scroll to the active tab when it changes
    val activeIndex by remember(tabs, activeTabId) {
        derivedStateOf { tabs.indexOfFirst { it.id == activeTabId } }
    }
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    val stripBackgroundColor = if (isDarkTheme) TabBarBackgroundDark else TabBarBackgroundLight

    Surface(
        color = stripBackgroundColor,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(start = 8.dp, end = 64.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = tabs,
                    key = { it.id }
                ) { tab ->
                    TabChip(
                        tab = tab,
                        isActive = tab.id == activeTabId,
                        isDarkTheme = isDarkTheme,
                        onSelected = { onTabSelected(tab.id) },
                        onClosed = { onTabClosed(tab.id) },
                        onLongClick = { onTabLongClick(tab.id) }
                    )
                }
            }

            // Pinned FAB at the trailing edge
            FloatingActionButton(
                onClick = onAddTab,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(40.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New tab",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Individual Tab Chip ──────────────────────────────────────────────────────

/**
 * A single tab card within the [TabStrip].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabChip(
    tab: Tab,
    isActive: Boolean,
    isDarkTheme: Boolean,
    onSelected: () -> Unit,
    onClosed: () -> Unit,
    onLongClick: () -> Unit,
) {
    val displayTitle = tab.customName ?: tab.title.ifBlank {
        UrlNormalizer.getDomainFromUrl(tab.url)
    }

    // Card colors: active tab gets primary accent, inactive is surface
    val cardContainerColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "tab_chip_bg"
    )

    val cardContentColor = when {
        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = cardContentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        ),
        modifier = Modifier
            .height(44.dp)
            .combinedClickable(
                onClick = onSelected,
                onLongClick = onLongClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 6.dp, end = 2.dp)
        ) {
            // ── Hibernated indicator ──────────────────────────────────
            if (tab.isHibernated) {
                Icon(
                    imageVector = Icons.Default.Hotel,
                    contentDescription = "Hibernated",
                    tint = cardContentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer4()
            } else {
                // ── Favicon ──────────────────────────────────────────
                val faviconUrl = tab.faviconUrl
                    ?: if (tab.url.isNotBlank() && !tab.url.startsWith("about:")) {
                        UrlNormalizer.getFaviconUrl(tab.url, size = 32)
                    } else null

                if (faviconUrl != null) {
                    AsyncImage(
                        model = faviconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                } else {
                    // Placeholder circle for tabs without a favicon
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                cardContentColor.copy(alpha = 0.15f)
                            )
                    ) {
                        Text(
                            text = displayTitle.take(1).uppercase(),
                            color = cardContentColor.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer4()
            }

            // ── Title ────────────────────────────────────────────────
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.SemiBold
                    else androidx.compose.ui.text.font.FontWeight.Normal
                ),
                color = cardContentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            // ── Close button ─────────────────────────────────────────
            Surface(
                color = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onClosed)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    tint = cardContentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// ── Tiny spacing helper ──────────────────────────────────────────────────────

@Composable
private fun Spacer4() {
    Spacer(Modifier.width(4.dp))
}

// ── Utility ───────────────────────────────────────────────────────────────────

private fun androidx.compose.material3.ColorScheme.isLight(): Boolean {
    return this.background.luminance() > 0.5f
}

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
