package hsk.practice.myvoca.module

import android.content.Context
import com.hsk.domain.TodayWordPersistence
import com.hsk.domain.VocaPersistence
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.room.persistence.TodayWordDatabase
import hsk.practice.myvoca.room.persistence.VocaPersistenceDatabase
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
object VocaPersistenceModule {

    @LocalVocaPersistence
    @Provides
    fun provideVocaPersistenceDatabase(
        @ApplicationContext context: Context
    ): VocaPersistence = VocaPersistenceDatabase(context)

    @LocalTodayWordPersistence
    @Provides
    fun provideTodayWordDatabase(
        @ApplicationContext context: Context
    ): TodayWordPersistence = TodayWordDatabase(context)
}