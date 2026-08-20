package com.pratham.webhub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pratham.webhub.data.db.entity.SessionSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionSnapshotDao {

    @Query("SELECT * FROM session_snapshots ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: SessionSnapshotEntity)

    @Query("DELETE FROM session_snapshots WHERE id = :id")
    suspend fun delete(id: String)
}