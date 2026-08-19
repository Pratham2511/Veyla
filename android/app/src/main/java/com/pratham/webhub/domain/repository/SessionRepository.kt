package com.pratham.webhub.domain.repository

import com.pratham.webhub.domain.model.SessionSnapshot
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    fun getSessionSnapshots(): Flow<List<SessionSnapshot>>

    suspend fun saveSession(name: String)

    suspend fun restoreSession(snapshotId: String)

    fun getLastSession(): Flow<SessionSnapshot?>

    suspend fun deleteSession(snapshotId: String)
}