package com.pratham.webhub.data.repository

import com.pratham.webhub.data.db.dao.SessionSnapshotDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import com.pratham.webhub.data.db.entity.SessionSnapshotEntity
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.domain.model.SessionSnapshot
import com.pratham.webhub.domain.model.Tab
import com.pratham.webhub.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionSnapshotDao: SessionSnapshotDao,
    private val tabDao: TabDao,
    private val workspaceDao: WorkspaceDao
) : SessionRepository {

    override fun getSessionSnapshots(): Flow<List<SessionSnapshot>> {
        return sessionSnapshotDao.getAllSessions().map { entities ->
            entities.map { SessionSnapshot.fromEntity(it) }
        }
    }

    override suspend fun saveSession(name: String) {
        val allTabs = tabDao.getAllTabs().first()
        val workspaces = workspaceDao.getAllWorkspaces().first()

        val sessionJson = JSONObject()
        sessionJson.put("version", 1)
        sessionJson.put("savedAt", System.currentTimeMillis())

        // Serialize workspaces
        val workspacesArray = JSONArray()
        for (ws in workspaces) {
            val wsObj = JSONObject()
            wsObj.put("id", ws.id)
            wsObj.put("name", ws.name)
            wsObj.put("themeMode", ws.themeMode)
            wsObj.put("accentColor", ws.accentColor ?: JSONObject.NULL)
            wsObj.put("position", ws.position)
            wsObj.put("isDefault", ws.isDefault)
            workspacesArray.put(wsObj)
        }
        sessionJson.put("workspaces", workspacesArray)

        // Serialize tabs
        val tabsArray = JSONArray()
        for (tab in allTabs) {
            val tabObj = JSONObject()
            tabObj.put("id", tab.id)
            tabObj.put("workspaceId", tab.workspaceId)
            tabObj.put("url", tab.url)
            tabObj.put("title", tab.title)
            tabObj.put("faviconUrl", tab.faviconUrl ?: JSONObject.NULL)
            tabObj.put("customName", tab.customName ?: JSONObject.NULL)
            tabObj.put("isJsEnabled", tab.isJsEnabled)
            tabObj.put("isAdBlockEnabled", tab.isAdBlockEnabled)
            tabObj.put("cssOverride", tab.cssOverride ?: JSONObject.NULL)
            tabObj.put("userScript", tab.userScript ?: JSONObject.NULL)
            tabObj.put("position", tab.position)
            tabObj.put("isHibernated", tab.isHibernated)
            tabObj.put("savedScrollY", tab.savedScrollY)
            tabsArray.put(tabObj)
        }
        sessionJson.put("tabs", tabsArray)

        val snapshot = SessionSnapshotEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            data = sessionJson.toString(),
            createdAt = System.currentTimeMillis()
        )
        sessionSnapshotDao.insert(snapshot)
    }

    override suspend fun restoreSession(snapshotId: String) {
        val snapshots = sessionSnapshotDao.getAllSessions().first()
        val snapshot = snapshots.firstOrNull { it.id == snapshotId }
            ?: throw IllegalArgumentException("Session snapshot not found: $snapshotId")

        val json = JSONObject(snapshot.data)
        val tabsArray = json.getJSONArray("tabs")

        // Clear existing tabs and workspaces
        val existingWorkspaces = workspaceDao.getAllWorkspaces().first()
        for (ws in existingWorkspaces) {
            tabDao.deleteByWorkspace(ws.id)
        }

        // Restore workspaces (skip if workspace with same ID already handled by clear above)
        val workspacesArray = json.getJSONArray("workspaces")
        for (i in 0 until workspacesArray.length()) {
            val wsObj = workspacesArray.getJSONObject(i)
            com.pratham.webhub.data.db.entity.WorkspaceEntity(
                id = wsObj.getString("id"),
                name = wsObj.getString("name"),
                themeMode = wsObj.optString("themeMode", "system"),
                accentColor = wsObj.opt("accentColor")?.takeIf { it != JSONObject.NULL }?.toString(),
                position = wsObj.getInt("position"),
                isDefault = wsObj.optBoolean("isDefault", false),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ).let { workspaceDao.insert(it) }
        }

        // Restore tabs
        val now = System.currentTimeMillis()
        for (i in 0 until tabsArray.length()) {
            val tabObj = tabsArray.getJSONObject(i)
            TabEntity(
                id = tabObj.getString("id"),
                workspaceId = tabObj.getString("workspaceId"),
                url = tabObj.getString("url"),
                title = tabObj.optString("title", ""),
                faviconUrl = tabObj.opt("faviconUrl")?.takeIf { it != JSONObject.NULL }?.toString(),
                customName = tabObj.opt("customName")?.takeIf { it != JSONObject.NULL }?.toString(),
                isJsEnabled = tabObj.optBoolean("isJsEnabled", true),
                isAdBlockEnabled = tabObj.optBoolean("isAdBlockEnabled", true),
                cssOverride = tabObj.opt("cssOverride")?.takeIf { it != JSONObject.NULL }?.toString(),
                userScript = tabObj.opt("userScript")?.takeIf { it != JSONObject.NULL }?.toString(),
                position = tabObj.getInt("position"),
                isHibernated = tabObj.optBoolean("isHibernated", false),
                savedScrollY = tabObj.optInt("savedScrollY", 0),
                createdAt = now,
                updatedAt = now
            ).let { tabDao.insert(it) }
        }
    }

    override fun getLastSession(): Flow<SessionSnapshot?> {
        return sessionSnapshotDao.getAllSessions().map { list ->
            list.firstOrNull()?.let { SessionSnapshot.fromEntity(it) }
        }
    }

    override suspend fun deleteSession(snapshotId: String) {
        sessionSnapshotDao.delete(snapshotId)
    }
}
