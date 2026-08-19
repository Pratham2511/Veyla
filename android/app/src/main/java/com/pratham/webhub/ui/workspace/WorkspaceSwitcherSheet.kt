package com.pratham.webhub.ui.workspace

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Briefcase
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pratham.webhub.domain.model.Workspace

/**
 * Modal bottom sheet for managing workspaces – switch, rename, delete, set default,
 * and create new workspaces.
 *
 * @param viewModel     The [WorkspaceViewModel] driving the UI state and events.
 * @param onDismiss     Called when the sheet should be dismissed.
 * @param sheetState    Optional sheet state for controlling dismiss behaviour.
 * @param modifier      Optional [Modifier] applied to the sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSwitcherSheet(
    viewModel: WorkspaceViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ── Delete confirmation dialog ───────────────────────────────────────
    state.showDeleteConfirmation?.let { workspaceId ->
        val workspace = state.workspaces.find { it.id == workspaceId }
        DeleteConfirmationDialog(
            workspaceName = workspace?.name ?: "this workspace",
            onConfirm = { viewModel.deleteWorkspace(workspaceId) },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

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
            // ── Title ────────────────────────────────────────────────────
            Text(
                text = "Workspaces",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ── Create new workspace ─────────────────────────────────────
            CreateWorkspaceRow(
                name = state.newWorkspaceName,
                isCreating = state.isCreating,
                onNameChange = viewModel::setNewWorkspaceName,
                onCreate = viewModel::createWorkspace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Workspace list ───────────────────────────────────────────
            if (state.workspaces.isEmpty()) {
                EmptyWorkspaceState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = state.workspaces,
                        key = { it.id }
                    ) { workspace ->
                        val tabCount = state.tabCounts[workspace.id] ?: 0
                        val isEditing = state.editingWorkspaceId == workspace.id
                        val isActive = state.activeWorkspaceId == workspace.id

                        WorkspaceListItem(
                            workspace = workspace,
                            tabCount = tabCount,
                            isActive = isActive,
                            isEditing = isEditing,
                            editingName = if (isEditing) state.editingWorkspaceName else "",
                            onSwitch = { viewModel.switchWorkspace(workspace.id) },
                            onStartRename = {
                                viewModel.startEditing(workspace.id, workspace.name)
                            },
                            onRenameChange = viewModel::setEditingWorkspaceName,
                            onRenameConfirm = {
                                viewModel.renameWorkspace(
                                    workspace.id,
                                    state.editingWorkspaceName
                                )
                            },
                            onRenameCancel = viewModel::cancelEditing,
                            onSetDefault = { viewModel.setDefaultWorkspace(workspace.id) },
                            onDelete = { viewModel.showDeleteConfirmation(workspace.id) }
                        )
                    }
                }
            }
        }
    }
}

// ── Create workspace row ───────────────────────────────────────────────────

@Composable
private fun CreateWorkspaceRow(
    name: String,
    isCreating: Boolean,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "New workspace name",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            maxLines = 1,
            enabled = !isCreating,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (name.isNotBlank()) onCreate() }
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onCreate,
            enabled = name.isNotBlank() && !isCreating,
            modifier = Modifier.size(48.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create workspace",
                    tint = if (name.isNotBlank()) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
    }
}

// ── Workspace list item ────────────────────────────────────────────────────

@Composable
private fun WorkspaceListItem(
    workspace: Workspace,
    tabCount: Int,
    isActive: Boolean,
    isEditing: Boolean,
    editingName: String,
    onSwitch: () -> Unit,
    onStartRename: () -> Unit,
    onRenameChange: (String) -> Unit,
    onRenameConfirm: () -> Unit,
    onRenameCancel: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primaryContainer
                     else MaterialTheme.colorScheme.surface,
        label = "ws_bg"
    )
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurface

    ListItem(
        headlineContent = {
            if (isEditing) {
                val renameFocus = remember { FocusRequester() }
                LaunchedEffect(Unit) { renameFocus.requestFocus() }
                OutlinedTextField(
                    value = editingName,
                    onValueChange = onRenameChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(renameFocus),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = onRenameConfirm)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = workspace.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (workspace.isDefault) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Default workspace",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        supportingContent = if (!isEditing) {
            {
                Text(
                    text = "$tabCount ${if (tabCount == 1) "tab" else "tabs"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        leadingContent = {
            BadgedBox(
                badge = {
                    if (tabCount > 0) {
                        Badge {
                            Text(
                                text = tabCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                WorkspaceIcon(isActive = isActive)
            }
        },
        trailingContent = if (!isEditing) {
            {
                Row {
                    SmallIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Rename",
                        onClick = onStartRename
                    )
                    SmallIconButton(
                        icon = Icons.Default.Star,
                        contentDescription =
                            if (workspace.isDefault) "Already default" else "Set as default",
                        onClick = onSetDefault,
                        tint = if (workspace.isDefault) MaterialTheme.colorScheme.tertiary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SmallIconButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            {
                Row {
                    SmallIconButton(
                        icon = Icons.Default.Done,
                        contentDescription = "Confirm rename",
                        onClick = onRenameConfirm,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    SmallIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Cancel",
                        onClick = onRenameCancel
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = containerColor,
            headlineColor = contentColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { if (!isEditing) onSwitch() }
            )
    )
}

// ── Workspace leading icon ─────────────────────────────────────────────────

@Composable
private fun WorkspaceIcon(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant
    val fgColor = if (isActive) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Briefcase,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Delete confirmation dialog ─────────────────────────────────────────────

@Composable
private fun DeleteConfirmationDialog(
    workspaceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete workspace") },
        text = {
            Text(
                "Are you sure you want to delete \"$workspaceName\"? " +
                    "All tabs in this workspace will be lost."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyWorkspaceState() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Briefcase,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No workspaces yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Create your first workspace above",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ── Small icon button (48dp touch target) ───────────────────────────────────

@Composable
private fun SmallIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
