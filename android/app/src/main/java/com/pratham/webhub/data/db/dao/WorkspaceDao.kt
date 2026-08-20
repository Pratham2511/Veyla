package com.pratham.webhub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pratham.webhub.data.db.entity.WorkspaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    @Query("SELECT * FROM workspaces ORDER BY position ASC")
    fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    fun getWorkspaceById(id: String): Flow<WorkspaceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workspace: WorkspaceEntity)

    @Update
    suspend fun update(workspace: WorkspaceEntity)

    @Query("DELETE FROM workspaces WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE workspaces SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearAllDefaults()

    @Query("UPDATE workspaces SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultFlag(id: String)

    @Transaction
    suspend fun setDefaultWorkspace(id: String) {
        clearAllDefaults()
        setDefaultFlag(id)
    }

    @Query("SELECT MAX(position) FROM workspaces")
    suspend fun getMaxPosition(): Int?
}