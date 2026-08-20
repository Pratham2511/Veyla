package com.pratham.webhub.di

import com.pratham.webhub.data.repository.BookmarkRepositoryImpl
import com.pratham.webhub.data.repository.ClosedTabRepositoryImpl
import com.pratham.webhub.data.repository.SessionRepositoryImpl
import com.pratham.webhub.data.repository.SettingsRepositoryImpl
import com.pratham.webhub.data.repository.TabRepositoryImpl
import com.pratham.webhub.data.repository.WorkspaceRepositoryImpl
import com.pratham.webhub.domain.repository.BookmarkRepository
import com.pratham.webhub.domain.repository.ClosedTabRepository
import com.pratham.webhub.domain.repository.SessionRepository
import com.pratham.webhub.domain.repository.SettingsRepository
import com.pratham.webhub.domain.repository.TabRepository
import com.pratham.webhub.domain.repository.WorkspaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTabRepository(impl: TabRepositoryImpl): TabRepository

    @Binds
    @Singleton
    abstract fun bindWorkspaceRepository(impl: WorkspaceRepositoryImpl): WorkspaceRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindClosedTabRepository(impl: ClosedTabRepositoryImpl): ClosedTabRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
