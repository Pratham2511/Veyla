package com.pratham.webhub.domain.model

import com.pratham.webhub.data.db.entity.SessionSnapshotEntity

data class SessionSnapshot(
    val id: String,
    val name: String,
    val data: String,
    val createdAt: Long
) {
    companion object {
        fun fromEntity(entity: SessionSnapshotEntity): SessionSnapshot = SessionSnapshot(
            id = entity.id,
            name = entity.name,
            data = entity.data,
            createdAt = entity.createdAt
        )
    }
}

fun SessionSnapshot.toEntity(): SessionSnapshotEntity = SessionSnapshotEntity(
    id = id,
    name = name,
    data = data,
    createdAt = createdAt
)