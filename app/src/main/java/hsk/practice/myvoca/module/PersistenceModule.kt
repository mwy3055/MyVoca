package hsk.practice.myvoca.module

import android.content.Context
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.room.persistence.TodayWordPersistenceRoom
import hsk.practice.myvoca.room.persistence.VocaPersistenceRoom
import javax.inject.Qualifier


/**
 * VocaPersistence의 여러 구현을 제공하는 모듈.
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalVocaPersistence

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalTodayWordPersistence

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @LocalVocaPersistence
    @Provides
    fun provideVocaPersistenceDatabase(
        @ApplicationContext context: Context
    ): VocaPersistence = VocaPersistenceRoom(context)

    @LocalTodayWordPersistence
    @Provides
    fun provideTodayWordDatabase(
        @ApplicationContext context: Context
    ): TodayWordPersistence = TodayWordPersistenceRoom(context)
}