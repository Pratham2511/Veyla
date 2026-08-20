package com.pratham.webhub.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// ── Constants ─────────────────────────────────────────────────────────────────

private val TransitionDuration = 350

// ── Theme card data ───────────────────────────────────────────────────────────

private data class ThemeCardInfo(
    val theme: OnboardingTheme,
    val label: String,
    val icon: ImageVector,
    val description: String
)

private val themeCards = listOf(
    ThemeCardInfo(
        theme = OnboardingTheme.System,
        label = "System",
        icon = Icons.Default.PhoneAndroid,
        description = "Follow your device settings"
    ),
    ThemeCardInfo(
        theme = OnboardingTheme.Light,
        label = "Light",
        icon = Icons.Default.LightMode,
        description = "Always use light mode"
    ),
    ThemeCardInfo(
        theme = OnboardingTheme.Dark,
        label = "Dark",
        icon = Icons.Default.DarkMode,
        description = "Always use dark mode"
    )
)

// ── Search engine card data ───────────────────────────────────────────────────

private data class EngineCardInfo(
    val name: String,
    val icon: ImageVector,
    val url: String,
    val description: String
)

private val engineCards = listOf(
    EngineCardInfo("Google", Icons.Default.Search, "https://www.google.com/search?q=", "The world's most popular search engine"),
    EngineCardInfo("DuckDuckGo", Icons.Default.Search, "https://duckduckgo.com/?q=", "Privacy-focused search"),
    EngineCardInfo("Bing", Icons.Default.Search, "https://www.bing.com/search?q=", "Microsoft's search engine"),
    EngineCardInfo("Brave", Icons.Default.Search, "https://search.brave.com/search?q=", "Independent, private search")
)

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * Three-step onboarding flow for first-time users.
 *
 * Step 0: Welcome + theme selection
 * Step 1: Search engine selection
 * Step 2: Default workspace name
 *
 * @param onComplete Called when onboarding completes successfully.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Track whether the user has kicked off completion so we can
    // navigate away once the ViewModel finishes persisting.
    var hasTriggeredComplete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isCompleting) {
        if (hasTriggeredComplete && !state.isCompleting) {
            onComplete()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top progress indicator ─────────────────────────────────────
            val progress by animateFloatAsState(
                targetValue = (state.currentStep + 1) / 3f,
                animationSpec = tween(durationMillis = 400),
                label = "onboarding_progress"
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            // ── Step content (animated crossfade / slide) ──────────────────
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { width -> direction * width },
                        animationSpec = tween(TransitionDuration)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { width -> -direction * width },
                        animationSpec = tween(TransitionDuration)
                    )
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep(
                        selectedTheme = state.selectedTheme,
                        onThemeSelected = viewModel::selectTheme,
                        onNext = {
                            viewModel.nextStep()
                        }
                    )
                    1 -> SearchEngineStep(
                        selectedUrl = state.selectedSearchEngineUrl,
                        onEngineSelected = viewModel::selectSearchEngine,
                        onBack = viewModel::previousStep,
                        onNext = {
                            viewModel.nextStep()
                        }
                    )
                    2 -> WorkspaceStep(
                        workspaceName = state.defaultWorkspaceName,
                        onWorkspaceNameChanged = viewModel::setDefaultWorkspaceName,
                        onBack = viewModel::previousStep,
                        onFinish = {
                            hasTriggeredComplete = true
                            viewModel.complete()
                        },
                        isCompleting = state.isCompleting
                    )
                }
            }
        }
    }
}

// ── Step 0: Welcome ───────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(
    selectedTheme: OnboardingTheme,
    onThemeSelected: (OnboardingTheme) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "WebHub logo",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to WebHub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your private, workspace-based browser.\nLet's set things up.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Theme selection cards
        Text(
            text = "Choose your appearance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            themeCards.forEach { card ->
                ThemeSelectionCard(
                    card = card,
                    isSelected = selectedTheme == card.theme,
                    onClick = { onThemeSelected(card.theme) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    card: ThemeCardInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ── Step 1: Search engine ─────────────────────────────────────────────────────

@Composable
private fun SearchEngineStep(
    selectedUrl: String,
    onEngineSelected: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Choose your search engine",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You can always change this later in Settings.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Search engine cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            engineCards.forEach { card ->
                SearchEngineCard(
                    card = card,
                    isSelected = selectedUrl == card.url,
                    onClick = { onEngineSelected(card.url) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun SearchEngineCard(
    card: EngineCardInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Step 2: Default workspace ─────────────────────────────────────────────────

@Composable
private fun WorkspaceStep(
    workspaceName: String,
    onWorkspaceNameChanged: (String) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    isCompleting: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Name your workspace",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Workspaces help you organize tabs by context.\nYou can create more later.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = workspaceName,
            onValueChange = onWorkspaceNameChanged,
            label = { Text("Workspace name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isCompleting,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back")
            }
            Button(
                onClick = onFinish,
                enabled = workspaceName.isNotBlank() && !isCompleting,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCompleting) "Setting up..." else "Finish")
            }
        }
    }
}
