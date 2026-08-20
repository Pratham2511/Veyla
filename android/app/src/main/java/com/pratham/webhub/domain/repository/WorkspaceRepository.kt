package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {

    fun getWorkspaces(): Flow<List<Workspace>>

    fun getWorkspace(workspaceId: String): Flow<Workspace?>

    suspend fun createWorkspace(name: String): String

    suspend fun deleteWorkspace(workspaceId: String)

    suspend fun renameWorkspace(workspaceId: String, newName: String)

    suspend fun switchWorkspace(workspaceId: String)

    suspend fun setDefaultWorkspace(workspaceId: String)

    suspend fun getWorkspaceCount(): Int

    suspend fun getActiveWorkspaceId(): String?
}