package me.mendez.ela.persistence.database.blocks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface BlockDao {
    @Query("SELECT COUNT(*) FROM message")
    fun amount(): Flow<Int>

    @Insert
    suspend fun insert(block: Block)
}
