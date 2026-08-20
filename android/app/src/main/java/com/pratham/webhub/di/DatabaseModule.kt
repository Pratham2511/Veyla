package com.pratham.webhub.di

import android.content.Context
import com.pratham.webhub.data.db.WebHubDatabase
import com.pratham.webhub.data.db.dao.BookmarkDao
import com.pratham.webhub.data.db.dao.ClosedTabHistoryDao
import com.pratham.webhub.data.db.dao.SessionSnapshotDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WebHubDatabase =
        WebHubDatabase.getDatabase(context)

    @Provides
    fun provideTabDao(db: WebHubDatabase): TabDao = db.tabDao()

    @Provides
    fun provideWorkspaceDao(db: WebHubDatabase): WorkspaceDao = db.workspaceDao()

    @Provides
    fun provideBookmarkDao(db: WebHubDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideClosedTabHistoryDao(db: WebHubDatabase): ClosedTabHistoryDao =
        db.closedTabHistoryDao()

    @Provides
    fun provideSessionSnapshotDao(db: WebHubDatabase): SessionSnapshotDao =
        db.sessionSnapshotDao()
}
