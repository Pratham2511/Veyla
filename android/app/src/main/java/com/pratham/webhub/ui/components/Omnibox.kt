package com.pratham.webhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratham.webhub.ui.browser.SslState
import com.pratham.webhub.ui.theme.SslInvalid
import com.pratham.webhub.ui.theme.SslValid
import com.pratham.webhub.ui.theme.UrlBarBackgroundDark
import com.pratham.webhub.ui.theme.UrlBarBackgroundLight

/**
 * The URL/search bar (omnibox) displayed at the top of the browser screen.
 *
 * Shows the page title when idle, switches to an editable URL field when
 * tapped. Displays SSL status, navigation controls, bookmark toggle,
 * and a thin loading progress indicator below the bar.
 *
 * @param url            The current page URL.
 * @param title          The current page title.
 * @param isLoading      Whether the page is currently loading.
 * @param progress       The page load progress (0-100).
 * @param sslState       The current SSL certificate state.
 * @param canGoBack      Whether the WebView can navigate back.
 * @param canGoForward   Whether the WebView can navigate forward.
 * @param isIncognito    Whether the active tab is in incognito mode.
 * @param onUrlSubmit    Called when the user submits a URL or search query.
 * @param onBack         Called to navigate back in WebView history.
 * @param onForward      Called to navigate forward in WebView history.
 * @param onRefresh      Called to reload the current page.
 * @param onBookmarkToggle Called to toggle the bookmark status for the current page.
 * @param isBookmarked   Whether the current page is bookmarked.
 * @param onMenuClick    Called when the overflow menu icon is tapped.
 * @param modifier       Optional [Modifier] applied to the root container.
 */
@Composable
fun Omnibox(
    url: String,
    title: String,
    isLoading: Boolean,
    progress: Int,
    sslState: SslState,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isIncognito: Boolean,
    onUrlSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onBookmarkToggle: () -> Unit,
    isBookmarked: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = !MaterialTheme.colorScheme.isLight()

    // Edit state: when the user taps the bar, we enter edit mode
    var isEditing by remember { mutableStateOf(false) }
    var textFieldValue by remember(url) {
        mutableStateOf(
            TextFieldValue(
                text = if (isEditing) url else url,
                selection = androidx.compose.ui.text.TextRange(url.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    // Enter edit mode and select all text
    val onBarClick: () -> Unit = {
        isEditing = true
        textFieldValue = TextFieldValue(
            text = url,
            selection = androidx.compose.ui.text.TextRange(0, url.length)
        )
    }

    // Submit URL / search
    val onSubmit: () -> Unit = {
        val submitted = textFieldValue.text.trim()
        if (submitted.isNotBlank()) {
            onUrlSubmit(submitted)
        }
        isEditing = false
        focusRequester.freeFocus()
    }

    // Cancel editing
    val onCancelEdit: () -> Unit = {
        isEditing = false
        textFieldValue = TextFieldValue(text = url)
        focusRequester.freeFocus()
    }

    // Sync the text field when not editing (page navigates in the background)
    LaunchedEffect(url) {
        if (!isEditing) {
            textFieldValue = TextFieldValue(text = url)
        }
    }

    // Display text: show title when idle, URL when editing or loading
    val displayText = when {
        isEditing -> textFieldValue.text
        isLoading -> url
        title.isNotBlank() -> title
        else -> url
    }

    // Bar background color: incognito tint, or standard URL bar color
    val barBackgroundColor by animateColorAsState(
        targetValue = when {
            isIncognito -> if (isDarkTheme) {
                Color(0xFF1A1A2E)
            } else {
                Color(0xFF2D2D44)
            }
            isDarkTheme -> UrlBarBackgroundDark
            else -> UrlBarBackgroundLight
        },
        label = "omnibox_bg"
    )

    val contentColor = if (isIncognito) {
        Color(0xFFE0E0E0)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // SSL icon & color
    val sslIcon: ImageVector
    val sslColor: Color
    when (sslState) {
        SslState.Valid -> {
            sslIcon = Icons.Default.Https
            sslColor = SslValid
        }
        SslState.Invalid -> {
            sslIcon = Icons.Default.Warning
            sslColor = SslInvalid
        }
        SslState.None -> {
            sslIcon = Icons.Default.Search
            sslColor = contentColor.copy(alpha = 0.5f)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Main bar row
        Surface(
            color = barBackgroundColor,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = if (isIncognito) 2.dp else 0.dp,
            shadowElevation = if (isIncognito) 4.dp else 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 4.dp)
            ) {
                // ── Back button ────────────────────────────────────────
                IconButton(
                    onClick = onBack,
                    enabled = canGoBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = if (canGoBack) contentColor else contentColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // ── Forward button ────────────────────────────────────
                IconButton(
                    onClick = onForward,
                    enabled = canGoForward,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go forward",
                        tint = if (canGoForward) contentColor else contentColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // ── SSL / Search icon ─────────────────────────────────
                Icon(
                    imageVector = sslIcon,
                    contentDescription = when (sslState) {
                        SslState.Valid -> "Secure connection"
                        SslState.Invalid -> "Insecure connection"
                        SslState.None -> "Search or enter URL"
                    },
                    tint = sslColor,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 2.dp)
                )

                // ── URL / Title text field ────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isEditing) {
                                Modifier.background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(!isEditing, onClick = onBarClick)
                        .padding(horizontal = 8.dp)
                        .height(36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isEditing) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .focusable(),
                            textStyle = TextStyle(
                                color = contentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = { onSubmit() }
                            ),
                            singleLine = true,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = if (isLoading) FontWeight.Medium else FontWeight.Normal,
                                color = contentColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Clear button shown when editing and text is non-blank
                    if (isEditing && textFieldValue.text.isNotBlank()) {
                        IconButton(
                            onClick = { textFieldValue = TextFieldValue(text = "") },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = contentColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── Refresh / Stop button ─────────────────────────────
                IconButton(
                    onClick = { if (isLoading) onRefresh() else onRefresh() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = if (isLoading) "Stop loading" else "Refresh",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // ── Bookmark toggle ──────────────────────────────────
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = if (isBookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            contentColor
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                // ── Overflow menu ─────────────────────────────────────
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Progress indicator ────────────────────────────────────────
        if (isLoading && progress < 100) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .padding(horizontal = 16.dp),
                color = if (isIncognito) {
                    Color(0xFF7C7CFF)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ── Utility ───────────────────────────────────────────────────────────────────

/**
 * Returns `true` when the current [MaterialTheme.colorScheme] is a dark scheme.
 */
private fun androidx.compose.material3.ColorScheme.isLight(): Boolean {
    // A simple heuristic: if the surface luminance is high, it's a light theme.
    // In practice we compare background against a known light color.
    return this.background.luminance() > 0.5f
}

/**
 * Computes the relative luminance of this [Color] (simplified Rec. 709).
 */
private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
