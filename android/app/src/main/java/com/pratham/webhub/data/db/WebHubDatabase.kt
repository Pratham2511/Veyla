package com.pratham.webhub.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pratham.webhub.data.db.converter.Converters
import com.pratham.webhub.data.db.dao.BookmarkDao
import com.pratham.webhub.data.db.dao.ClosedTabHistoryDao
import com.pratham.webhub.data.db.dao.SessionSnapshotDao
import com.pratham.webhub.data.db.dao.TabDao
import com.pratham.webhub.data.db.dao.WorkspaceDao
import com.pratham.webhub.data.db.entity.BookmarkEntity
import com.pratham.webhub.data.db.entity.ClosedTabHistoryEntity
import com.pratham.webhub.data.db.entity.SessionSnapshotEntity
import com.pratham.webhub.data.db.entity.TabEntity
import com.pratham.webhub.data.db.entity.WorkspaceEntity

@Database(
    entities = [
        TabEntity::class,
        WorkspaceEntity::class,
        BookmarkEntity::class,
        ClosedTabHistoryEntity::class,
        SessionSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WebHubDatabase : RoomDatabase() {

    abstract fun tabDao(): TabDao

    abstract fun workspaceDao(): WorkspaceDao

    abstract fun bookmarkDao(): BookmarkDao

    abstract fun closedTabHistoryDao(): ClosedTabHistoryDao

    abstract fun sessionSnapshotDao(): SessionSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: WebHubDatabase? = null

        fun getDatabase(context: Context): WebHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WebHubDatabase::class.java,
                    "webhub_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
