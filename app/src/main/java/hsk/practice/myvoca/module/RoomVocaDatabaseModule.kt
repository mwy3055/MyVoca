package hsk.practice.myvoca.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.framework.RoomVocaDatabase
import hsk.practice.myvoca.framework.VocaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomVocaDatabaseModule {

    @Singleton
    @Provides
    fun provideRoomVocaDatabase(
            @ApplicationContext context: Context)
            : RoomVocaDatabase = Room.databaseBuilder(context,
            RoomVocaDatabase::class.java,
            RoomVocaDatabase.vocaDatabaseName)
            .addMigrations(*RoomVocaDatabase.migrations)
            .build()

    @Singleton
    @Provides
    fun provideVocaDao(database: RoomVocaDatabase): VocaDao = database.vocaDao()!!
}