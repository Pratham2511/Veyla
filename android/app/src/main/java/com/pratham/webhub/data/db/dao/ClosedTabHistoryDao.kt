package com.pratham.webhub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClosedTabHistoryDao {

    @Query("SELECT * FROM closed_tab_history ORDER BY closedAt DESC LIMIT 20")
    fun getRecentClosedTabs(): Flow<List<ClosedTabHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClosedTabHistoryEntity)

    @Query("DELETE FROM closed_tab_history WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM closed_tab_history")
    suspend fun deleteAll()

    @Query("""
        DELETE FROM closed_tab_history
        WHERE id NOT IN (
            SELECT id FROM closed_tab_history ORDER BY closedAt DESC LIMIT 20
        )
    """)
    suspend fun pruneOldEntries()
}