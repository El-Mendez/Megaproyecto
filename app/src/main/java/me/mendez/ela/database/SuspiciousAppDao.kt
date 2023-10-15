package me.mendez.ela.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SuspiciousAppDao {
    @Query("SELECT * FROM SuspiciousApp")
    fun getAll(): Flow<List<SuspiciousApp>>

    @Upsert
    suspend fun add(app: SuspiciousApp)

    @Query("SELECT * FROM SuspiciousApp where packageName = :packageName LIMIT 1")
    suspend fun findApp(packageName: String): SuspiciousApp?
}
