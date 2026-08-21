package com.pratham.webhub.ui.main

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.ui.addtab.AddTabSheet
import com.pratham.webhub.ui.browser.BrowserUiState
import com.pratham.webhub.ui.browser.BrowserViewModel
import com.pratham.webhub.ui.components.Omnibox
import com.pratham.webhub.ui.components.QuickSwitcherOverlay
import com.pratham.webhub.ui.components.RecentlyClosedSheet
import com.pratham.webhub.ui.components.TabStrip
import com.pratham.webhub.ui.overview.TabOverviewScreen
import com.pratham.webhub.ui.workspace.WorkspaceSwitcherSheet
import com.pratham.webhub.util.UrlNormalizer
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// MainScreen – the primary browser screen that orchestrates the entire app.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The top-level browser screen that hosts the [Omnibox], WebView content,
 * [TabStrip], and all overlay bottom-sheets / dialogs.
 *
 * @param initialUrl          An optional URL from a share-intent or deep link.
 * @param onNavigateToBookmarks Callback to navigate to the bookmarks screen.
 * @param onNavigateToSettings  Callback to navigate to the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainScreen(
    initialUrl: String? = null,
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel(),
    browserViewModel: BrowserViewModel = hiltViewModel(),
) {
    val uiState by mainViewModel.state.collectAsStateWithLifecycle()
    val browserState by browserViewModel.state.collectAsStateWithLifecycle()
    val webView by browserViewModel.webView.collectAsStateWithLifecycle()
    val customView by browserViewModel.customView.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Handle initial URL from share intent / deep link ────────────
    val initialUrlHandled = remember { mutableStateOf(false) }
    LaunchedEffect(initialUrl) {
        if (!initialUrlHandled.value && !initialUrl.isNullOrBlank()) {
            mainViewModel.onEvent(MainEvent.AddTab(initialUrl, null))
            initialUrlHandled.value = true
        }
    }

    // ── Track active tab changes and attach to browser ──────────────
    //
    // IMPORTANT: We deliberately do NOT call detachTab() when activeTab
    // becomes null. During tab creation there is a brief moment where
    // activeTabId is set but the tab-list Flow hasn't emitted yet, so
    // activeTab is transiently null. Calling detachTab() in that window
    // would clear _webView.value prematurely and cause the Compose layer
    // to flicker / lose the WebView reference. The original code (which
    // worked) only called attachTab when activeTab was non-null, and
    // relied on attachTab's internal `if (currentTabId == tabId) return`
    // guard to prevent double-attach. We preserve that behavior.
    val activeTab = uiState.activeTab
    LaunchedEffect(activeTab?.id) {
        if (activeTab != null && !activeTab.isHibernated) {
            browserViewModel.attachTab(activeTab.id, activeTab)
        }
    }

    // Refresh bookmark status when the active tab URL changes
    LaunchedEffect(activeTab?.url) {
        activeTab?.let { mainViewModel.refreshBookmarkStatus(it.url) }
    }

    // ── Drawer state for workspace rail ─────────────────────────────
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // ── Menu expansion state ────────────────────────────────────────
    var showOverflowMenu by remember { mutableStateOf(false) }

    // ═══════════════════════════════════════════════════════════════════
    // Back-press handling: sheets > WebView.goBack > close tab
    // ═══════════════════════════════════════════════════════════════════
    BackHandler(enabled = true) {
        when {
            uiState.showQuickSwitcher ||
            uiState.showWorkspaceSwitcher ||
            uiState.showAddTabSheet ||
            uiState.showRecentlyClosed -> {
                mainViewModel.onEvent(MainEvent.DismissAll)
            }
            uiState.showTabOverview -> {
                mainViewModel.onEvent(MainEvent.DismissAll)
            }
            showOverflowMenu -> {
                showOverflowMenu = false
            }
            browserState.canGoBack -> {
                browserViewModel.goBack()
            }
            activeTab != null -> {
                // User pressed back on a tab with no WebView history.
                // Close the tab explicitly so its WebView is destroyed.
                browserViewModel.destroyTab(activeTab.id)
                mainViewModel.onEvent(MainEvent.CloseTab(activeTab.id))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Fullscreen video overlay
    // ═══════════════════════════════════════════════════════════════════
    val fullscreenView = customView
    if (fullscreenView != null) {
        FullscreenVideoOverlay(
            customView = fullscreenView,
            onExitFullscreen = { browserViewModel.exitFullscreen() }
        )
        return
    }

    // ═══════════════════════════════════════════════════════════════════
    // Quick Switcher overlay
    // ═══════════════════════════════════════════════════════════════════
    if (uiState.showQuickSwitcher) {
        QuickSwitcherOverlay(
            query = uiState.quickSwitcherQuery,
            tabs = uiState.filteredTabs,
            activeTabId = uiState.activeTabId,
            onQueryChanged = { mainViewModel.onEvent(MainEvent.UpdateQuickSwitcherQuery(it)) },
            onTabSelected = {
                mainViewModel.onEvent(MainEvent.SelectTab(it))
                mainViewModel.onEvent(MainEvent.DismissAll)
            },
            onDismiss = { mainViewModel.onEvent(MainEvent.DismissAll) }
        )
        return
    }

    // ═══════════════════════════════════════════════════════════════════
    // Tab Overview (fullscreen grid)
    // ═══════════════════════════════════════════════════════════════════
    if (uiState.showTabOverview) {
        TabOverviewScreen(
            viewModel = hiltViewModel(),
            onTabSelected = {
                mainViewModel.onEvent(MainEvent.SelectTab(it))
                mainViewModel.onEvent(MainEvent.DismissAll)
            },
            onDismiss = { mainViewModel.onEvent(MainEvent.DismissAll) },
            onAddTab = { mainViewModel.onEvent(MainEvent.ShowAddTab) }
        )
        return
    }

    // ═══════════════════════════════════════════════════════════════════
    // Main browser layout
    // ═══════════════════════════════════════════════════════════════════
    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable the drawer's edge-swipe gesture so it doesn't compete
        // with vertical page scrolling in the WebView. The drawer is
        // reachable via the folder button in the header.
        gesturesEnabled = false,
        drawerContent = {
            WorkspaceDrawerContent(
                workspaces = uiState.workspaces,
                activeWorkspaceId = uiState.activeWorkspaceId,
                onWorkspaceSelected = { wsId ->
                    mainViewModel.onEvent(MainEvent.SwitchWorkspace(wsId))
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToBookmarks = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToBookmarks()
                },
                onNavigateToSettings = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onNavigateToHistory = {
                    mainViewModel.onEvent(MainEvent.ShowRecentlyClosed)
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier
                    .navigationBarsPadding(),
                topBar = {
                    MainTopBar(
                        uiState = uiState,
                        browserState = browserState,
                        onUrlSubmit = { input ->
                            val result = UrlNormalizer.normalize(
                                input,
                                uiState.settings.searchEngineUrl
                            )
                            browserViewModel.loadUrl(result.url)
                        },
                        onBack = { browserViewModel.goBack() },
                        onForward = { browserViewModel.goForward() },
                        onRefresh = {
                            if (browserState.isLoading) browserViewModel.stopLoading()
                            else browserViewModel.reload()
                        },
                        onBookmarkToggle = {
                            val tab = activeTab ?: return@MainTopBar
                            mainViewModel.onEvent(
                                MainEvent.ToggleBookmark(
                                    url = tab.url,
                                    title = tab.title,
                                    faviconUrl = tab.faviconUrl
                                )
                            )
                        },
                        onMenuClick = { showOverflowMenu = true },
                        onDrawerClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )
                },
                bottomBar = {
                    if (uiState.tabs.isNotEmpty()) {
                        TabStrip(
                            tabs = uiState.tabs,
                            activeTabId = uiState.activeTabId,
                            onTabSelected = { mainViewModel.onEvent(MainEvent.SelectTab(it)) },
                            onTabClosed = { tabId ->
                                // Destroy the WebView BEFORE the DB delete so
                                // the manager's map and the Compose layer both
                                // drop the reference deterministically. This
                                // is the ONLY place destroyTab is called from
                                // (besides the back-press handler above) —
                                // never as a side-effect of recomposition.
                                browserViewModel.destroyTab(tabId)
                                mainViewModel.onEvent(MainEvent.CloseTab(tabId))
                            },
                            onTabLongClick = {
                                mainViewModel.onEvent(MainEvent.ShowTabOverview)
                            },
                            onAddTab = { mainViewModel.onEvent(MainEvent.ShowAddTab) }
                        )
                    }
                },
                floatingActionButton = {
                    // Only show the FAB when there are zero tabs. Once the
                    // tab strip exists, its own trailing-edge "+" is the
                    // single source of truth for adding a tab. This avoids
                    // duplicate "+" buttons.
                    if (uiState.tabs.isEmpty()) {
                        ExtendedNewTabFab(
                            onClick = { mainViewModel.onEvent(MainEvent.ShowAddTab) },
                            onLongClick = { mainViewModel.onEvent(MainEvent.ShowQuickSwitcher) }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            ) { paddingValues ->

                // ── WebView content area ────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val currentWebView = webView
                    if (currentWebView != null && activeTab != null && !activeTab.isHibernated) {
                        // key() forces AndroidView to recreate when switching tabs,
                        // ensuring the correct WebView is attached to the composition tree.
                        key(activeTab.id) {
                            AndroidView(
                                factory = { ctx ->
                                    // Safety: remove from any stale parent before re-attaching
                                    (currentWebView.parent as? android.view.ViewGroup)?.removeView(currentWebView)
                                    currentWebView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (activeTab?.isHibernated == true) {
                        HibernatedTabPlaceholder(
                            tab = activeTab,
                            onWake = {
                                mainViewModel.onEvent(MainEvent.SelectTab(activeTab.id))
                            }
                        )
                    } else {
                        HomePlaceholder(
                            activeWorkspaceName = uiState.activeWorkspace?.name,
                            onSearch = { query ->
                                val result = UrlNormalizer.normalize(
                                    query,
                                    uiState.settings.searchEngineUrl
                                )
                                if (activeTab != null) {
                                    browserViewModel.loadUrl(result.url)
                                } else {
                                    mainViewModel.onEvent(MainEvent.AddTab(result.url, null))
                                }
                            },
                            onNavigateToBookmarks = onNavigateToBookmarks
                        )
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Overflow dropdown menu
    // ═══════════════════════════════════════════════════════════════════
    if (showOverflowMenu) {
        OverflowMenuDialog(
            onDismiss = { showOverflowMenu = false },
            onTabOverview = {
                showOverflowMenu = false
                mainViewModel.onEvent(MainEvent.ShowTabOverview)
            },
            onNewTab = {
                showOverflowMenu = false
                mainViewModel.onEvent(MainEvent.ShowAddTab)
            },
            onBookmarks = {
                showOverflowMenu = false
                onNavigateToBookmarks()
            },
            onHistory = {
                showOverflowMenu = false
                mainViewModel.onEvent(MainEvent.ShowRecentlyClosed)
            },
            onSettings = {
                showOverflowMenu = false
                onNavigateToSettings()
            }
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // Bottom sheets
    // ═══════════════════════════════════════════════════════════════════
    if (uiState.showWorkspaceSwitcher) {
        WorkspaceSwitcherSheet(
            viewModel = hiltViewModel(),
            onDismiss = { mainViewModel.onEvent(MainEvent.DismissAll) }
        )
    }

    if (uiState.showAddTabSheet) {
        AddTabSheet(
            viewModel = hiltViewModel(),
            onTabCreated = { params ->
                mainViewModel.onEvent(MainEvent.AddTab(params.url, params.customName))
                mainViewModel.onEvent(MainEvent.DismissAll)
            },
            onDismiss = { mainViewModel.onEvent(MainEvent.DismissAll) }
        )
    }

    if (uiState.showRecentlyClosed) {
        RecentlyClosedSheet(
            closedTabs = emptyList(),
            onRestore = { mainViewModel.onEvent(MainEvent.RestoreTab(it)) },
            onDismiss = { mainViewModel.onEvent(MainEvent.DismissAll) },
            onDismissItem = {},
            onClearAll = {}
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compact header: drawer button + tab-count chip + Omnibox (NO duplicate menu)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compact header that replaces the previous (empty-title) TopAppBar.
 *
 * The old layout wasted ~64dp of vertical space above the Omnibox because
 * the TopAppBar rendered an empty `title` slot plus the default status-bar
 * inset. This version collapses everything into a single status-bar-padded
 * Column with a small icon row + the Omnibox.
 *
 * NOTE: The Omnibox already has its own trailing three-dot overflow menu
 * button. We deliberately do NOT add a second MoreVert button here —
 * doing so would create the duplicate-menu issue reported in Phase 11.
 * The only overflow menu for browser actions lives inside the Omnibox.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    uiState: MainUiState,
    browserState: BrowserUiState,
    onUrlSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onMenuClick: () -> Unit,
    onDrawerClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // ── Top icon row: drawer button + tab-count chip ────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onDrawerClick) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Workspaces",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tab-count chip (informational only — no action)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "Tabs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${uiState.tabs.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            // NOTE: No second MoreVert here — the Omnibox below has the
            // single overflow menu button. Adding one here would duplicate
            // the three-dot menu (Phase 11 regression).
        }

        // ── Omnibox below the icon row ──────────────────────────────
        Omnibox(
            url = browserState.currentUrl,
            title = browserState.currentTitle,
            isLoading = browserState.isLoading,
            progress = browserState.loadingProgress,
            sslState = browserState.sslState,
            canGoBack = browserState.canGoBack,
            canGoForward = browserState.canGoForward,
            onUrlSubmit = onUrlSubmit,
            onBack = onBack,
            onForward = onForward,
            onRefresh = onRefresh,
            onBookmarkToggle = onBookmarkToggle,
            isBookmarked = uiState.isBookmarked,
            onMenuClick = onMenuClick,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Workspace Drawer Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WorkspaceDrawerContent(
    workspaces: List<Workspace>,
    activeWorkspaceId: String?,
    onWorkspaceSelected: (String) -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Header
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Veyla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Workspaces",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Workspace list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(workspaces, key = { it.id }) { workspace ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = workspace.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = workspace.id == activeWorkspaceId,
                    onClick = { onWorkspaceSelected(workspace.id) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Bottom navigation items
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Bookmarks") },
            selected = false,
            onClick = onNavigateToBookmarks,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Recently Closed") },
            selected = false,
            onClick = onNavigateToHistory,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Settings") },
            selected = false,
            onClick = onNavigateToSettings,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FAB: New Tab / Quick Switcher
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExtendedNewTabFab(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New tab (long-press for quick switcher)",
            modifier = Modifier.size(24.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fullscreen video overlay
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FullscreenVideoOverlay(
    customView: android.view.View,
    onExitFullscreen: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).also { frameLayout ->
                    (customView.parent as? ViewGroup)?.removeView(customView)
                    frameLayout.addView(customView)
                }
            },
            update = { parent ->
                customView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            },
            modifier = Modifier.fillMaxSize()
        )
        // Exit fullscreen button
        FloatingActionButton(
            onClick = onExitFullscreen,
            containerColor = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = "Exit fullscreen",
                tint = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home placeholder when no tab content is loaded
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The empty-state home screen shown when there are zero tabs.
 *
 * Design goals (Phase 13 polish):
 *  - Premium, calm, modern, intentional, branded, lightweight
 *  - Subtle entrance animation (fade-in) so the screen feels alive
 *  - Clear primary action: the search/URL field
 *  - Workspace context shown as a subtle greeting
 *  - Refined typography hierarchy with the Veyla wordmark
 *
 * Deliberately avoids: giant illustrations, excessive cards,
 * gradient-heavy aesthetics, glowing effects, excessive animation.
 */
@Composable
private fun HomePlaceholder(
    activeWorkspaceName: String?,
    onSearch: (String) -> Unit,
    onNavigateToBookmarks: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    // Subtle entrance animation — alpha fade-in.
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 320,
                delayMillis = 40,
                easing = FastOutSlowInEasing
            )
        )
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .alpha(enterAnim.value)
    ) {
        item {
            Spacer(Modifier.height(40.dp))

            // ── Veyla wordmark with refined hierarchy ───────────────
            Text(
                text = "Veyla",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            // ── Workspace context greeting ──────────────────────────
            val greeting = if (!activeWorkspaceName.isNullOrBlank()) {
                "Browsing in $activeWorkspaceName"
            } else {
                "Your web workspace"
            }
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // ── Primary action: search/URL field ────────────────────
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    onSearch(searchQuery)
                                    searchQuery = ""
                                }
                            }
                        )
                    )
                    if (searchQuery.isNotBlank()) {
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Quick-action shortcut ───────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ShortcutChip(
                    label = "Bookmarks",
                    icon = Icons.Default.CheckCircle,
                    onClick = onNavigateToBookmarks,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Subtle tip ──────────────────────────────────────────
            Text(
                text = "Tip: long-press the New Tab button to switch tabs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ShortcutChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hibernated tab placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HibernatedTabPlaceholder(
    tab: Tab,
    onWake: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Tab is sleeping to save memory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = tab.title.ifBlank { UrlNormalizer.getDomainFromUrl(tab.url) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onWake) {
            Text("Wake Tab")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overflow menu dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverflowMenuDialog(
    onDismiss: () -> Unit,
    onTabOverview: () -> Unit,
    onNewTab: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                MenuDialogItem(
                    label = "Tab overview",
                    icon = Icons.Default.ViewAgenda,
                    onClick = onTabOverview
                )
                MenuDialogItem(
                    label = "New tab",
                    icon = Icons.Default.Add,
                    onClick = onNewTab
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MenuDialogItem(
                    label = "Bookmarks",
                    icon = Icons.Default.CheckCircle,
                    onClick = onBookmarks
                )
                MenuDialogItem(
                    label = "Recently closed",
                    icon = Icons.Default.History,
                    onClick = onHistory
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MenuDialogItem(
                    label = "Settings",
                    icon = Icons.Default.Edit,
                    onClick = onSettings
                )
            }
        }
    }
}

@Composable
private fun MenuDialogItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
