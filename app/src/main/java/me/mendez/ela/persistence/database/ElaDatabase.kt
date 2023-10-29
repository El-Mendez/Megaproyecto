package me.mendez.ela.persistence.database

import android.app.Application
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.mendez.ela.persistence.database.apps.SuspiciousApp
import me.mendez.ela.persistence.database.apps.SuspiciousAppDao
import me.mendez.ela.persistence.database.blocks.Block
import me.mendez.ela.persistence.database.blocks.BlockDao
import me.mendez.ela.persistence.database.blocks.chats.Message
import me.mendez.ela.persistence.database.blocks.chats.MessageDao
import me.mendez.ela.persistence.database.chat.ChatDao
import me.mendez.ela.persistence.database.chat.ChatMessage

const val DEFAULT_DATABASE_NAME = "ela_default.sqlite"
const val BLOCK_DATABASE_NAME = "ela_blocks.sqlite"

@Database(
    entities = [SuspiciousApp::class],
    version = 3,
)
@TypeConverters(Converters::class)
abstract class ElaDefaultDatabase : RoomDatabase() {
    abstract val suspiciousApps: SuspiciousAppDao
}

@Database(
    entities = [Block::class, Message::class, ChatMessage::class],
    version = 2,
    autoMigrations = [
        AutoMigration(1, 2)
    ]
)
@TypeConverters(Converters::class)
abstract class ElaBlockDatabase : RoomDatabase() {
    abstract val blocks: BlockDao
    abstract val messages: MessageDao
    abstract val chat: ChatDao
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
    fun provideChatDao(database: ElaBlockDatabase): ChatDao {
        return database.chat
    }

    @Provides
    fun provideMessageDao(database: ElaBlockDatabase): MessageDao {
        return database.messages
    }

    @Provides
    fun provideBlockDao(database: ElaBlockDatabase): BlockDao {
        return database.blocks
    }
}
