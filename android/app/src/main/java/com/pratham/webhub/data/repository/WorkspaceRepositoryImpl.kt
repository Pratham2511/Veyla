package com.pratham.webhub.data.repository

import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import com.pratham.webhub.data.db.entity.WorkspaceEntity
import com.pratham.webhub.domain.model.Workspace
import com.pratham.webhub.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepositoryImpl @Inject constructor(
    private val workspaceDao: WorkspaceDao,
    private val tabDao: TabDao
) : WorkspaceRepository {

    override fun getWorkspaces(): Flow<List<Workspace>> {
        return workspaceDao.getAllWorkspaces().map { entities ->
            entities.map { Workspace.fromEntity(it) }
        }
    }

    override fun getWorkspace(workspaceId: String): Flow<Workspace?> {
        return workspaceDao.getWorkspaceById(workspaceId).map { entity ->
            entity?.let { Workspace.fromEntity(it) }
        }
    }

    override suspend fun createWorkspace(name: String): String {
        val now = System.currentTimeMillis()
        val maxPosition = workspaceDao.getMaxPosition() ?: -1

        val workspace = WorkspaceEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            position = maxPosition + 1,
            createdAt = now,
            updatedAt = now
        )
        workspaceDao.insert(workspace)
        return workspace.id
    }

    override suspend fun deleteWorkspace(workspaceId: String) {
        tabDao.deleteByWorkspace(workspaceId)
        workspaceDao.delete(workspaceId)
    }

    override suspend fun renameWorkspace(workspaceId: String, newName: String) {
        val entity = workspaceDao.getWorkspaceById(workspaceId).first() ?: return
        workspaceDao.update(entity.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun switchWorkspace(workspaceId: String) {
        val entity = workspaceDao.getWorkspaceById(workspaceId).first() ?: return
        workspaceDao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun setDefaultWorkspace(workspaceId: String) {
        workspaceDao.setDefaultWorkspace(workspaceId)
    }

    override suspend fun getWorkspaceCount(): Int {
        return workspaceDao.getAllWorkspaces().first().size
    }

    override suspend fun getActiveWorkspaceId(): String? {
        val workspaces = workspaceDao.getAllWorkspaces().first()
        val default = workspaces.firstOrNull { it.isDefault }
        return default?.id ?: workspaces.firstOrNull()?.id
    }
}
