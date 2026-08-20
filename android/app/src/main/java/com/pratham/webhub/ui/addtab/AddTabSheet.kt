package com.pratham.webhub.ui.addtab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.util.UrlNormalizer

/**
 * Data class describing the result of a tab-creation action so the caller
 * can perform the actual repository insertion.
 */
data class TabCreationParams(
    val url: String,
    val title: String?,
    val customName: String?,
    val workspaceId: String?
)

/**
 * Modal bottom sheet for adding a new tab.
 *
 * @param viewModel       The [AddTabViewModel] driving the UI state.
 * @param onTabCreated    Called with the finalised parameters when the user
 *                        taps "Create" and the input is valid.
 * @param onDismiss       Called when the sheet should be dismissed.
 * @param sheetState      Optional sheet state for controlling dismiss behaviour.
 * @param modifier        Optional [Modifier] applied to the sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTabSheet(
    viewModel: AddTabViewModel,
    onTabCreated: (TabCreationParams) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val urlFocusRequester = remember { FocusRequester() }

    // Auto-focus the URL field when the sheet opens
    LaunchedEffect(Unit) {
        urlFocusRequester.requestFocus()
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
                .verticalScroll(rememberScrollState())
        ) {
            // ── Title ────────────────────────────────────────────────────
            Text(
                text = "Add new tab",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ── URL input ────────────────────────────────────────────────
            OutlinedTextField(
                value = state.urlInput,
                onValueChange = viewModel::setUrlInput,
                label = { Text("URL or search") },
                placeholder = { Text("Enter URL or search query") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(urlFocusRequester)
            )

            // ── URL normalisation preview ────────────────────────────────
            AnimatedVisibility(
                visible = state.urlResult != null && state.urlInput.isNotBlank()
            ) {
                state.urlResult?.let { result ->
                    UrlPreviewRow(result = result)
                }
            }

            // ── Favicon preview ──────────────────────────────────────────
            AnimatedVisibility(
                visible = state.urlResult != null &&
                    state.urlResult!!.url.isNotBlank() &&
                    !state.urlResult!!.url.startsWith("about:")
            ) {
                state.urlResult?.let { result ->
                    FaviconPreview(url = result.url)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Custom name (optional) ───────────────────────────────────
            OutlinedTextField(
                value = state.customName,
                onValueChange = viewModel::setCustomName,
                label = { Text("Custom name (optional)") },
                placeholder = { Text("e.g. \"My Blog\"") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Workspace selector ───────────────────────────────────────
            WorkspaceSelector(
                workspaces = state.workspaces,
                selectedWorkspaceId = state.selectedWorkspaceId,
                onWorkspaceSelected = viewModel::setSelectedWorkspace
            )

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ── Validation error ─────────────────────────────────────────
            if (state.urlInput.isNotBlank() && !state.isValid) {
                Text(
                    text = "Please enter a valid URL or search query",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Create button ────────────────────────────────────────────
            Button(
                onClick = {
                    val result = state.urlResult ?: return@Button
                    onTabCreated(
                        TabCreationParams(
                            url = result.url,
                            title = if (result.isSearch) result.searchTerm else null,
                            customName = state.customName.ifBlank { null },
                            workspaceId = state.selectedWorkspaceId
                        )
                    )
                    viewModel.clear()
                    onDismiss()
                },
                enabled = state.isValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Create tab",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ── URL normalisation preview ───────────────────────────────────────────────

@Composable
private fun UrlPreviewRow(
    result: UrlNormalizer.UrlResult,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = if (result.isSearch) Icons.Default.Search
                          else Icons.Default.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        if (result.isSearch) {
            Text(
                text = "Search: ${result.searchTerm ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = result.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Favicon preview ──────────────────────────────────────────────────────────

@Composable
private fun FaviconPreview(
    url: String,
    modifier: Modifier = Modifier,
) {
    val faviconUrl = remember(url) {
        UrlNormalizer.getFaviconUrl(url, size = 64)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        AsyncImage(
            model = faviconUrl,
            contentDescription = "Favicon preview",
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = UrlNormalizer.getDomainFromUrl(url),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Workspace selector dropdown ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkspaceSelector(
    workspaces: List<Workspace>,
    selectedWorkspaceId: String?,
    onWorkspaceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedWorkspace = workspaces.find { it.id == selectedWorkspaceId }

    if (workspaces.isEmpty()) {
        Text(
            text = "No workspaces available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedWorkspace?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Workspace") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            workspaces.forEach { workspace ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = workspace.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onWorkspaceSelected(workspace.id)
                        expanded = false
                    },
                    leadingIcon = {
                        if (workspace.id == selectedWorkspaceId) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}


