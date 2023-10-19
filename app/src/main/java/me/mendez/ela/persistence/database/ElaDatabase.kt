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
import me.mendez.ela.persistence.database.chats.Message
import me.mendez.ela.persistence.database.chats.MessageDao

@Database(
    entities = [SuspiciousApp::class, Message::class],
    version = 2,
)
@TypeConverters(Converters::class)
abstract class ElaDatabase : RoomDatabase() {
    abstract val suspiciousApps: SuspiciousAppDao
    abstract val messages: MessageDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideDatabase(app: Application): ElaDatabase {
        return Room
            .databaseBuilder(app, ElaDatabase::class.java, "ela_database")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSuspiciousAppDao(database: ElaDatabase): SuspiciousAppDao {
        return database.suspiciousApps
    }

    @Provides
    fun provideMessageDao(database: ElaDatabase): MessageDao {
        return database.messages
    }
}
