package com.pratham.webhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.model.Workspace

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * Modal bottom sheet for per-tab settings and actions.
 *
 * @param tab             The tab to configure.
 * @param onUpdateTab     Called with the updated [Tab] when a setting changes.
 * @param onHibernate     Called with the tab ID to hibernate the tab.
 * @param onIncognitoToggle Called with the tab ID to toggle incognito mode.
 * @param onMoveToWorkspace Called with (tabId, workspaceId) to move the tab.
 * @param onBookmark      Called with (url, title, faviconUrl) to bookmark,
 *                        or (url, "", null) to un-bookmark.
 * @param workspaces      Available workspaces for the move-to dropdown.
 * @param isBookmarked    Whether the tab's URL is currently bookmarked.
 * @param onDismiss       Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSettingsSheet(
    tab: Tab,
    onUpdateTab: (Tab) -> Unit,
    onHibernate: (String) -> Unit,
    onIncognitoToggle: (String) -> Unit,
    onMoveToWorkspace: (String, String) -> Unit,
    onBookmark: (String, String, String?) -> Unit,
    workspaces: List<Workspace>,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local edit state
    var tabName by remember(tab) { mutableStateOf(tab.customName ?: "") }
    var cssOverride by remember(tab) { mutableStateOf(tab.cssOverride ?: "") }
    var userScript by remember(tab) { mutableStateOf(tab.userScript ?: "") }
    var cssExpanded by remember { mutableStateOf(false) }
    var scriptExpanded by remember { mutableStateOf(false) }
    var showMoveDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Tab Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Current URL for context
            Text(
                text = tab.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider()

            // ── Tab name ──────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tab name",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = tabName,
                onValueChange = { newName ->
                    tabName = newName
                    onUpdateTab(tab.copy(customName = newName.ifBlank { null }))
                },
                placeholder = { Text(tab.title.ifBlank { "Untitled" }) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Toggle settings ──────────────────────────────────────────
            SettingsToggleRow(
                icon = Icons.Default.Code,
                label = "JavaScript",
                description = "Enable JavaScript for this tab",
                checked = tab.isJsEnabled,
                onCheckedChange = { onUpdateTab(tab.copy(isJsEnabled = it)) }
            )

            SettingsToggleRow(
                icon = Icons.Default.Shield,
                label = "Block ads",
                description = "Block advertisements on this tab",
                checked = tab.isAdBlockEnabled,
                onCheckedChange = { onUpdateTab(tab.copy(isAdBlockEnabled = it)) }
            )

            SettingsToggleRow(
                icon = Icons.Default.VisibilityOff,
                label = "Incognito",
                description = "Browse without saving history",
                checked = tab.isIncognito,
                onCheckedChange = { onIncognitoToggle(tab.id) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Custom CSS override (collapsible) ────────────────────────
            CollapsibleSection(
                title = "Custom CSS override",
                icon = Icons.Default.Palette,
                expanded = cssExpanded,
                onToggle = { cssExpanded = it }
            ) {
                OutlinedTextField(
                    value = cssOverride,
                    onValueChange = { newCss ->
                        cssOverride = newCss
                        onUpdateTab(tab.copy(cssOverride = newCss.ifBlank { null }))
                    },
                    placeholder = { Text("body { background: #222; }") },
                    minLines = 3,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── User script (collapsible) ────────────────────────────────
            CollapsibleSection(
                title = "User script",
                icon = Icons.Default.Code,
                expanded = scriptExpanded,
                onToggle = { scriptExpanded = it }
            ) {
                OutlinedTextField(
                    value = userScript,
                    onValueChange = { newScript ->
                        userScript = newScript
                        onUpdateTab(tab.copy(userScript = newScript.ifBlank { null }))
                    },
                    placeholder = { Text("// JavaScript injected into every page load") },
                    minLines = 3,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Move to workspace ────────────────────────────────────────
            MoveToWorkspaceDropdown(
                currentWorkspaceId = tab.workspaceId,
                workspaces = workspaces,
                onWorkspaceSelected = { newWorkspaceId ->
                    onMoveToWorkspace(tab.id, newWorkspaceId)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Action buttons ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bookmark / Unbookmark
                OutlinedButton(
                    onClick = {
                        if (isBookmarked) {
                            onBookmark(tab.url, "", null)
                        } else {
                            onBookmark(tab.url, tab.title, tab.faviconUrl)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBookmarked) "Unbookmark" else "Bookmark")
                }

                // Hibernate
                OutlinedButton(
                    onClick = { onHibernate(tab.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hotel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hibernate")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Close tab (destructive)
            Button(
                onClick = {
                    onDismiss()
                    // The caller should handle the actual close via onDismiss + CloseTab event
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Close tab")
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!expanded) }
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 40.dp, top = 8.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveToWorkspaceDropdown(
    currentWorkspaceId: String,
    workspaces: List<Workspace>,
    onWorkspaceSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentWorkspace = workspaces.find { it.id == currentWorkspaceId }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Move to workspace",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentWorkspace?.name ?: "Unknown",
            onValueChange = {},
            readOnly = true,
            label = { Text("Current workspace") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            workspaces.forEach { workspace ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            workspace.name +
                                    if (workspace.id == currentWorkspaceId) " (current)" else ""
                        )
                    },
                    onClick = {
                        if (workspace.id != currentWorkspaceId) {
                            onWorkspaceSelected(workspace.id)
                        }
                        expanded = false
                    },
                    enabled = workspace.id != currentWorkspaceId
                )
            }
        }
    }
}
