package com.pratham.webhub.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pratham.webhub.domain.model.AppSettings
import com.pratham.webhub.domain.model.Bookmark
import com.pratham.webhub.domain.repository.BookmarkRepository
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.repository.TabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class BookmarksUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val searchQuery: String = "",
    val settings: AppSettings = AppSettings()
) {
    /** Bookmarks filtered by the search query. */
    val filteredBookmarks: List<Bookmark>
        get() {
            if (searchQuery.isBlank()) return bookmarks
            val q = searchQuery.lowercase()
            return bookmarks.filter { bookmark ->
                bookmark.title.lowercase().contains(q) ||
                        bookmark.url.lowercase().contains(q)
            }
        }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val tabRepository: TabRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val state: StateFlow<BookmarksUiState> = combine(
        bookmarkRepository.getBookmarks(),
        settingsRepository.getSettings(),
        _searchQuery
    ) { bookmarks, settings, query ->
        BookmarksUiState(
            bookmarks = bookmarks,
            searchQuery = query,
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BookmarksUiState()
    )

    // ── Public API ────────────────────────────────────────────────────────

    /** Update the search/filter query. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Delete a bookmark by its [bookmarkId]. */
    fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            bookmarkRepository.removeBookmark(bookmarkId)
        }
    }

    /** Open a bookmark URL in a new tab within the given workspace. */
    fun openInNewTab(
        url: String,
        title: String,
        faviconUrl: String? = null,
        workspaceId: String
    ) {
        viewModelScope.launch {
            tabRepository.addTab(
                workspaceId = workspaceId,
                url = url,
                title = title,
                faviconUrl = faviconUrl
            )
        }
    }
}