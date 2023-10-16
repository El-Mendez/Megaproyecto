package me.mendez.ela.persistence.database.apps

import android.content.pm.PackageInfo
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SuspiciousAppDao {
    @Query("SELECT * FROM SuspiciousApp")
    fun getAll(): Flow<List<SuspiciousApp>>

    @Query("SELECT * FROM SuspiciousApp")
    suspend fun all(): List<SuspiciousApp>

    @Query("DELETE FROM SuspiciousApp")
    suspend fun deleteAll()

    @Upsert
    suspend fun addAll(app: List<SuspiciousApp>)

    @Query("SELECT * FROM SuspiciousApp where packageName = :packageName LIMIT 1")
    suspend fun findApp(packageName: String): SuspiciousApp?

    @Transaction
    suspend fun setSuspiciousApps(packageInfo: List<PackageInfo>) {
        deleteAll()
        addAll(
            packageInfo
                .map { SuspiciousApp(it.packageName) }
        )
    }
}
