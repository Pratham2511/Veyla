package com.pratham.webhub.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Padding
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.ColorScheme
import androidx.glance.material3.MaterialTheme
import androidx.glance.material3.Text
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import com.pratham.webhub.MainActivity
import com.pratham.webhub.ui.theme.md_theme_light_primary

/**
 * Glance AppWidget receiver that hosts [WebHubWidget] on the home screen.
 */
class WebHubWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WebHubWidget()

    /**
     * Called when the widget is added or updated.
     * Use this hook to refresh [WidgetState] from the data layer if needed.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onRefresh(
        context: Context,
        glanceId: GlanceId,
    ) {
        // Trigger a state update / re-render of the widget.
        super.onRefresh(context, glanceId)
    }
}

/**
 * The actual Glance composable widget definition for WebHub.
 *
 * Displays the workspace name, active tab count, and a quick-launch
 * button that opens the app.
 */
class WebHubWidget : GlanceAppWidget() {

    @Composable
    override fun Content() {
        val context = LocalContext.current

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color(0xFF1C1B1F)))
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(Padding(16.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // App name
                    Text(
                        text = "WebHub",
                        style = androidx.glance.text.TextStyle(
                            color = ColorProvider(day = md_theme_light_primary, night = com.pratham.webhub.ui.theme.md_theme_dark_primary),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        ),
                    )

                    // Tab count subtitle
                    Text(
                        text = "Tap to open browser",
                        style = androidx.glance.text.TextStyle(
                            color = ColorProvider(
                                day = androidx.compose.ui.graphics.Color(0xFF45464F),
                                night = androidx.compose.ui.graphics.Color(0xFFC6C5D0),
                            ),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Normal,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }

    /**
     * Minimal Glance Material 3 theme wrapper.
     * Replaces the deprecated `GlanceTheme` with direct MaterialTheme usage.
     */
    @Composable
    private fun GlanceTheme(content: @Composable () -> Unit) {
        // Glance doesn't have full dynamic theming yet, so we apply
        // MaterialTheme with our color scheme and pass through content.
        MaterialTheme(content = content)
    }
}
