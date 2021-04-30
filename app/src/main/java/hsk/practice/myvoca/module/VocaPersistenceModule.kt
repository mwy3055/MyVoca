package hsk.practice.myvoca.module

import com.hsk.data.VocaPersistence
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RoomVocaPersistence

@Module
@InstallIn(SingletonComponent::class)
abstract class VocaPersistenceModule {

    @RoomVocaPersistence
    @Binds
    abstract fun bindRoomVocaPersistence(
            vocaPersistenceDatabase: VocaPersistenceDatabase): VocaPersistence
}