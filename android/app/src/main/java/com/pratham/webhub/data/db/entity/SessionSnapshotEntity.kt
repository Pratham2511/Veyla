package com.pratham.webhub.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_snapshots",
    indices = [
        Index("createdAt")
    ]
)
data class SessionSnapshotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val data: String,
    val createdAt: Long
)
