package com.pratham.webhub.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pratham.webhub.data.db.entity.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {

    @Query("SELECT * FROM tabs WHERE workspaceId = :workspaceId AND isHibernated = 0 ORDER BY position ASC")
    fun getTabsByWorkspace(workspaceId: String): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE id = :id")
    fun getTabById(id: String): Flow<TabEntity?>

    @Query("SELECT * FROM tabs")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: TabEntity)

    @Update
    suspend fun update(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM tabs WHERE workspaceId = :workspaceId")
    suspend fun deleteByWorkspace(workspaceId: String)

    @Query("UPDATE tabs SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: String, position: Int)

    @Query("UPDATE tabs SET isHibernated = :isHibernated WHERE id = :id")
    suspend fun updateHibernation(id: String, isHibernated: Boolean)

    @Query("UPDATE tabs SET url = :url, title = :title WHERE id = :id")
    suspend fun updateUrlAndTitle(id: String, url: String, title: String)

    @Query("UPDATE tabs SET savedScrollY = :scrollY WHERE id = :id")
    suspend fun updateScrollY(id: String, scrollY: Int)
}