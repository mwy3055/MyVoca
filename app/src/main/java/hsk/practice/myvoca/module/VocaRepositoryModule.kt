package hsk.practice.myvoca.module

import com.hsk.domain.VocaPersistence
import com.hsk.domain.VocaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hsk.practice.myvoca.room.FakeVocaPersistence
import javax.inject.Qualifier

/**
 * 다른 VocaPersistence를 사용하는 VocaRepository를 추가하고 싶다면
 * 여기에 @Qualifier와 @Provides 메소드를 만들어야 한다.
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RoomVocaRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FakeVocaRepository

@Module
@InstallIn(SingletonComponent::class)
object VocaRepositoryModule {

    @RoomVocaRepository
    @Provides
    fun provideRoomVocaRepository(
        @RoomVocaPersistence vocaPersistence: VocaPersistence
    ): VocaRepository = VocaRepository(vocaPersistence)

    @FakeVocaRepository
    @Provides
    fun provideFakeVocaRepository(fakeVocaPersistence: FakeVocaPersistence): VocaRepository =
        VocaRepository(fakeVocaPersistence)
}