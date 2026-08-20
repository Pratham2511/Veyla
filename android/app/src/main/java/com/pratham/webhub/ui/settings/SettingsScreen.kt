package com.pratham.webhub.ui.settings

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pratham.webhub.BuildConfig
import com.pratham.webhub.R
import com.pratham.webhub.util.SearchEngineHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Theme options ─────────────────────────────────────────────────────────────

private enum class ThemeOption(val value: String, val label: String, val icon: ImageVector) {
    System("system", "System", Icons.Default.Palette),
    Light("light", "Light", Icons.Default.LightMode),
    Dark("dark", "Dark", Icons.Default.DarkMode),
}

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * Full-screen settings for WebHub.
 *
 * @param onBack Called when the user presses the back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Dialog state
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteSessionDialog by remember { mutableStateOf<String?>(null) }
    var showSaveSessionDialog by remember { mutableStateOf(false) }
    var sessionNameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ═══════════════════════════════════════════════════════════════════
            // Section 1: Appearance
            // ═══════════════════════════════════════════════════════════════════
            item {
                SectionHeader("Appearance")
            }

            item {
                // Theme selector
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val currentTheme = ThemeOption.entries.find {
                        it.value == state.settings.globalThemeMode
                    } ?: ThemeOption.System

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeOption.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index, ThemeOption.entries.size),
                                onClick = { viewModel.setThemeMode(option.value) },
                                selected = currentTheme == option,
                                icon = {
                                    if (currentTheme == option) {
                                        // Show a small indicator or leave empty for clean M3 look
                                    }
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(option.label)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Material You info
                ListItem(
                    headlineContent = { Text("Material You") },
                    supportingContent = { Text("Follows your system dynamic color theme") },
                    leadingContent = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = { /* Material You is always on */ },
                            enabled = false
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ═══════════════════════════════════════════════════════════════════
            // Section 2: Browser
            // ═══════════════════════════════════════════════════════════════════
            item {
                SectionHeader("Browser")
            }

            item {
                // Search engine dropdown
                SearchEngineDropdown(
                    engines = state.searchEngines,
                    currentEngineName = state.currentSearchEngineName,
                    onEngineSelected = { url -> viewModel.setSearchEngine(url) }
                )
            }

            item {
                // Ad block toggle
                ListItem(
                    headlineContent = { Text("Block ads") },
                    supportingContent = { Text("Block advertisements and trackers") },
                    leadingContent = {
                        Icon(Icons.Default.Block, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = state.settings.adBlockEnabled,
                            onCheckedChange = { viewModel.setAdBlockEnabled(it) }
                        )
                    }
                )
            }

            item {
                // JavaScript toggle (per-tab default)
                ListItem(
                    headlineContent = { Text("JavaScript") },
                    supportingContent = { Text("Enable JavaScript by default for new tabs") },
                    leadingContent = {
                        Icon(Icons.Default.Code, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = true, // default is on
                            onCheckedChange = { /* TODO: Wire to per-tab JS default setting */ }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                // Clear browsing data
                ListItem(
                    headlineContent = { Text("Clear browsing data") },
                    supportingContent = {
                        Text("Remove saved sessions and reset ad-block counters")
                    },
                    leadingContent = {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showClearDataDialog = true }
                )
            }

            if (state.isClearingData) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════════
            // Section 3: Privacy
            // ═══════════════════════════════════════════════════════════════════
            item {
                SectionHeader("Privacy")
            }

            item {
                ListItem(
                    headlineContent = { Text("Biometric lock") },
                    supportingContent = {
                        if (state.biometricAvailable) {
                            Text("Require fingerprint to open WebHub")
                        } else {
                            Text("No biometric hardware available")
                        }
                    },
                    leadingContent = {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = state.settings.isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            enabled = state.biometricAvailable
                        )
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("HTTPS security") },
                    supportingContent = {
                        Text(
                            "WebHub enforces HTTPS connections where possible. " +
                                    "Sites with invalid certificates are flagged with a warning."
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Security, contentDescription = null)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // ═══════════════════════════════════════════════════════════════════
            // Section 4: Sessions
            // ═══════════════════════════════════════════════════════════════════
            item {
                SectionHeader("Sessions")
            }

            item {
                // Save current session
                ListItem(
                    headlineContent = { Text("Save current session") },
                    supportingContent = { Text("Save all open tabs as a restorable session") },
                    leadingContent = {
                        Icon(Icons.Default.NoteAdd, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        sessionNameInput = ""
                        showSaveSessionDialog = true
                    }
                )
            }

            item {
                // Auto-restore toggle (informational for now)
                ListItem(
                    headlineContent = { Text("Auto-restore last session") },
                    supportingContent = { Text("Automatically restore tabs when WebHub opens") },
                    leadingContent = {
                        Icon(Icons.Default.Restore, contentDescription = null)
                    },
                    trailingContent = {
                        Switch(
                            checked = false,
                            onCheckedChange = { /* TODO: Wire to auto-restore setting */ }
                        )
                    }
                )
            }

            if (state.sessions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved sessions",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Save your current session to restore it later",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(state.sessions, key = { it.id }) { session ->
                    SessionListItem(
                        session = session,
                        onRestore = { viewModel.restoreSession(session.id) },
                        onDelete = { showDeleteSessionDialog = session.id }
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════════
            // Section 5: About
            // ═══════════════════════════════════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("About")
            }

            item {
                ListItem(
                    headlineContent = { Text("Version") },
                    supportingContent = {
                        Text(
                            BuildConfig.VERSION_NAME
                                    + " (${BuildConfig.VERSION_CODE})"
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                )
            }

            item {
                val context = LocalContext.current
                ListItem(
                    headlineContent = { Text("GitHub") },
                    supportingContent = {
                        Text(
                            "github.com/pratham/webhub",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Code, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/pratham/webhub")
                        )
                        context.startActivity(intent)
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("License") },
                    supportingContent = { Text("Open-source under the GNU GPLv3 license") },
                    leadingContent = {
                        Icon(Icons.Default.Stars, contentDescription = null)
                    }
                )
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────

    // Clear browsing data confirmation
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear browsing data?") },
            text = {
                Text(
                    "This will delete all saved sessions and reset ad-block " +
                            "counters. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        viewModel.clearBrowsingData()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete session confirmation
    if (showDeleteSessionDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSessionDialog = null },
            title = { Text("Delete session?") },
            text = { Text("This saved session will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteSessionDialog?.let { viewModel.deleteSession(it) }
                        showDeleteSessionDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSessionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save session dialog
    if (showSaveSessionDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSessionDialog = false },
            title = { Text("Save session") },
            text = {
                OutlinedTextField(
                    value = sessionNameInput,
                    onValueChange = { sessionNameInput = it },
                    label = { Text("Session name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = sessionNameInput.trim()
                        if (name.isNotBlank()) {
                            viewModel.saveSession(name)
                            showSaveSessionDialog = false
                        }
                    },
                    enabled = sessionNameInput.trim().isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSessionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchEngineDropdown(
    engines: List<Pair<String, String>>,
    currentEngineName: String,
    onEngineSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = "Default search engine",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = currentEngineName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Search engine") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                engines.forEach { (name, url) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onEngineSelected(url)
                            expanded = false
                        },
                        trailingIcon = {
                            if (name == currentEngineName) {
                                Text(
                                    "\u2713", // checkmark
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionListItem(
    session: com.pratham.webhub.domain.model.SessionSnapshot,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormatted = remember(session.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy \u2022 hh:mm a", Locale.getDefault())
        sdf.format(Date(session.createdAt))
    }

    ListItem(
        headlineContent = {
            Text(
                session.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = { Text(dateFormatted) },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore session",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete session",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
