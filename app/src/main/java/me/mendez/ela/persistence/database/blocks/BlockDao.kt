package me.mendez.ela.persistence.database.blocks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface BlockDao {
    @Query("SELECT COUNT(*) FROM block")
    fun amount(): Flow<Int>

    @Insert
    suspend fun insert(block: Block)

    @Query("SELECT domain, COUNT(*) AS amount FROM block WHERE :start < date AND date < :end GROUP BY domain order by amount desc")
    fun blocksInTimeRange(start: Long, end: Long): Flow<List<DailyBlocks>>

    @Query("SELECT COUNT(*) AS amount FROM block WHERE :start < date AND date < :end")
    fun blockAmountInTimeRange(start: Long, end: Long): Flow<Int>

    fun dailyBlocks(): Flow<List<DailyBlocks>> {
        val (start, end) = today()
        return blocksInTimeRange(start, end)
    }

    fun dailyBlockAmount(): Flow<Int> {
        val (start, end) = today()
        return blockAmountInTimeRange(start, end)
    }

    fun today(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val end = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return Pair(start.timeInMillis, end.timeInMillis)
    }
}
