package com.pratham.webhub.ui.widget

/**
 * Holds the data that the [WebHubWidget] displays.
 *
 * @param workspaceName The name of the current workspace shown in the widget.
 * @param tabCount      Number of open tabs in the workspace.
 * @param activeTabTitle Title of the currently active tab, or null if no tabs are open.
 */
data class WidgetState(
    val workspaceName: String = "WebHub",
    val tabCount: Int = 0,
    val activeTabTitle: String? = null,
)