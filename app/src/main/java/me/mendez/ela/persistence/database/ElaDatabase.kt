package me.mendez.ela.persistence.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.mendez.ela.persistence.database.apps.SuspiciousApp
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.database.blocks.Block
import me.mendez.ela.persistence.database.blocks.BlockDao
import me.mendez.ela.persistence.database.chats.Message
import me.mendez.ela.persistence.database.chats.MessageDao

const val DEFAULT_DATABASE_NAME = "ela_default.sqlite"
const val BLOCK_DATABASE_NAME = "ela_blocks.sqlite"

@Database(
    entities = [SuspiciousApp::class, Message::class],
    version = 2,
)
@TypeConverters(Converters::class)
abstract class ElaDefaultDatabase : RoomDatabase() {
    abstract val suspiciousApps: SuspiciousAppDao
    abstract val messages: MessageDao
}

@Database(
    entities = [Block::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class ElaBlockDatabase : RoomDatabase() {
    abstract val blocks: BlockDao
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideDefaultDatabase(app: Application): ElaDefaultDatabase {
        return Room
            .databaseBuilder(app, ElaDefaultDatabase::class.java, DEFAULT_DATABASE_NAME)
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBlocksDatabase(app: Application): ElaBlockDatabase {
        return Room
            .databaseBuilder(app, ElaBlockDatabase::class.java, BLOCK_DATABASE_NAME)
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSuspiciousAppDao(database: ElaDefaultDatabase): SuspiciousAppDao {
        return database.suspiciousApps
    }

    @Provides
    fun provideMessageDao(database: ElaDefaultDatabase): MessageDao {
        return database.messages
    }

    @Provides
    fun provideBlockDao(database: ElaBlockDatabase): BlockDao {
        return database.blocks
    }
}
